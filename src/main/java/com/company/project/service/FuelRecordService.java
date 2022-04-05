package com.company.project.service;
import com.company.project.model.FuelRecord;
import com.company.project.core.Service;

import java.util.Date;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
public interface FuelRecordService extends Service<FuelRecord> {

    public String getScanCode();

    public Date getScanDate();


    /**
     * 点加油完成按钮操作
     * 如果返回值不为空 则操作失败 失败信息存储在返回值中
     * @return
     */
    public String fuelComplete(Integer userId);

    /**
     * 扫码加油开始 触发的事件
     * @param scanCode
     * @param operId
     *
     */
    public void scanStart(String scanCode,Integer operId);

    /**
     * 判断扫码加油是否结束 true:结束 false:没有结束
     * @return
     */
    public boolean scanIsEnd();
}
