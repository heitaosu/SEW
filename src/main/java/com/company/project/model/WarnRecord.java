package com.company.project.model;


import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "warn_record")
public class WarnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 告警类型
     */
    @Column(name = "warn_type")
    private Integer warnType;

    /**
     * 告警时间
     */
    @Column(name = "create_time")
    private Long createTime;

    /**
     * 告警内容
     */
    @Column(name = "warn_msg")
    private String warnMsg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWarnType() {
        return warnType;
    }

    public void setWarnType(Integer warnType) {
        this.warnType = warnType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getWarnMsg() {
        return warnMsg;
    }

    public void setWarnMsg(String warnMsg) {
        this.warnMsg = warnMsg;
    }
}