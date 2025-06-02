package com.zheng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.constant.ThumbConstant;
import com.zheng.manager.cache.CacheManager;
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
@Service("thumbServiceLocalCache")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService{

    private final UserService userService;

    private final TransactionTemplate transactionTemplate;

    private final BlogService blogService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final CacheManager cacheManager;

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
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, fieldKey, realThumbId);
                    cacheManager.putIfPresent(hashKey, fieldKey, realThumbId);
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
                 String hashKey =  ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                 String fieldKey = blogId.toString();
                 Object thumbIdObj = cacheManager.get(hashKey, fieldKey);
                 if(thumbIdObj == null || thumbIdObj.equals(ThumbConstant.UN_THUMB_CONSTANT)){
                     throw new RuntimeException("用户未点赞！");
                 }
                 Long thumbId = Long.valueOf(thumbIdObj.toString());
                 boolean remove = this.removeById(thumbId);
                 boolean update = blogService.lambdaUpdate()
                         .eq(Blog::getId, blogId)
                         .setSql("thumbCount = thumbCount - 1")
                         .update();
                 boolean success = remove && update;
                 if(success){
                     redisTemplate.opsForHash().delete( hashKey, fieldKey);
                     cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
                 }
                 return success;
             });
        }
        return true;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        if(thumbIdObj == null) return false;
        Long thumbId = (Long) thumbIdObj;
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }
}




