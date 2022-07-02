package com.company.project.dao;

import com.company.project.core.Mapper;
import com.company.project.model.WarnRecord;

import java.util.List;

public interface WarnRecordMapper extends Mapper<WarnRecord> {

    public List<WarnRecord> findByCreateTime(long createTimeStart, long createTimeEnd);
}