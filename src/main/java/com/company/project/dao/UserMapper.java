package com.company.project.dao;

import com.company.project.core.Mapper;
import com.company.project.model.User;

import java.util.List;

public interface UserMapper extends Mapper<User> {

    public User findByUserName(String username);

    public List<User> findUser(Integer state);
}