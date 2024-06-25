package com.yanweiyi.micodebackend.model.vo;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yanweiyi
 */
@Data
public class QuestionVO {
    /**
     * 题目编号
     */
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
     * 标签（Json）
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
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 提交通过数
     */
    private Integer acceptedNum;

    /**
     * 判题用例（json 对象）
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 上传者的用户编号
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

}
