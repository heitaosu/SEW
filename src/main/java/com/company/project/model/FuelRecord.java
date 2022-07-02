package com.company.project.model;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "fuel_record")
public class FuelRecord {
    @ExcelProperty("编号")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 工单号
     */
    @ExcelProperty("订单id")
    @Column(name = "work_order")
    private Long workOrder;

    /**
     * 加油结束时间
     */
    @Column(name = "fuel_end")
    private Long fuelEnd;

    /**
     * 加油开始时间
     */
    @Column(name = "fuel_start")
    private Long fuelStart;

    /**
     * 序列号
     */
    @Column(name = "sequence_code")
    private String sequenceCode;

    /**
     * 型号
     */
    @Column(name = "model_type")
    private String modelType;

    /**
     * 安装方式
     */
    @Column(name = "install_type")
    private String installType;

    /**
     * 油品型号
     */
    @Column(name = "oil_type")
    private String oilType;

    /**
     * 注油量设定值
     */
    @Column(name = "fuel_set_val")
    private Double fuelSetVal;

    /**
     * 注油量实际值
     */
    @Column(name = "fuel_real_val")
    private Double fuelRealVal;

    /**
     * 上传到铭牌的注油量
     */
    @Column(name = "tag_real_val")
    private Double tagRealVal;

    /**
     * 操作员用户id
     */
    @Column(name = "oper_id")
    private Integer operId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Long createTime;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取工单号
     *
     * @return work_order - 工单号
     */
    public Long getWorkOrder() {
        return workOrder;
    }

    /**
     * 设置工单号
     *
     * @param workOrder 工单号
     */
    public void setWorkOrder(Long workOrder) {
        this.workOrder = workOrder;
    }

    /**
     * 获取加油结束时间
     *
     * @return fuel_end - 加油结束时间
     */
    public Long getFuelEnd() {
        return fuelEnd;
    }

    /**
     * 设置加油结束时间
     *
     * @param fuelEnd 加油结束时间
     */
    public void setFuelEnd(Long fuelEnd) {
        this.fuelEnd = fuelEnd;
    }

    /**
     * 获取加油开始时间
     *
     * @return fuel_start - 加油开始时间
     */
    public Long getFuelStart() {
        return fuelStart;
    }

    /**
     * 设置加油开始时间
     *
     * @param fuelStart 加油开始时间
     */
    public void setFuelStart(Long fuelStart) {
        this.fuelStart = fuelStart;
    }

    /**
     * 获取序列号
     *
     * @return sequence_code - 序列号
     */
    public String getSequenceCode() {
        return sequenceCode;
    }

    /**
     * 设置序列号
     *
     * @param sequenceCode 序列号
     */
    public void setSequenceCode(String sequenceCode) {
        this.sequenceCode = sequenceCode;
    }

    /**
     * 获取型号
     *
     * @return model_type - 型号
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * 设置型号
     *
     * @param modelType 型号
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * 获取安装方式
     *
     * @return install_type - 安装方式
     */
    public String getInstallType() {
        return installType;
    }

    /**
     * 设置安装方式
     *
     * @param installType 安装方式
     */
    public void setInstallType(String installType) {
        this.installType = installType;
    }

    /**
     * 获取注油量设定值
     *
     * @return fuel_set_val - 注油量设定值
     */
    public Double getFuelSetVal() {
        return fuelSetVal;
    }

    /**
     * 设置注油量设定值
     *
     * @param fuelSetVal 注油量设定值
     */
    public void setFuelSetVal(Double fuelSetVal) {
        this.fuelSetVal = fuelSetVal;
    }

    /**
     * 获取注油量实际值
     *
     * @return fuel_real_val - 注油量实际值
     */
    public Double getFuelRealVal() {
        return fuelRealVal;
    }

    /**
     * 设置注油量实际值
     *
     * @param fuelRealVal 注油量实际值
     */
    public void setFuelRealVal(Double fuelRealVal) {
        this.fuelRealVal = fuelRealVal;
    }

    /**
     * 获取上传到铭牌的注油量
     *
     * @return tag_real_val - 上传到铭牌的注油量
     */
    public Double getTagRealVal() {
        return tagRealVal;
    }

    /**
     * 设置上传到铭牌的注油量
     *
     * @param tagRealVal 上传到铭牌的注油量
     */
    public void setTagRealVal(Double tagRealVal) {
        this.tagRealVal = tagRealVal;
    }

    /**
     * 获取操作员用户id
     *
     * @return oper_id - 操作员用户id
     */
    public Integer getOperId() {
        return operId;
    }

    /**
     * 设置操作员用户id
     *
     * @param operId 操作员用户id
     */
    public void setOperId(Integer operId) {
        this.operId = operId;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Long getCreateTime() {
        return createTime;
    }


    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getOilType() {
        return oilType;
    }

    public void setOilType(String oilType) {
        this.oilType = oilType;
    }

}