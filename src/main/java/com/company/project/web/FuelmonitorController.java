package com.company.project.web;

import com.alibaba.excel.util.FileUtils;
import com.company.project.core.IGlobalCache;
import com.company.project.core.Result;
import com.company.project.core.ResultGenerator;
import com.company.project.model.FuelRecord;
import com.company.project.model.FuelState;
import com.company.project.service.FuelRecordService;
import com.company.project.util.DateUtil;
import com.company.project.util.FileUtil;
import com.company.project.util.SmbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

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

    // button key值的结束符 1 表示按下按钮
    private String btn_on_flag = ":1";

    /**
     * 按钮操作
     * @param type
     * @return
     */
    @PostMapping("/btn.json")
    public Result btn(Integer type) {
        String key = FuelState.getSwitchKey(type);
        globalCache.publish("btn.event.channel", key + btn_on_flag);
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 改变设置属性值
     * @param type
     * @param value
     * @return
     */
    @PostMapping("/setValue.json")
    public Result set(Integer type, String value) {
        String key = FuelState.getSwitchKey(type);
        globalCache.set(key,value);
        return ResultGenerator.genSuccessResult();
    }

    /**
     *  获取设备显示值(考虑是否用队列)
     * @param type
     * @return
     */
    @GetMapping(value= "/getValue.json")
    public Result get(Integer type) {
        String key = FuelState.getSwitchKey(type);
        String value = (String)globalCache.get(key);
        System.out.println("key=" + key+",value=" + value);
        return ResultGenerator.genSuccessResult("key=" + key+",value=" + value);
    }

    /**
     * 加油完成后将数据归档
     * @param fuelRecord
     * @return
     */
    @PostMapping("/fuelComplete.json")
    public Result fuelComplete(FuelRecord fuelRecord) throws IOException {
        Long workOrder = fuelRecord.getWorkOrder();
        String yyyyMM = DateUtil.DateToString(new Date(),DateUtil.DateStyle.YYYYMM);
        String localDownload = "D:\\test\\" + yyyyMM;
        SmbUtil.downloadFile(SmbUtil.SMB_REMOTE_HOST,SmbUtil.SMB_USERNAME,SmbUtil.SMB_PASSWORD,SmbUtil.SMB_SHARE_PATH+yyyyMM,workOrder+".001",localDownload);

        File file = new File(localDownload);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        String str = new String(bytes);
        str = str.replaceAll( "\\s+", " ");
        str = str.replace(" ","_");
        String[] array = str.split("_");
        fuelRecord.setInstallType(array[15]);
        fuelRecord.setModelType(array[1]);
        fuelRecord.setSequenceCode(array[3]);

        fuelRecordService.save(fuelRecord);

        System.out.println("型号="+array[1]+",序列号="+array[3]+",安装方式="+array[15]);

        String fileContent = fuelRecord.getFuelRealVal() + " " + fuelRecord.getFuelStart() + " " + fuelRecord.getFuelEnd();

        FileUtil.writeContent("D:\\",yyyyMM+".001",fileContent);
        //SmbUtil.uploadFile();
        return ResultGenerator.genSuccessResult();
    }
}