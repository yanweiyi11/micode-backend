package com.yanweiyi.micodebackend.model.dto.questionsubmit;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import lombok.Data;

/**
 * @author yanweiyi
 */
@Data
public class QuestionSubmitAddRequest {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题状态
     */
    private Integer status;

    /**
     * 判题信息（Json 对象）
     */
    private JudgeConfig judgeInfo;

    /**
     * 题目id
     */
    private Long questionId;

}
