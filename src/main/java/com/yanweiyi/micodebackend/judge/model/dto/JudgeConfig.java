package com.yanweiyi.micodebackend.judge.model.dto;

import lombok.Data;

/**
 * 判题配置
 *
 * @author yanweiyi
 */
@Data
public class JudgeConfig {

    /**
     * 时间限制（ms）
     */
    private Long timeLimit;

    /**
     * 内存限制（kb）
     */
    private Long memoryLimit;

}
