package com.zheng.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.constant.ThumbConstant;
import com.zheng.constant.UserConstant;
import com.zheng.mapper.BlogMapper;
import com.zheng.model.entity.Blog;
import com.zheng.model.entity.Thumb;
import com.zheng.model.entity.User;
import com.zheng.model.vo.BlogVO;
import com.zheng.service.BlogService;
import com.zheng.service.ThumbService;
import com.zheng.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2025-05-31 17:00:12
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if(ObjUtil.isNotEmpty(loginUser)){
            List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogIdList);
            for(int i = 0; i < thumbList.size(); i++){
                if(thumbList.get(i) == null) continue;
                blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
            }

        };
        return blogList.stream().map(blog -> {
            BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
            blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
            return blogVO;
        }).toList();
    }

    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);
        if(loginUser == null) return blogVO;

        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getBlogId, blog.getId())
                .eq(Thumb::getUserId, loginUser.getId())
                .one();
        Boolean exist = thumbService.hasThumb(thumb.getBlogId(), loginUser.getId());
        blogVO.setHasThumb(exist);

        return blogVO;
    }
}




