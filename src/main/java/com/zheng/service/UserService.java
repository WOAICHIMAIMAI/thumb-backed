package com.zheng.service;

import com.zheng.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author ZhengJJ
* @description 针对表【user】的数据库操作Service
* @createDate 2025-05-31 17:04:09
*/
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);
}
