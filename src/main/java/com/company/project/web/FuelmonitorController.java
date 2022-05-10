package com.company.project.web;

import com.alibaba.excel.util.StringUtils;
import com.company.project.configurer.ProjectConfig;
import com.company.project.core.*;
import com.company.project.model.FuelState;
import com.company.project.model.FuelValue;
import com.company.project.service.FuelRecordService;
import com.company.project.util.JwtUtil;
import com.company.project.util.SmbUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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

    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
    //
    private String[] btn_on_flag = new String[]{"True","False"};
    private String btn_connect_flag = ":";
    //按钮高低频间隔时间 毫秒
    private long timeMil = 100l;
    // 页面初始化值的模板
    private String INIT_VAL_FORMAT = "val_%s_%s";

    /**
     * 按钮操作
     * @param type
     * @return
     */
    @PostMapping("/btn.json")
    public Result btn(HttpServletRequest request,@RequestParam(required=true) Integer type,final String value) throws IOException {
        String  val =  value;
        // 扫码加油如未结束不允许手动加油或停止
        if (FuelState.FUEL_STATE_11.getState() == type || FuelState.FUEL_STATE_12.getState() == type){
            this.scanIsEndMsg();
        }
        if (FuelState.FUEL_STATE_16.getState() == type && StringUtils.isEmpty(val)){
            return ResultGenerator.genFailResult("循环OR注油开关的值不能为空,请检查");
        }
        //加油完成
        if (FuelState.FUEL_STATE_14.getState() == type){
            String token = request.getHeader("access_token");
            String userId = JwtUtil.getUserId(token);
            String realName = JwtUtil.getRealName(token);
            fuelRecordService.fuelComplete(Integer.valueOf(userId),realName);
        } else if (FuelState.FUEL_STATE_17.getState() == type || FuelState.FUEL_STATE_16.getState() == type){
            Object obj = null;
            if (FuelState.FUEL_STATE_16.getState() == type){
                obj = globalCache.get(FuelState.FUEL_STATE_16.getSwitchKey());
            }else if (FuelState.FUEL_STATE_17.getState() == type){
                obj = globalCache.get(FuelState.FUEL_STATE_17.getSwitchKey());
            }
            log.error("obj="+ obj);
            if (obj != null){
                if ("1".equals(val) && obj.equals(btn_on_flag[0])){
                    val = "0";
                }
            }
            log.error("FuelState.FUEL_STATE_17.getSwitchKey():"+globalCache.get(FuelState.FUEL_STATE_17.getSwitchKey()));
        }

        log.error("value="+ ("1".equals(val) ? btn_on_flag[0] : btn_on_flag[1]));
        String key = FuelState.getSwitchKey(type);

        if (FuelState.FUEL_STATE_11.getState() == type
                || FuelState.FUEL_STATE_12.getState() == type
                || FuelState.FUEL_STATE_13.getState() == type
                || FuelState.FUEL_STATE_14.getState() == type
                || FuelState.FUEL_STATE_15.getState() == type){
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
        }else {
            globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, key + btn_connect_flag + ("1".equals(val) ? btn_on_flag[0] : btn_on_flag[1]));
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
    public Result  scanstart(HttpServletRequest request,@RequestParam(required=true) String code){
        if (!NumberUtils.isNumber(code)){
            return ResultGenerator.genFailResult("扫码的订单ID不合法,订单ID必须全部为数字");
        }
        if (code.length() < 8 || code.length() >15){
            return ResultGenerator.genFailResult("扫码信息不正确,请重新扫码,订单ID长度必须是8-15位");
        }
        String content = fuelRecordService.getReadRemoteFileContent(code);
        if (StringUtils.isEmpty(content)){
            return ResultGenerator.genFailResult("系统中的订单不存在,请检查共享文件盘中文件是否存在,订单ID编号为:" + code);
        }
        this.scanIsEndMsg();
        String token = request.getHeader("access_token");
        String userId = JwtUtil.getUserId(token);
        fuelRecordService.scanStart(code,Integer.valueOf(userId));
        //设定注油目标值(系统设定的值)
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_53_NUM());
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
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 注油操作页面的初始化值
     * @return
     */
    @GetMapping("/initValue.json")
    public Result initValue(){
        List<String> list = new ArrayList<String>();
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_51.getState(),projectConfig.getSYSTEM_INIT_VAL_51_NUM()));
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_52.getState(),projectConfig.getSYSTEM_INIT_VAL_52_NUM()));
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_53.getState(),projectConfig.getSYSTEM_INIT_VAL_53_NUM()));
        list.add(String.format(INIT_VAL_FORMAT,FuelValue.FUEL_STATE_54.getState(),projectConfig.getSYSTEM_INIT_VAL_54_NUM()));

        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_51.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_51_NUM());
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_52.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_52_NUM());
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_53_NUM());
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_54.getSwitchKey() + btn_connect_flag + projectConfig.getSYSTEM_INIT_VAL_54_NUM());
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
        String content = SmbUtil.readOneFileString(projectConfig.getSMB_READ_REMOTE_HOST(),projectConfig.getSMB_READ_USERNAME(),projectConfig.getSMB_READ_PASSWORD(),projectConfig.getSMB_READ_SHARE_PATH(),removeDir,fileName);
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