package com.yanweiyi.micodebackend.model.dto.question;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import lombok.Data;

import java.util.List;

/**
 * @author yanweiyi
 */
@Data
public class QuestionUpdateRequest {

    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签（Json 数组）
     */
    private List<String> tags;

    /**
     * 答案
     */
    private String answer;

    /**
     * 难度（简单 / 中等 / 困难）
     */
    private String difficulty;

    /**
     * 判题用例（Json 数组）
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

}
