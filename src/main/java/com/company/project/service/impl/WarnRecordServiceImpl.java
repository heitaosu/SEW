package com.company.project.service.impl;

import com.company.project.core.AbstractService;
import com.company.project.dao.WarnRecordMapper;
import com.company.project.model.WarnRecord;
import com.company.project.service.WarnRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
@Slf4j
@Service
@Transactional
public class WarnRecordServiceImpl extends AbstractService<WarnRecord> implements WarnRecordService {

    @Resource
    private WarnRecordMapper warnRecordMapper;

    @Override
    public List<WarnRecord> findByCreateTime(long createTimeStart, long createTimeEnd) {
        return warnRecordMapper.findByCreateTime(createTimeStart,createTimeEnd);
    }
}
