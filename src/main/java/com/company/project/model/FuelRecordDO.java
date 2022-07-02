package com.company.project.model;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FuelRecordDO {

    @ExcelProperty("记录ID")
    private Integer id;
    /**
     * 日期
     */
    @ExcelProperty("日期")
    private String createTime;

    /**
     * 起始时间
     */
    @ExcelProperty("起始时间")
    private String fuelStart;

    /**
     * 完成时间
     */
    @ExcelProperty("完成时间")
    private String fuelEnd;

    /**
     * 注油时间(完成时间-起始时间)
     */
    @ExcelProperty("注油时间")
    private String fuelTime;

    /**
     * 工单号
     */
    @ExcelProperty("工单")
    private String workOrder;

    /**
     * 序列号
     */
    @ExcelProperty("序列号")
    private String sequenceCode;

    /**
     * 型号
     */
    @ExcelProperty("型号")
    private String modelType;

    /**
     * 油品型号
     */
    @ExcelProperty("油品型号")
    private String oilType;

    /**
     * 安装方式
     */
    @ExcelProperty("安装方式")
    private String installType;

    /**
     * 注油量设定值
     */
    @ExcelProperty("注油量(设定值)")
    private String fuelSetVal;

    /**
     * 注油量实际值
     */
    @ExcelProperty("注油量(实际值)")
    private String fuelRealVal;

    /**
     * 上传到铭牌的注油量
     */
    @ExcelProperty("铭牌上传油量")
    private String tagRealVal;

    /**
     * 操作员真实姓名
     */
    @ExcelProperty("操作员")
    private String operName;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFuelStart() {
        return fuelStart;
    }

    public void setFuelStart(String fuelStart) {
        this.fuelStart = fuelStart;
    }

    public String getFuelEnd() {
        return fuelEnd;
    }

    public void setFuelEnd(String fuelEnd) {
        this.fuelEnd = fuelEnd;
    }

    public String getFuelTime() {
        return fuelTime;
    }

    public void setFuelTime(String fuelTime) {
        this.fuelTime = fuelTime;
    }

    public String getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(String workOrder) {
        this.workOrder = workOrder;
    }

    public String getSequenceCode() {
        return sequenceCode;
    }

    public void setSequenceCode(String sequenceCode) {
        this.sequenceCode = sequenceCode;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getInstallType() {
        return installType;
    }

    public void setInstallType(String installType) {
        this.installType = installType;
    }

    public String getFuelSetVal() {
        return fuelSetVal;
    }

    public void setFuelSetVal(String fuelSetVal) {
        this.fuelSetVal = fuelSetVal;
    }

    public String getFuelRealVal() {
        return fuelRealVal;
    }

    public void setFuelRealVal(String fuelRealVal) {
        this.fuelRealVal = fuelRealVal;
    }

    public String getTagRealVal() {
        return tagRealVal;
    }

    public void setTagRealVal(String tagRealVal) {
        this.tagRealVal = tagRealVal;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getOilType() {
        return oilType;
    }

    public void setOilType(String oilType) {
        this.oilType = oilType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}