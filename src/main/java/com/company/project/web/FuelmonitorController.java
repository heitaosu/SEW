package com.company.project.web;

import com.alibaba.excel.util.StringUtils;
import com.company.project.configurer.ProjectConfig;
import com.company.project.core.*;
import com.company.project.model.*;
import com.company.project.service.FuelRecordService;
import com.company.project.service.WarnRecordService;
import com.company.project.util.JwtUtil;
import com.company.project.util.SmbUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 操控页面的请求
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
public class FuelmonitorController {
    @Autowired
    private IGlobalCache globalCache;
    @Autowired
    private ProjectConfig projectConfig;
    @Resource
    private FuelRecordService fuelRecordService;
    @Resource
    private WarnRecordService warnRecordService;

    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
    //
    private String[] btn_on_flag = new String[]{"True","False"};
    private String btn_connect_flag = ":";
    //按钮高低频间隔时间 毫秒
    private long timeMil = 100l;
    // 页面初始化值的模板
    private String INIT_VAL_FORMAT = "val_%s_%s";

    private String INIT_BTN_FORMAT = "btn_%s_%s";

    private String PAGE_LOOP_SWITCH = "False";


    /**
     * 按钮操作
     * @param type
     * @return
     */
    @PostMapping("/btn.json")
    public Result btn(HttpServletRequest request,@RequestParam(required=true) Integer type,final String value) throws ServiceException {
        String  val =  value;
        // 扫码加油如未结束不允许手动加油或停止
        if (FuelState.FUEL_STATE_11.getState() == type || FuelState.FUEL_STATE_12.getState() == type){
            this.scanIsEndMsg();
        }
        if (FuelState.FUEL_STATE_16.getState() == type && StringUtils.isEmpty(val)){
            return ResultGenerator.genFailResult("循环OR注油开关的值不能为空,请检查");
        }
        if (FuelState.FUEL_STATE_18.getState() == type && fuelRecordService.scanIsEnd()){
            return ResultGenerator.genFailResult("扫码加油结束后不能操作补油按钮");
        }

        //加油完成
        if (FuelState.FUEL_STATE_14.getState() == type){
            String token = request.getHeader("access_token");
            String userId = JwtUtil.getUserId(token);
            String realName = JwtUtil.getRealName(token);
            fuelRecordService.fuelComplete(Integer.valueOf(userId),realName);
        } else if (FuelState.FUEL_STATE_16.getState() == type){
           /* if (btn_on_flag[1].equals(PAGE_LOOP_SWITCH)){
                PAGE_LOOP_SWITCH = btn_on_flag[0];
            }else {
                PAGE_LOOP_SWITCH = btn_on_flag[1];
            }
            val = (btn_on_flag[0].equals(PAGE_LOOP_SWITCH)) ? "1" : "0";*/


            /*//不用特殊处理了
            Object obj = globalCache.get(FuelState.PAGE_CODE_206.getSwitchKey());

            if (obj != null){
                if (btn_on_flag[0].equals(obj)){
                    val = "0";
                }else {
                    val = "1";
                }
                globalCache.set(FuelState.PAGE_CODE_206.getSwitchKey(),("1".equals(val) ? btn_on_flag[0] : btn_on_flag[1]));
            }else {
                globalCache.set(FuelState.PAGE_CODE_206.getSwitchKey(),btn_on_flag[0]);
                val = "1";
            }
            log.error("obj={},val={},FuelState.FUEL_STATE_16.getSwitchKey()={}",obj,val,globalCache.get(FuelState.FUEL_STATE_16.getSwitchKey()));*/
        }

        log.error("value="+ ("1".equals(val) ? btn_on_flag[0] : btn_on_flag[1]));
        String key = FuelState.getSwitchKey(type);

        if (FuelState.FUEL_STATE_11.getState() == type
                || FuelState.FUEL_STATE_12.getState() == type
                || FuelState.FUEL_STATE_13.getState() == type
                || FuelState.FUEL_STATE_14.getState() == type
                || FuelState.FUEL_STATE_18.getState() == type
                || FuelState.FUEL_STATE_19.getState() == type){
            // 手动加油启停状态单独处理
            if (FuelState.FUEL_STATE_11.getState() == type){
                val = val.equals("1") ? btn_on_flag[0] : btn_on_flag[1];
                globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelState.FUEL_STATE_20.getSwitchKey() + btn_connect_flag + val);
            }
            globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, key + btn_connect_flag + btn_on_flag[0]);
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(timeMil);
                    } catch (InterruptedException e) {
                        log.error("threadPool.execute error");
                    }
                    globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, key + btn_connect_flag + btn_on_flag[1]);
                }
            });

            // 数据复位
            if(FuelState.FUEL_STATE_19.getState() == type){
                fuelRecordService.scanEnd();
                //设定注油目标值0
                globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + btn_connect_flag + 0);
            }
        }else {
            String msg = key + btn_connect_flag + ("1".equals(val) ? btn_on_flag[0] : btn_on_flag[1]);
            log.error("publish : "+msg);
            globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, msg);
        }
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 改变设置属性值
     * @param type
     * @param value
     * @return
     */
    @PostMapping("/setValue.json")
    public Result set(@RequestParam(required=true) Integer type, @RequestParam(required=true) Number value) {
        String key = FuelValue.getSwitchKey(type);
        if (StringUtils.isEmpty(key)){
            return ResultGenerator.genFailResult("没有找到类型[" + type + "]对应的值,请检查");
        }
        globalCache.set(key,value);
        String msg = key + ":" + value;
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL,msg);
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 扫码加油开始
     * @param code
     * @return
     */
    @PostMapping("/scanstart.json")
    public Result  scanstart(HttpServletRequest request,@RequestParam(required=true) String code,@RequestParam(required=true) String sequenceCode) throws ServiceException{
        if (!NumberUtils.isNumber(code) || !NumberUtils.isNumber(sequenceCode)){
            return ResultGenerator.genFailResult("扫码的订单ID或者序列号不合法,必须全部为数字");
        }
        if (code.length() < 8 || code.length() >15){
            return ResultGenerator.genFailResult("扫码信息不正确,请重新扫码,订单ID长度必须是8-15位");
        }
        if (sequenceCode.length() < 12 || sequenceCode.length() >20){
            return ResultGenerator.genFailResult("扫码信息不正确,请重新扫码,序列号长度必须是12-20位");
        }
        String content = fuelRecordService.getReadRemoteFileContent(code);
        if (StringUtils.isEmpty(content)){
            return ResultGenerator.genFailResult("系统中的订单不存在,请检查共享文件盘中文件是否存在,订单ID编号为:" + code);
        }
        FuelRecord fuelRecord = fuelRecordService.stringTransformBeanV2(content,code);

        if (!fuelRecordService.compareSeq(sequenceCode,fuelRecord.getSequenceCode())){
            return ResultGenerator.genFailResult("扫码输入的序列号与对应的订单号信息中的序列号对比不正确，请检查或重新扫码");
        }
        this.scanIsEndMsg();
        String token = request.getHeader("access_token");
        String userId = JwtUtil.getUserId(token);


        //如果当前订单号下已经有历史的加油记录则注油目标值设定为之前的真实加油量
        Double reaVal = 0d;
        List<FuelRecordDO> list = fuelRecordService.findByWorkOrder(fuelRecord.getWorkOrder()+"");
        if (!CollectionUtils.isEmpty(list)){
            String temVal = list.get(0).getFuelRealVal();
            reaVal = Double.valueOf(temVal);
        }else if(fuelRecord.getFuelSetVal() != null){//如果当前工单没有加过油则取工单信息中的注油目标值
            reaVal = fuelRecord.getFuelSetVal();
        }else {//如果以上两个都没有则注油目标值设定为默认值
            reaVal = Double.valueOf(projectConfig.getSYSTEM_INIT_VAL_53_NUM());
        }
        fuelRecord.setFuelSetVal(reaVal);
        log.error("set 注油目标值为={}",fuelRecord.getFuelSetVal());
        fuelRecordService.scanStart(code,Integer.valueOf(userId),fuelRecord);

        //设定注油目标值
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + btn_connect_flag + fuelRecord.getFuelSetVal());
        //扫码加油开始按钮打开
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL,FuelState.FUEL_STATE_13.getSwitchKey() + btn_connect_flag + btn_on_flag[0]);

        //给定一个低电频到PLC
        threadPool.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(timeMil);
                } catch (InterruptedException e) {
                    log.error("threadPool.execute error");
                }
                globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelState.FUEL_STATE_13.getSwitchKey() + btn_connect_flag + btn_on_flag[1]);
            }
        });
        log.error("code="+code);
        return ResultGenerator.genSuccessResult(fuelRecord);
    }

    /**
     * 注油操作页面的初始化值
     * @return
     */
    @GetMapping("/initValue.json")
    public Result initValue(){
        List<String> list = new ArrayList<String>();
        Object fuel_state_51 = globalCache.get(FuelValue.FUEL_STATE_51.getSwitchKey());
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_51.getState(),(fuel_state_51 == null ? projectConfig.getSYSTEM_INIT_VAL_51_NUM() : fuel_state_51)));
        Object fuel_state_52 = globalCache.get(FuelValue.FUEL_STATE_52.getSwitchKey());
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_52.getState(),(fuel_state_52 == null ? projectConfig.getSYSTEM_INIT_VAL_52_NUM() : fuel_state_52)));
        //Object fuel_state_53 = globalCache.get(FuelValue.FUEL_STATE_53.getSwitchKey());
        //list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_53.getState(),(fuel_state_53 == null ? projectConfig.getSYSTEM_INIT_VAL_53_NUM() : fuel_state_53)));
        Object fuel_state_54 = globalCache.get(FuelValue.FUEL_STATE_54.getSwitchKey());
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_54.getState(),(fuel_state_54 == null ? projectConfig.getSYSTEM_INIT_VAL_54_NUM() : fuel_state_54)));


        if(!fuelRecordService.scanIsEnd()){
            // 注油目标值
            Object fuel_state_204 = globalCache.get(FuelState.PAGE_CODE_204.getSwitchKey());
            if (fuel_state_204 != null){
                list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_53.getState(),fuel_state_204));
            }

            Object fuel_state_62 = globalCache.get(FuelValue.FUEL_STATE_62.getSwitchKey());
            if (fuel_state_62 != null){
                // 实时注油值
                list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_62.getState(),fuel_state_62));
            }

            Object fuel_state_63 = globalCache.get(FuelValue.FUEL_STATE_63.getSwitchKey());
            if (fuel_state_63 != null){
                // 剩余注油值
                list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_63.getState(),fuel_state_63));
            }


            Object fuel_btn_11 = globalCache.get(FuelState.FUEL_STATE_20.getSwitchKey());
            if (fuel_btn_11 != null){
                // 手动加油开关
                list.add(String.format(INIT_BTN_FORMAT,FuelState.FUEL_STATE_11.getState(),fuel_btn_11));
            }

            Object fuel_btn_16 = globalCache.get(FuelState.FUEL_STATE_16.getSwitchKey());
            if (fuel_btn_16 != null){
                // 循环or注油
                list.add(String.format(INIT_BTN_FORMAT,FuelState.FUEL_STATE_16.getState(),fuel_btn_16));
            }

            Object fuel_btn_17 = globalCache.get(FuelState.FUEL_STATE_17.getSwitchKey());
            if (fuel_btn_17 != null){
                // 消音按钮
                list.add(String.format(INIT_BTN_FORMAT,FuelState.FUEL_STATE_17.getState(),fuel_btn_17));
            }

            // 安装方式
            Object page_installType_200 = globalCache.get(FuelState.PAGE_INSTALLTYPE_200.getSwitchKey());
            if (page_installType_200 != null){
                list.add(FuelState.PAGE_INSTALLTYPE_200.getSwitchKey() + page_installType_200);
            }

            // 型号
            Object PAGE_MODEL_201 = globalCache.get(FuelState.PAGE_MODEL_201.getSwitchKey());
            if (PAGE_MODEL_201 != null){
                list.add(FuelState.PAGE_MODEL_201.getSwitchKey() + PAGE_MODEL_201);
            }
            // 序列号
            Object PAGE_SEQUENCECODE_202 = globalCache.get(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey());
            if (PAGE_SEQUENCECODE_202 != null){
                list.add(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey() + PAGE_SEQUENCECODE_202);
            }
            // 工单号
            Object PAGE_CODE_203 = globalCache.get(FuelState.PAGE_CODE_203.getSwitchKey());
            if (PAGE_CODE_203 != null){
                list.add(FuelState.PAGE_CODE_203.getSwitchKey() + PAGE_CODE_203);
            }
        }else {
            list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_53.getState(),0));

            list.add(FuelState.PAGE_CODE_203.getSwitchKey()+"");
            list.add(FuelState.PAGE_SEQUENCECODE_202.getSwitchKey()+"");
            list.add(FuelState.PAGE_MODEL_201.getSwitchKey()+"");
            list.add(FuelState.PAGE_INSTALLTYPE_200.getSwitchKey()+"");

        }
        //globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_51.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_51_NUM());
        //globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_52.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_52_NUM());
        //globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_53_NUM());
        //globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_54.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_54_NUM());
        return ResultGenerator.genSuccessResult(list);
    }

    /**
     * 根据告警时间查询历史告警记录
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    @GetMapping("/listWarnRecord.json")
    public Result listWarnRecord(@RequestParam(required=true)Long createTimeStart, @RequestParam(required=true)Long createTimeEnd){
        List<WarnRecord> list = warnRecordService.findByCreateTime(createTimeStart,createTimeEnd);
        return ResultGenerator.genSuccessResult(list);
    }


    public void scanIsEndMsg() throws ServiceException {
        if (!fuelRecordService.scanIsEnd()){
            throw new ServiceException("本次扫码注油还未结束,不允许此操作,请等待当前注油结束,如已结束请按扫码加油结束按钮结束本次注油");
        }
    }

    /**
     * 测试读取共享文件
     * @param request
     * @param fileName  共享文件的名称
     * @return
     */
    @GetMapping("/testReadRemote.json")
    public Result  testReadRemote(HttpServletRequest request, @RequestParam(required=true) String fileName) throws Exception {
        String removeDir = "\\E25\\025\\@1\\";
        // System.out.println(SmbUtil.SMB_REMOTE_HOST+":"+SmbUtil.SMB_USERNAME+":"+SmbUtil.SMB_PASSWORD+":"+SmbUtil.SMB_SHARE_PATH);
        //SmbUtil.listFile(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH,"E25");
        //String content = SmbUtil.readOneFileString(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH,projectConfig.getSystemShareRemoteDir(),fileName);
        String content = SmbUtil.readOneFileOnDirTree(projectConfig.getSMB_READ_REMOTE_HOST(),projectConfig.getSMB_READ_USERNAME(),projectConfig.getSMB_READ_PASSWORD(),projectConfig.getSMB_READ_SHARE_PATH(),removeDir,fileName);
        System.out.println("content="+content);
        return ResultGenerator.genSuccessResult(content);
    }

    /**
     * 测试写文件到共享文件
     * @param request
     * @param fileAllName  本地文件全限地址
     * @return
     */
    @GetMapping("/testWriteRemote.json")
    public Result  testWriteRemote(HttpServletRequest request, String fileAllName) throws Exception {
        String removeDir = "\\SC\\Public\\18 注油机\\";
        SmbUtil.uploadFile(projectConfig.getSMB_WRITE_REMOTE_HOST(), projectConfig.getSMB_WRITE_USERNAME(),projectConfig.getSMB_WRITE_PASSWORD(),projectConfig.getSMB_WRITE_SHARE_PATH(), removeDir,"D:/7888888888.001");
        //SmbFileUtil.uploadFile(null,"D:/7888888888.001");
        return ResultGenerator.genSuccessResult();
    }
}