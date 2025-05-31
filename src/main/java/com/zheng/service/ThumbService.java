package com.zheng.service;

import com.zheng.model.dto.thumb.DoThumbRequest;
import com.zheng.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author ZhengJJ
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-05-31 17:02:22
*/
public interface ThumbService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean unDoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

}
