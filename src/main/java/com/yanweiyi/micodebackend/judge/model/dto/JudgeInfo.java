package com.yanweiyi.micodebackend.judge.model.dto;

import lombok.Data;

/**
 * @author yanweiyi
 */
@Data
public class JudgeInfo {

    /**
     * 判题结果
     */
    private String result;

    /**
     * 判题错误的消息
     */
    private String errorMessage;

    /**
     * 消耗时间（ms）
     */
    private Long timeUsed;

    /**
     * 消耗内存（kb）
     */
    private Long memoryUsed;

}
