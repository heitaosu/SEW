package com.company.project.web;

import com.company.project.core.Result;
import com.company.project.core.ResultGenerator;
import com.company.project.model.User;
import com.company.project.service.UserService;
import com.company.project.util.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by CodeGenerator on 2021/12/20.
*/
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 新增或者修改用户
     * @param user
     * @return
     */
    @PostMapping("/save.json")
    public Result save(User user) {
        if (user.getId() != null){
            User oldUser = userService.findById(user.getId());
            if (oldUser != null){
                if(0 == user.getState()){ //删除的情况
                    oldUser.setState(0);
                    userService.update(oldUser);
                }else{  //修改的情况
                    if (!oldUser.getUsername().equals(user.getUsername())){
                        return ResultGenerator.genFailResult("用户名不能修改");
                    }
                    if (StringUtils.isEmpty(user.getPassword())){
                        user.setPassword(oldUser.getPassword());
                    }
                    userService.update(user);
                }
            }else {
                return ResultGenerator.genFailResult("修改的用户不存在 id="+user.getId());
            }
        }else{ //新增的情况
            User oldUser = userService.findByUserName(user.getUsername());
            if (oldUser != null){
                return ResultGenerator.genFailResult("已经存在用户名为:"+user.getUsername()+"的用户,请重新填写");
            }
            user.setRegisterDate(System.currentTimeMillis());
            userService.save(user);
        }
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 查询用户列表
     * @param request
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list.json")
    public Result list(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        List<User> list = userService.findAll();
       /* Iterator<User> iterator = list.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getState() == 0) {
                iterator.remove();
            }
        }*/
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login.json")
    public Result login(@RequestParam(required=true) String username, @RequestParam(required=true) String password) {
        //身份验证是否成功
        User user = userService.findByUserName(username);
        if (user == null || user.getState() == 0 || !password.equals(user.getPassword())){
            return ResultGenerator.genFailResult("用户名或者密码不正确");
        }
        String token = JwtUtil.sign(username, user.getId()+"",user.getRealName());
        Map<String,Object> map = new HashMap<String,Object>();
        user.setPassword(null);
        map.put("user",user);
        map.put("access_token",token);
        return ResultGenerator.genSuccessResult(map);
    }
}
