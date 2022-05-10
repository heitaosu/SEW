package com.company.project.task;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.StringUtils;
import com.company.project.configurer.ProjectConfig;
import com.company.project.core.ProjectConstant;
import com.company.project.model.FuelRecordDO;
import com.company.project.service.FuelRecordService;
import com.company.project.util.DateUtil;
import com.company.project.util.FileUtil;
import com.company.project.util.SmbUtil;
import com.company.project.websocket.PushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SewTask {

    @Resource
    private FuelRecordService fuelRecordService;

    @Resource
    private PushService pushService;

    @Resource
    private ProjectConfig projectConfig;

    private final String ONE_BLANK_STR = " ";
    private final String EXCEL_SUF_STR = ".xlsx";
    private final String EXCEL_PRE_STR = "注油记录";

    /**
     * 每隔5分钟执行一次 fixedDelay = 1000*60*5
     * 监听正在扫码加油的时长,如果超过30分钟操作员还没有点击扫码加油完成弯扭则系统自动结束
     */
    @Scheduled(fixedDelay = 300000)
    public void initVal(){
        log.error("监听正在扫码加油的时长,如果超过30分钟操作员还没有点击扫码加油完成弯扭则系统自动结束 start");
        Date scanDate = fuelRecordService.getScanDate();
        //如果扫码时间为null直接返回
        if (scanDate == null){
            log.error("当前没有正在执行中的扫码加油");
            return;
        }
        Date nowDate = new Date(System.currentTimeMillis());
        int minutes = DateUtil.getIntervalMinutes(scanDate,nowDate);
        if (minutes >= 30){
            fuelRecordService.fuelComplete(fuelRecordService.getScanOperId(),"超时系统自动完成");
            pushService.pushMsgToAll(String.format(ProjectConstant.VAL_ARRAY[3], "1","扫码加油在规定的时间内没有点击扫码完成按钮,系统自动完成,请确认"));
        }
        log.error("监听正在扫码加油的时长,如果超过30分钟操作员还没有点击扫码加油完成弯扭则系统自动结束 start");
    }

    /**
     * 每天的凌晨 4:35执行一次 cron = "0 35 4 * * ?"
     * 生成扫码加油的记录Excel
     */
    @Scheduled(cron = "0 35 4 * * ?")
    public void writeExcel() throws Exception {
        log.error("生成扫码加油的记录Excel start");
        Date yesterday = DateUtil.addDay(new Date(),-1);
        int mouth = DateUtil.getMonth(yesterday);
        //本月的第一天
        Date currentMouthDay =  DateUtil.StringToDate(DateUtil.getFirstDayOfMonth(mouth), DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS);
        //下个月的第一天
        Date nextMouthDay =  DateUtil.StringToDate(DateUtil.getFirstDayOfMonth(mouth+1), DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS);
        List<FuelRecordDO> data =  fuelRecordService.findByCreateTime(currentMouthDay.getTime(),nextMouthDay.getTime());
        if (CollectionUtils.isEmpty(data)){
            log.error("生成扫码加油的记录Excel 当前没有数据 返回");
            return;
        }
        for (FuelRecordDO fr :data){
            if (StringUtils.isEmpty(fr.getCreateTime()) || StringUtils.isEmpty(fr.getFuelEnd()) || StringUtils.isEmpty(fr.getFuelStart())){
                continue;
            }
            String endTime = fr.getCreateTime() + ONE_BLANK_STR + fr.getFuelEnd();
            String startTime = fr.getCreateTime() + ONE_BLANK_STR + fr.getFuelStart();
            String fuelTime = DateUtil.getIntervaHMS(DateUtil.StringToDate(endTime,DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS),DateUtil.StringToDate(startTime,DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS));
            fr.setFuelTime(fuelTime);
        }
        String filePath = projectConfig.getSYSTEM_SEW_WRITE_FILE_DIR() + projectConfig.getSYSTEM_SEW_DOMAIN_NAME() + File.separator;
        String fileName = EXCEL_PRE_STR + DateUtil.DateToString(currentMouthDay,DateUtil.DateStyle.YYYYMM) + EXCEL_SUF_STR;
        log.error("filePath="+filePath + ",fileName=" +fileName);
        // 防止文件夹不存在 先创建
        FileUtil.createDir(filePath);
        EasyExcel.write(filePath + fileName, FuelRecordDO.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(EXCEL_PRE_STR)
                .doWrite(data);
        // 上传文件到共享盘
        SmbUtil.uploadFile(projectConfig.getSMB_WRITE_REMOTE_HOST(), projectConfig.getSMB_WRITE_USERNAME(),projectConfig.getSMB_WRITE_PASSWORD(),projectConfig.getSMB_WRITE_SHARE_PATH(), projectConfig.getSMB_WRITE_SHARE_BASEDIR() + projectConfig.getSYSTEM_SEW_DOMAIN_NAME(),filePath+fileName);
        log.error("生成扫码加油的记录Excel end");
    }

    /**
     * 每天的凌晨 4:10执行一次 cron = "0 10 4 * * ?"
     * 定时清理本地临时生成的制作铭牌信息的文件
     */
    @Scheduled(cron = "0 5 4 * * ?")
    public void removeLocalFile() throws IOException {
        log.error("定时清理本地临时生成的制作铭牌信息的文件 start");
        File file = new File(projectConfig.getSYSTEM_SEW_WRITE_FILE_DIR());
        FileUtils.deleteDirectory(file);
        log.error("定时清理本地临时生成的制作铭牌信息的文件 end");
    }
}
