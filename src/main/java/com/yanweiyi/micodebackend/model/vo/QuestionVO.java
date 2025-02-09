package com.yanweiyi.micodebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.model.entity.Question;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yanweiyi
 */
@Data
public class QuestionVO {

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
     * 上传者id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    public void copyByQuestion(Question question) {
        this.setId(question.getId());
        this.setTitle(question.getTitle());
        this.setContent(question.getContent());
        this.setTags(JSONUtil.toList(question.getTags(), String.class));
        this.setAnswer(question.getAnswer());
        this.setDifficulty(question.getDifficulty());
        this.setSubmitNum(question.getSubmitNum());
        this.setAcceptedNum(question.getAcceptedNum());
        this.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
        this.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        this.setUserId(question.getUserId());
        this.setCreateTime(question.getCreateTime());
    }
}
