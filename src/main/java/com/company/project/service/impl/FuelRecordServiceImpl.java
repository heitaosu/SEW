package com.company.project.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.company.project.core.AbstractService;
import com.company.project.core.ProjectConstant;
import com.company.project.core.ServiceException;
import com.company.project.dao.FuelRecordMapper;
import com.company.project.model.FuelRecord;
import com.company.project.service.FuelRecordService;
import com.company.project.util.DateUtil;
import com.company.project.util.FileUtil;
import com.company.project.util.SmbUtil;
import com.company.project.websocket.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
@Service
@Transactional
public class FuelRecordServiceImpl extends AbstractService<FuelRecord> implements FuelRecordService {
    @Resource
    private FuelRecordMapper fuelRecordMapper;

    @Autowired
    private PushService pushService;

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
     * @param scanCode  传扫码的订单号
     */
    @Override
    public void scanStart(String scanCode,Integer operId) {
        if (StringUtils.isEmpty(scanCode)){
            throw new ServiceException("scanCode 不能为空");
        }
        if (operId == null){
            throw new ServiceException("operId 不能为空");
        }
        scan_is_end = false;
        scan_start_code = scanCode;
        scan_start_date = new Date();
        this.startTimer();
    }

    @Override
    public boolean scanIsEnd() {
        return scan_is_end;
    }

    /**
     * 加油结束
     */
    public void scanEnd() {
        scan_is_end = true;
        scan_start_code = null;
        scan_start_date = null;
        scan_oper_id = null;
    }

    @Override
    public String getScanCode() {
        return scan_start_code;
    }

    @Override
    public Date getScanDate() {
        return scan_start_date;
    }

    public Integer getScanOperId() {
        return scan_oper_id;
    }

    /**
     * 点加油完成按钮操作
     * 如果返回值不为空 则操作失败 失败信息存储在返回值中
     * @return
     */
  /*  @Override
    public String fuelComplete(Integer userId) {
        String code = getScanCode();
        String str = getRemoteFileContent(code);
        FuelRecord fuelRecord = stringTransformBean(str,code);
        this.save(fuelRecord);
        String writeFileContent = fuelRecord.getFuelRealVal() + " " + fuelRecord.getFuelStart() + " " + fuelRecord.getFuelEnd();
        FileUtil.writeContent("D:\\",code+".001",writeFileContent);
        this.scanEnd();
        return  null;
    }*/

    @Override
    public String fuelComplete(Integer userId) {
        String code = getScanCode();
        FuelRecord fuelRecord = new FuelRecord();
        fuelRecord.setWorkOrder(Long.valueOf(code));
        fuelRecord.setModelType("55666");

        this.save(fuelRecord);
        String writeFileContent = fuelRecord.getFuelRealVal() + " " + fuelRecord.getFuelStart() + " " + fuelRecord.getFuelEnd();
        FileUtil.writeContent("D:\\",code+".001",writeFileContent);
        this.scanEnd();
        return  null;
    }

    /**
     * 获取远程文件的内容
     * @param code
     * @return
     */
    private String getRemoteFileContent(String code){
        String yyyyMM = DateUtil.DateToString(new Date(),DateUtil.DateStyle.YYYYMM);
        return SmbUtil.readOneFileString(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH+yyyyMM,"",code+".001");
    }

    /**
     * 将文件的内容转换为FuelRecord
     * @param fileContent
     * @return
     */
    private FuelRecord stringTransformBean(String fileContent,String code){
        //处理文件内容多余的空格
        fileContent = fileContent.replaceAll( "\\s+", " ");
        fileContent = fileContent.replace(" ","_");
        String[] array = fileContent.split("_");
        FuelRecord fuelRecord = new FuelRecord();
        fuelRecord.setWorkOrder(Long.valueOf(code));
        fuelRecord.setInstallType(array[15]);
        fuelRecord.setModelType(array[1]);
        fuelRecord.setSequenceCode(array[3]);
        return fuelRecord;
    }

    /**
     * 异步方法 计时器计算加油时长 如果超过的规定时长则强制结束加油
     */
    public void startTimer(){
        new Thread(new Runnable() {
            @Override
             public void run() {
                Date date = new Date();
                for (int i = 0; i < 30; i++){
                    date.setTime(System.currentTimeMillis());
                    int minutes = DateUtil.getIntervalMinutes(getScanDate(),date);
                    if (minutes >= 30){
                        fuelComplete(getScanOperId());
                        pushService.pushMsgToAll(String.format(ProjectConstant.VAL_ARRAY[3], "1","扫码加油在规定的时间内没有点击扫码完成按钮,系统自动完成,请确认"));
                        break;
                    }
                    try {
                        //一分钟检测一次
                        Thread.sleep(1000l*60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                             }
        }).start();
    }
}
