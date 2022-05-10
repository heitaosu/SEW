package com.company.project.service;
import com.company.project.model.User;
import com.company.project.core.Service;

import java.util.List;


/**
 * Created by CodeGenerator on 2021/12/20.
 */
public interface UserService extends Service<User> {

    public User findByUserName(String  username);

    public List<User> findUser(Integer state);
}
