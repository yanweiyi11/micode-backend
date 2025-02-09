package com.yanweiyi.micodebackend.model.dto.questionsubmit;

import com.yanweiyi.micodebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yanweiyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSubmitDetailQueryRequest extends PageRequest {
    /**
     * 通用搜索词（搜索题目编号、题目名和用户名）
     */
    private String searchKey;

    /**
     * 选择的编程语言列表
     */
    private List<String> languageList;

    /**
     * 判题结果
     */
    private String judgeInfoResult;

    /**
     * 判题状态
     */
    private Integer status;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交者id
     */
    private Long userId;
}
