package com.company.project.service.impl;

import com.company.project.core.ServiceException;
import com.company.project.dao.UserMapper;
import com.company.project.model.User;
import com.company.project.service.UserService;
import com.company.project.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * Created by CodeGenerator on 2021/12/20.
 */
@Service
@Transactional
public class UserServiceImpl extends AbstractService<User> implements UserService {
    @Resource
    private UserMapper userMapper;

    public void deleteById(Integer id){
        throw new ServiceException("该手机号已被注册");
    }

    @Override
    public User findByUserName(String username) {
        User user = userMapper.findByUserName(username);
        return user;
    }

    @Override
    public List<User> findUser(Integer state) {
        return userMapper.findUser(state);
    }
}
