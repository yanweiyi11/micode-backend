package com.yanweiyi.micodebackend.model.dto.questionsubmit;

import com.yanweiyi.micodebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yanweiyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSubmitQueryRequest extends PageRequest {
    /**
     * 题目提交编号
     */
    private Long id;

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
     * 判题结果
     */
    private String judgeInfoResult;

    /**
     * 题目编号
     */
    private Long questionId;

    /**
     * 提交者的用户编号
     */
    private Long userId;

}
