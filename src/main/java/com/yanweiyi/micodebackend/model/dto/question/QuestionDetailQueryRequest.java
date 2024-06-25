package com.yanweiyi.micodebackend.model.dto.question;

import com.yanweiyi.micodebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yanweiyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionDetailQueryRequest extends PageRequest {

    /**
     * 通用搜索词（搜索题目、编号或内容）
     */
    private String searchKey;

    /**
     * 标签（Json 数组）
     */
    private List<String> tags;

    /**
     * 难度（简单 / 中等 / 困难）
     */
    private String difficulty;

    /**
     * 判题结果
     */
    private String judgeInfoResult;

    /**
     * 判题状态
     */
    private Integer status;
}
