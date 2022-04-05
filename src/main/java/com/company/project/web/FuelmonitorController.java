package com.company.project.web;

import com.alibaba.excel.util.StringUtils;
import com.company.project.core.*;
import com.company.project.model.FuelState;
import com.company.project.model.FuelValue;
import com.company.project.service.FuelRecordService;
import com.company.project.util.JwtUtil;
import com.company.project.util.SmbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 操控页面的请求
 */
@RestController
@RequestMapping("/monitor")
public class FuelmonitorController {
    @Autowired
    private IGlobalCache globalCache;

    @Resource
    private FuelRecordService fuelRecordService;
    //
    private String[] btn_on_flag = new String[]{"True","False"};
    private String btn_connect_flag = ":";

    /**
     * 按钮操作
     * @param type
     * @return
     */
    @PostMapping("/btn.json")
    public Result btn(HttpServletRequest request,@RequestParam(required=true) Integer type,String value) throws IOException {
        // 扫码加油如未结束不允许手动加油或停止
        if (FuelState.FUEL_STATE_11.getState() == type || FuelState.FUEL_STATE_12.getState() == type){
            this.scanIsEndMsg();
        }
        if (FuelState.FUEL_STATE_16.getState() == type && StringUtils.isEmpty(value)){
            return ResultGenerator.genFailResult("循环OR注油开关的值不能为空,请检查");
        }
        if (StringUtils.isEmpty(value)){
            value = btn_on_flag[0];
        }else {
            value = "1".equals(value) ? btn_on_flag[0] : btn_on_flag[1];
        }
        //加油完成
        if (FuelState.FUEL_STATE_14.getState() == type){
            String token = request.getHeader("access_token");
            String userId = JwtUtil.getUserId(token);
            fuelRecordService.fuelComplete(Integer.valueOf(userId));
        }
        String key = FuelState.getSwitchKey(type);
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, key + btn_connect_flag + value);
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
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL,key + ":" + value);
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 扫码加油开始
     * @param code
     * @return
     */
    @PostMapping("/scanstart.json")
    public Result  scanstart(HttpServletRequest request,@RequestParam(required=true) String code){
        if (code.length() < 8 || code.length() >10){
            throw new ServiceException("扫码信息不正确,请重新扫码");
        }
        // 暂时关闭测试
        // this.scanIsEndMsg();
        String token = request.getHeader("access_token");
        String userId = JwtUtil.getUserId(token);
        fuelRecordService.scanStart(code,Integer.valueOf(userId));
        //设定注油目标值(系统设定的值)
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL, FuelValue.FUEL_STATE_53.getSwitchKey() + ":" + ProjectConfig.SYSTEM_OIL_INIT_VALUE);
        //扫码加油开始按钮打开
        globalCache.publish(ProjectConstant.BTN_EVENT_CHANNEL,FuelState.FUEL_STATE_13.getSwitchKey() + ":" + btn_on_flag[0]);
        System.out.println("code="+code);
        return ResultGenerator.genSuccessResult();
    }


    /**
     * 测试读取共享文件
     * @param request
     * @param fileName
     * @return
     */
    @GetMapping("/testReadRemote.json")
    public Result  testReadRemote(HttpServletRequest request,@RequestParam(required=true) String fileName){
        System.out.println(SmbUtil.SMB_REMOTE_HOST+":"+SmbUtil.SMB_USERNAME+":"+SmbUtil.SMB_PASSWORD+":"+SmbUtil.SMB_SHARE_PATH);
        SmbUtil.listFile(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH,"E25");
        String content = SmbUtil.readOneFileString(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH,ProjectConfig.SYSTEM_SHARE_REMOTE_DIR,fileName);
        return ResultGenerator.genSuccessResult(content);
    }

    /**
     * 测试写文件到共享文件
     * @param request
     * @param removeDir   拼在远程共享目录的下一级目录下面
     * @param fileAllName  本地文件全限地址
     * @return
     */
    @GetMapping("/testWriteRemote.json")
    public Result  testWriteRemote(HttpServletRequest request,String removeDir, @RequestParam(required=true) String fileAllName){

        SmbUtil.uploadFile(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH,("D:\\" + removeDir),fileAllName);
        return ResultGenerator.genSuccessResult();
    }

    public void scanIsEndMsg() throws ServiceException {
        if (!fuelRecordService.scanIsEnd()){
            throw new ServiceException("本次扫码注油还未结束,不允许此操作,请等待当前注油结束,如已结束请按扫码加油结束按钮结束本次注油");
        }
    }
}