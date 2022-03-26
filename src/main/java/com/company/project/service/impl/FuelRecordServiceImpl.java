package com.company.project.service.impl;

import com.company.project.dao.FuelRecordMapper;
import com.company.project.model.FuelRecord;
import com.company.project.service.FuelRecordService;
import com.company.project.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * Created by CodeGenerator on 2022/03/21.
 */
@Service
@Transactional
public class FuelRecordServiceImpl extends AbstractService<FuelRecord> implements FuelRecordService {
    @Resource
    private FuelRecordMapper fuelRecordMapper;

}
