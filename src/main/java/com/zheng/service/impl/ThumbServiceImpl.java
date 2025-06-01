package com.zheng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.constant.ThumbConstant;
import com.zheng.model.dto.thumb.DoThumbRequest;
import com.zheng.model.entity.Blog;
import com.zheng.model.entity.Thumb;
import com.zheng.model.entity.User;
import com.zheng.service.BlogService;
import com.zheng.service.ThumbService;
import com.zheng.mapper.ThumbMapper;
import com.zheng.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author ZhengJJ
* @description 针对表【thumb】的数据库操作Service实现
* @createDate 2025-05-31 17:02:22
*/
@Service("thumbServiceDB")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService{

    private final UserService userService;

    private final TransactionTemplate transactionTemplate;

    private final BlogService blogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null){
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        synchronized (loginUser.getId().toString().intern()){
            transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                boolean exists = this.hasThumb(blogId, loginUser.getId());
                if(exists){
                    throw new RuntimeException("用户已点赞！");
                }
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean save = this.save(thumb);
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();
                boolean success = update && save;
                if(success){
                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString(), thumb.getId());
                }
                return success;
            });
        }
        return true;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest == null ||  doThumbRequest.getBlogId() == null){
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        synchronized (loginUser.getId().toString().intern()){
             transactionTemplate.execute(status -> {
                 Long blogId = doThumbRequest.getBlogId();
                 Object thumbObject = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                 if(thumbObject == null){
                     throw new RuntimeException("用户未点赞！");
                 }
                 Long thumbId = Long.valueOf(thumbObject.toString());
                 boolean remove = this.removeById(thumbId);
                 boolean update = blogService.lambdaUpdate()
                         .eq(Blog::getId, blogId)
                         .setSql("thumbCount = thumbCount - 1")
                         .update();
                 boolean success = remove && update;
                 if(success){
                     redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                 }
                 return success;
             });
        }
        return true;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
    }
}




