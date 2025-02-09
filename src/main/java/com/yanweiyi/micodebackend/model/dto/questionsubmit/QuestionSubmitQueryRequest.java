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
     * 题目id
     */
    private Long questionId;

    /**
     * 提交者id
     */
    private Long userId;

}
