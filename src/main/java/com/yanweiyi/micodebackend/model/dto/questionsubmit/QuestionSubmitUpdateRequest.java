package com.yanweiyi.micodebackend.model.dto.questionsubmit;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import lombok.Data;

/**
 * @author yanweiyi
 */
@Data
public class QuestionSubmitUpdateRequest {
    /**
     * 提交编号
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
     * 判题信息（Json 对象）
     */
    private JudgeInfo judgeInfo;
}
