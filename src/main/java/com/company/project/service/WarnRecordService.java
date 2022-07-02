package com.company.project.service;

import com.company.project.core.Service;
import com.company.project.model.WarnRecord;

import java.util.List;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
public interface WarnRecordService extends Service<WarnRecord> {

    public List<WarnRecord> findByCreateTime(long createTimeStart, long createTimeEnd);
}
