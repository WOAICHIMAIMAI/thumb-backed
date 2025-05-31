package com.zheng.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.model.entity.Blog;
import com.zheng.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-05-31 17:00:12
*/
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);

}
