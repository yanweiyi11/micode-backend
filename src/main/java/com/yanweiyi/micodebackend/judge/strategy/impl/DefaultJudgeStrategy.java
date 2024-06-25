package com.yanweiyi.micodebackend.judge.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.model.enums.JudgeInfoMessageEnum;
import com.yanweiyi.micodebackend.judge.strategy.JudgeStrategy;
import com.yanweiyi.micodebackend.judge.strategy.model.JudgeContext;
import com.yanweiyi.micodebackend.model.entity.Question;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * 默认判题策略（Java）
 *
 * @author yanweiyi
 */
public class DefaultJudgeStrategy implements JudgeStrategy {

    @Override
    public JudgeInfo applyJudgeStrategy(JudgeContext judgeContext) {
        List<String> expectedOutputList = judgeContext.getExpectedOutputList();
        List<String> actualOutputList = judgeContext.getActualOutputList();
        Question question = judgeContext.getQuestion();
        Long maxMemoryUsed = judgeContext.getMaxMemoryUsed();
        Long maxTimeUsed = judgeContext.getMaxTimeUsed();

        // 解析题目的判题配置
        JudgeConfig judgeConfig = JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class);
        Long presetTimeLimit = judgeConfig.getTimeLimit();
        Long presetMemoryLimit = judgeConfig.getMemoryLimit();

        // 准备返回的判题信息对象
        JudgeInfo resultJudgeInfo = new JudgeInfo();
        resultJudgeInfo.setTimeUsed(maxTimeUsed);
        resultJudgeInfo.setMemoryUsed(maxMemoryUsed);

        // 处理判断逻辑
        if (expectedOutputList.size() != actualOutputList.size()) { // 先判断输出个数是否一致
            resultJudgeInfo.setResult(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
        } else {
            for (int i = 0; i < expectedOutputList.size(); i++) { // 再逐个判断是否相等
                String eOpt = expectedOutputList.get(i);
                String aOpt = actualOutputList.get(i);
                if (!eOpt.equals(aOpt)) {
                    resultJudgeInfo.setResult(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                    String format = String.format("预期输出：%s，实际输出：%s",
                            ArrayUtils.toString(eOpt), ArrayUtils.toString(aOpt));
                    resultJudgeInfo.setErrorMessage(format);
                    return resultJudgeInfo;
                }
            }
            // 判断是否符合题目预设限制
            if (maxMemoryUsed < 0 || maxTimeUsed < 0) {
                resultJudgeInfo.setResult(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
            } else if (maxTimeUsed > presetTimeLimit) {
                resultJudgeInfo.setResult(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            } else if (maxMemoryUsed > presetMemoryLimit) {
                resultJudgeInfo.setResult(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            } else {
                // 如果以上判断都通过，则认为答案正确
                resultJudgeInfo.setResult(JudgeInfoMessageEnum.ACCEPTED.getValue());
            }
        }
        return resultJudgeInfo;
    }
}
