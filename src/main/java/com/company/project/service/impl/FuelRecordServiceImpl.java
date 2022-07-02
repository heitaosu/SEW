package com.company.project.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.company.project.configurer.ProjectConfig;
import com.company.project.core.AbstractService;
import com.company.project.core.IGlobalCache;
import com.company.project.core.ServiceException;
import com.company.project.dao.FuelRecordMapper;
import com.company.project.model.FuelRecord;
import com.company.project.model.FuelRecordDO;
import com.company.project.model.FuelState;
import com.company.project.model.FuelValue;
import com.company.project.service.FuelRecordService;
import com.company.project.util.FileUtil;
import com.company.project.util.SmbUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
@Slf4j
@Service
@Transactional
public class FuelRecordServiceImpl extends AbstractService<FuelRecord> implements FuelRecordService {

    @Resource
    private FuelRecordMapper fuelRecordMapper;
    @Resource
    private IGlobalCache globalCache;
    @Resource
    private ProjectConfig projectConfig;

    public static NumberFormat nf = NumberFormat.getNumberInstance();

    //存储铭牌的位置的文件夹
    private String REMOTE_WRITE_MP_DIR = "\\mp\\";
    // 读文件的共享盘 读文件的目标基础路径
    private String REMOTE_READ_DIR = "\\E25\\025\\@1\\";
    // 写文件的文件名后缀
    private String FILE_NAME_SUF = ".001";
    //一个空格符
    private String STR_BLANK_SYM = " ";

    // 当前是否在扫码注油(判断扫码注油有没有结束 false:表示当前扫码加油没有结束 true:表示当前扫码注油已经结束)
    private boolean scan_is_end = true;
    // 扫码注油的二维码信息(订单号)
    private String scan_start_code = null;
    // 扫码注油的开始时间
    private Date scan_start_date = null;
    // 扫码操作的操作人id
    private Integer scan_oper_id = null;

    /**
     *
     * @param scanCode 传扫码的订单号
     * @param operId   操作人ID
     */
    @Override
    public void scanStart(String scanCode,Integer operId,FuelRecord fuelRecord) throws ServiceException {
        if (StringUtils.isEmpty(scanCode)){
            throw new ServiceException("扫码的订单号不能为空");
        }
        if (operId == null){
            throw new ServiceException("operId 不能为空");
        }
        scan_is_end = false;
        scan_start_code = scanCode;
        scan_start_date = new Date();
        scan_oper_id = operId;

        globalCache.set(FuelState.PAGE_INSTALLTYPE_200.getSwitchKey(),fuelRecord.getInstallType());
        globalCache.set(FuelState.PAGE_MODEL_201.getSwitchKey(),fuelRecord.getModelType());
        globalCache.set(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey(),fuelRecord.getSequenceCode());
        globalCache.set(FuelState.PAGE_CODE_203.getSwitchKey(),scanCode);
        globalCache.set(FuelState.PAGE_CODE_204.getSwitchKey(),String.valueOf(fuelRecord.getFuelSetVal()));
    }

    @Override
    public boolean scanIsEnd() {
        return scan_is_end;
    }

    /**
     * 加油结束
     */
    @Override
    public void scanEnd() {
        scan_is_end = true;
        scan_start_code = null;
        scan_start_date = null;
        scan_oper_id = null;

        globalCache.del(FuelState.PAGE_INSTALLTYPE_200.getSwitchKey());
        globalCache.del(FuelState.PAGE_MODEL_201.getSwitchKey());
        globalCache.del(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey());
        globalCache.del(FuelState.PAGE_CODE_203.getSwitchKey());
        globalCache.del(FuelState.PAGE_CODE_204.getSwitchKey());
        globalCache.del(FuelState.PAGE_CODE_205.getSwitchKey());
    }

    @Override
    public String getScanCode() {
        return scan_start_code;
    }

    @Override
    public Date getScanDate() {
        return scan_start_date;
    }

    @Override
    public Integer getScanOperId() {
        return scan_oper_id;
    }

    /**
     * 查询某个时间段 扫码加油的数据
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    @Override
    public List<FuelRecordDO> findByCreateTime(long createTimeStart, long createTimeEnd) {
        return fuelRecordMapper.findByCreateTime(createTimeStart,createTimeEnd);
    }

    /**
     * 根据工单号查询历史的加油记录
     * @param workOrder
     * @return
     */
    @Override
    public List<FuelRecordDO> findByWorkOrder(String workOrder) {
        return fuelRecordMapper.findByWorkOrder(workOrder);
    }

    /**
     * 扫码加油完成
     * @param userId
     * @param realName 操作人真实姓名
     * @return
     */
    @Override
    public String fuelComplete(Integer userId,String realName) throws ServiceException{
        if (scanIsEnd()){
            this.scanEnd();
            log.error("fuelComplete 当前机器没有已扫码的机器在加油");
            return  null;
        }
        String code = null;
        String fileContent = null;
        try {
            code = getScanCode();
            fileContent = getReadRemoteFileContent(code);
            if (StringUtils.isEmpty(fileContent)){
                throw new ServiceException("该订单号不存在,请检查远程订单文件是否存在,订单id=" + code);
            }
        }catch (Exception e){
            throw new ServiceException("读取订单信息失败，请检查网络连接是否正常，或联系管理员");
        }

        FuelRecord fuelRecord = null;
        try {
            fuelRecord = stringTransformBeanV2(fileContent, code);
            Double svval = Double.valueOf(globalCache.get(FuelValue.FUEL_STATE_53.getSwitchKey()).toString());
            nf.setMaximumFractionDigits(1);
            //注油量设定值
            fuelRecord.setFuelSetVal(Double.valueOf(nf.format(svval)));
            fuelRecord.setSequenceCode(String.valueOf(globalCache.get(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey())));
        }catch (Exception e){
            throw new ServiceException("订单信息中的数据转化异常，请检查近期订单信息格式是否已变更或联系管理员");
        }

        List<FuelRecordDO> list = this.findByWorkOrder(fuelRecord.getWorkOrder()+"");
        // 如果同一订单下  已经有加油记录则不用写铭牌文件了
        if (CollectionUtils.isEmpty(list)){
            String fileName = code + FILE_NAME_SUF;
            // String writeFileContent = fuelRecord.getFuelRealVal() + STR_BLANK_SYM + fuelRecord.getFuelStart() + STR_BLANK_SYM + fuelRecord.getFuelEnd() + STR_BLANK_SYM + realName;
            try {
                String writeFileContent = fuelRecord.getFuelRealVal() + "";
                //将需要铭牌打印的信息输出到本地文件
                FileUtil.writeContent(projectConfig.getSYSTEM_SEW_WRITE_FILE_DIR(), fileName, writeFileContent, false);
            }catch (Exception e){
                throw new ServiceException("上传铭牌打印的信息写到本地电脑出错，请检查文件是否被占用");
            }
            try {
                String localAllFileName = projectConfig.getSYSTEM_SEW_WRITE_FILE_DIR() + fileName;
                //将本地的铭牌信息同步到共享文件盘
                SmbUtil.uploadFile(projectConfig.getSMB_WRITE_REMOTE_HOST(), projectConfig.getSMB_WRITE_USERNAME(),projectConfig.getSMB_WRITE_PASSWORD(),projectConfig.getSMB_WRITE_SHARE_PATH(), projectConfig.getSMB_WRITE_SHARE_BASEDIR() + REMOTE_WRITE_MP_DIR,localAllFileName);
            }catch (Exception e){
                throw new ServiceException("上传铭牌打印的信息到共享盘时出错,请检查本地网络连接或者共享盘是否正常");
            }
        }
        fuelRecord.setFuelStart(scan_start_date.getTime());
        this.save(fuelRecord);
        this.scanEnd();
        return  null;
    }

    /**
     * 获取远程文件的内容
     * @param code
     * @return
     */
    @Override
    public String getReadRemoteFileContent(String code) throws ServiceException{
        String content = null;
        try{
            String fileName = code + FILE_NAME_SUF;
            log.error("getReadRemoteFileContent fileName="+fileName);
            content = SmbUtil.readOneFileOnDirTree(projectConfig.getSMB_READ_REMOTE_HOST(),projectConfig.getSMB_READ_USERNAME(),projectConfig.getSMB_READ_PASSWORD(),projectConfig.getSMB_READ_SHARE_PATH(),REMOTE_READ_DIR,fileName);
        }catch(Exception e){
            log.error(e.getMessage());
            throw new ServiceException("获取订单内容出现问题，请检查网络连接状态然后检查共享地址的订单内容是否存在");
        }
        return content;
    }

    /**
     * 将文件的内容转换为FuelRecord
     * @param fileContent
     * @return
     */
    /*private FuelRecord stringTransformBean(String fileContent,String code){
        //处理文件内容多余的空格
        fileContent = fileContent.replaceAll( "\\s+", STR_BLANK_SYM);
        fileContent = fileContent.replace(STR_BLANK_SYM,"_");
        String[] array = fileContent.split("_");
        FuelRecord fuelRecord = new FuelRecord();
        try {
            Double dval = Double.valueOf(globalCache.get(FuelValue.FUEL_STATE_62.getSwitchKey()).toString());
            nf.setMaximumFractionDigits(2);
            //注油量实际值
            fuelRecord.setFuelRealVal(Double.valueOf(nf.format(dval)));

            Double svval = Double.valueOf(globalCache.get(FuelValue.FUEL_STATE_53.getSwitchKey()).toString());
            nf.setMaximumFractionDigits(1);
            //注油量设定值
            fuelRecord.setFuelSetVal(Double.valueOf(nf.format(svval)));
            nf.setMaximumFractionDigits(0);
            //上传到铭牌的注油量
            fuelRecord.setTagRealVal(Double.valueOf(nf.format(dval)));
        }catch (Exception e){
            throw new ServiceException("获取缓存中的实际注油量不正确,请检查,如果强制完成请尝试修改实际注油量后,再次点击次按钮完成");
        }
        fuelRecord.setCreateTime(System.currentTimeMillis());
        fuelRecord.setFuelStart(scan_start_date.getTime());
        fuelRecord.setFuelEnd(System.currentTimeMillis());
        fuelRecord.setWorkOrder(Long.valueOf(code));
        try {
            //安装方式
            fuelRecord.setInstallType(array[15]);
            //型号
            fuelRecord.setModelType(array[1]);
            //序列号
            fuelRecord.setSequenceCode(array[3]);
        }catch (Exception e){
            throw new ServiceException("获取共享盘中的订单信息[安装方式、型号、序列号]不正确,请检查共享盘中的订单信息或者订单信息或格式是否已经变更");
        }
        fuelRecord.setOperId(scan_oper_id);
        return fuelRecord;
    }*/

    public FuelRecord stringTransformBeanV2(String fileContent,String code) throws ServiceException{
        FuelRecord fuelRecord = new FuelRecord();
        try {
            Double dval = Double.valueOf(globalCache.get(FuelValue.FUEL_STATE_62.getSwitchKey()).toString());
            nf.setMaximumFractionDigits(0);
            //注油量实际值
            fuelRecord.setFuelRealVal(Double.valueOf(nf.format(dval)));

           /* Double svval = Double.valueOf(globalCache.get(FuelValue.FUEL_STATE_53.getSwitchKey()).toString());
            nf.setMaximumFractionDigits(1);
            //注油量设定值
            fuelRecord.setFuelSetVal(Double.valueOf(nf.format(svval)));*/
            nf.setMaximumFractionDigits(0);
            //上传到铭牌的注油量
            fuelRecord.setTagRealVal(Double.valueOf(nf.format(dval)));
        }catch (Exception e){
            throw new ServiceException("获取缓存中的实际注油量不正确,请检查,如果强制完成请尝试修改实际注油量后,再次点击次按钮完成");
        }
        fuelRecord.setCreateTime(System.currentTimeMillis());
        fuelRecord.setFuelStart(System.currentTimeMillis());
        fuelRecord.setFuelEnd(System.currentTimeMillis());
        fuelRecord.setWorkOrder(Long.valueOf(code));
        try {
            //安装方式
            fuelRecord.setInstallType(fileContent.substring(693,708).trim().split(":")[1]);
            //型号
            fuelRecord.setModelType(fileContent.substring(4,34).trim());
            //序列号
            fuelRecord.setSequenceCode(fileContent.substring(267,297).trim().replace(".",""));
            // 油品型号
            fuelRecord.setOilType(fileContent.substring(297,327).trim());
        }catch (Exception e){
            throw new ServiceException("获取共享盘中的订单信息[安装方式、型号、序列号]不正确,请检查共享盘中的订单信息或者订单信息或格式是否已经变更");
        }
        try {
            //注油量设定值（从订单中读取的如果有的话）
            fuelRecord.setFuelSetVal(Double.valueOf(nf.format(fileContent.substring(760,768).trim())));
        }catch (Exception e){
            log.error("注油量从订单中读取错误，注油量设定为默认值");
        }
        fuelRecord.setOperId(scan_oper_id);
        return fuelRecord;
    }

    /**
     * 比较当前输入的序列号是否有效
     * @param currentSeq
     * @param remoteSeq
     * @return
     */
    @Override
    public boolean compareSeq(String currentSeq, String remoteSeq) {
        if (StringUtils.isEmpty(currentSeq) || StringUtils.isEmpty(remoteSeq)){
            return false;
        }
        String reSeq = remoteSeq.substring(2,12);
        String currs = currentSeq.substring(0,10);
        log.error("currentSeq="+currentSeq+",remoteSeq="+remoteSeq);
        if (!currs.equals(reSeq)){
            return false;
        }
        return true;
    }
}
