package com.zheng.listener.thumb;

import com.google.common.collect.Sets;
import com.zheng.constant.ThumbConstant;
import com.zheng.listener.thumb.msg.ThumbEvent;
import com.zheng.model.entity.Thumb;
import com.zheng.service.ThumbService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThumbReconcileJob {  
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
  
    @Resource  
    private ThumbService thumbService;
  
    @Resource  
    private PulsarTemplate<ThumbEvent> pulsarTemplate;
  
    /**  
     * 定时任务入口（每天凌晨2点执行）  
     */  
    @Scheduled(cron = "0 0 2 * * ?")
    public void run() {  
        long startTime = System.currentTimeMillis();  
  
        // 1. 获取该分片下的所有用户ID  
        Set<Long> userIds = new HashSet<>();
        String pattern = ThumbConstant.USER_THUMB_KEY_PREFIX + "*";
        try (Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
            while (cursor.hasNext()) {  
                String key = cursor.next();  
                Long userId = Long.valueOf(key.replace(ThumbConstant.USER_THUMB_KEY_PREFIX, ""));  
                userIds.add(userId);  
            }  
        }  
  
        // 2. 逐用户比对  
        userIds.forEach(userId -> {  
            Set<Long> redisBlogIds = redisTemplate.opsForHash().keys(ThumbConstant.USER_THUMB_KEY_PREFIX + userId).stream().map(obj -> Long.valueOf(obj.toString())).collect(Collectors.toSet());
            //Optional.ofNullable() 方法用于判断给定的对象是否为空，如果为空则返回一个 Optional 对象，否则返回一个包含给定对象的 Optional 对象。
            Set<Long> mysqlBlogIds = Optional.ofNullable(thumbService.lambdaQuery()
                            .eq(Thumb::getUserId, userId)
                            .list()
                    //orElse 方法用于获取一个 Optional 对象的值，如果 Optional 对象为空则返回一个默认值，否则返回 Optional 对象的值。
                    ).orElse(new ArrayList<>())
                    .stream()  
                    .map(Thumb::getBlogId)  
                    .collect(Collectors.toSet());

            // 3. 计算差异（Redis有但MySQL无）
            // Sets.difference() 函数用于计算两个集合的差集
            Set<Long> diffBlogIds = Sets.difference(redisBlogIds, mysqlBlogIds);
  
            // 4. 发送补偿事件  
            sendCompensationEvents(userId, diffBlogIds);  
        });  
  
        log.info("对账任务完成，耗时 {}ms", System.currentTimeMillis() - startTime);  
    }  
  
    /**  
     * 发送补偿事件到Pulsar  
     */  
    private void sendCompensationEvents(Long userId, Set<Long> blogIds) {  
        blogIds.forEach(blogId -> {  
            ThumbEvent thumbEvent = new ThumbEvent(userId, blogId, ThumbEvent.EventType.INCR, LocalDateTime.now());
            pulsarTemplate.sendAsync("thumb-topic", thumbEvent)  
                    .exceptionally(ex -> {  
                        log.error("补偿事件发送失败: userId={}, blogId={}", userId, blogId, ex);  
                        return null;  
                    });  
        });  
    }  
}
