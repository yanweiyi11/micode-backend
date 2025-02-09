package com.yanweiyi.micodebackend.model.dto.questionsubmit;

import lombok.Data;

/**
 * @author yanweiyi
 */
@Data
public class QuestionSubmitDoJudgeRequest {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目id
     */
    private Long questionId;

}
