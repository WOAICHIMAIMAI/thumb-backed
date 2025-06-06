package com.zheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.model.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author ZhengJJ
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2025-05-31 17:00:12
* @Entity com/zheng.entity.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {

    void batchUpdateThumbCount(@Param("countMap")Map<Long, Long> countMap);

}




