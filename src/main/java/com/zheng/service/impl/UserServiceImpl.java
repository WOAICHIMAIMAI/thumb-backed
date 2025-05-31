package com.zheng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.constant.UserConstant;
import com.zheng.model.entity.User;
import com.zheng.service.UserService;
import com.zheng.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author ZhengJJ
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-05-31 17:04:09
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        return user;
    }
}




