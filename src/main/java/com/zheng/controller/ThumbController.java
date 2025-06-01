package com.zheng.controller;

import com.zheng.common.BaseResponse;
import com.zheng.common.ResultUtils;
import com.zheng.model.dto.thumb.DoThumbRequest;
import com.zheng.model.vo.BlogVO;
import com.zheng.service.BlogService;
import com.zheng.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request){
        Boolean success = thumbService.doThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> unDoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request){
        Boolean success = thumbService.undoThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }


}
