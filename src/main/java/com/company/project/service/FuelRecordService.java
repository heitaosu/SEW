package com.company.project.service;

import com.company.project.core.Service;
import com.company.project.core.ServiceException;
import com.company.project.model.FuelRecord;
import com.company.project.model.FuelRecordDO;

import java.util.Date;
import java.util.List;


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
    public String fuelComplete(Integer userId,String realName) throws ServiceException;

    /**
     * 扫码加油开始 触发的事件
     * @param scanCode
     * @param operId
     *
     */
    public void scanStart(String scanCode,Integer operId,FuelRecord fuelRecord) throws ServiceException;

    /**
     * 判断扫码加油是否结束 true:结束 false:没有结束
     * @return
     */
    public boolean scanIsEnd();


    public void scanEnd();

    /**
     * 获取远程文件的内容
     * @param code
     * @return
     */
    public String getReadRemoteFileContent(String code) throws ServiceException;

    public Integer getScanOperId();

    public List<FuelRecordDO> findByCreateTime(long createTimeStart, long createTimeEnd);

    public FuelRecord stringTransformBeanV2(String fileContent,String code) throws ServiceException;

    public boolean compareSeq(String currentSeq,String remoteSeq);

    public List<FuelRecordDO> findByWorkOrder(String workOrder);
}
