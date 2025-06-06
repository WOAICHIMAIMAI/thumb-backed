package com.zheng.enums;

import lombok.Getter;

@Getter
public enum ThumbTypeEnum {
    //点赞
    INCR(1),
    //取消点赞
    DECR(2),
    //不发生改变
    NON(0);

    private final int value;
    ThumbTypeEnum(int value) {
        this.value = value;
    }
}
