package com.yanweiyi.micodebackend.model.vo;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import lombok.Data;

import java.util.Date;

/**
 * @author yanweiyi
 */
@Data
public class QuestionSubmitVO {

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

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交者id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;
}