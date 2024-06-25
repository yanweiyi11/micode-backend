package com.yanweiyi.micodebackend.model.entity;

import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.model.vo.QuestionDetailVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanweiyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionDetail extends QuestionDetailVO {

    /**
     * 标签（Json）
     */
    private String tagsStr;

    /**
     * 判题配置（json 对象）
     */
    private String judgeConfigStr;

    /**
     * 判题信息
     */
    private String judgeInfoStr;

    public static QuestionDetailVO toQuestionDetailVO(QuestionDetail questionDetail) {
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtils.copyProperties(questionDetail, questionDetailVO);
        String tagsStr = questionDetail.getTagsStr();
        if (StringUtils.isNotBlank(tagsStr)) {
            questionDetailVO.setTags(JSONUtil.toList(tagsStr, String.class));
        }
        String judgeConfigStr = questionDetail.getJudgeConfigStr();
        if (StringUtils.isNotBlank(judgeConfigStr)) {
            questionDetailVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        }
        String judgeInfoStr = questionDetail.getJudgeInfoStr();
        if (StringUtils.isNotBlank(judgeInfoStr)) {
            questionDetailVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));
        }
        return questionDetailVO;
    }

    public static List<QuestionDetailVO> toQuestionDetailVOList(List<QuestionDetail> questionDetailList) {
        List<QuestionDetailVO> questionDetailVOList = new ArrayList<>();
        for (QuestionDetail questionDetail : questionDetailList) {
            questionDetailVOList.add(toQuestionDetailVO(questionDetail));
        }
        return questionDetailVOList;
    }
}
