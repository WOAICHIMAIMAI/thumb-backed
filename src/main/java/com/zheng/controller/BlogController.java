package com.zheng.controller;

import com.zheng.common.BaseResponse;
import com.zheng.common.ResultUtils;
import com.zheng.constant.UserConstant;
import com.zheng.model.entity.User;
import com.zheng.model.vo.BlogVO;
import com.zheng.service.BlogService;
import com.zheng.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request){
        BlogVO blogVO = blogService.getBlogVOById(blogId, request);
        return ResultUtils.success(blogVO);
    }

    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request){
        List<BlogVO> blogVOS = blogService.getBlogVOList(blogService.list(), request);
        return  ResultUtils.success(blogVOS);
    }
}
