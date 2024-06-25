package com.yanweiyi.micodebackend.judge.strategy;

import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.strategy.model.JudgeContext;

/**
 * 判题策略抽象类（每种语言可能都有不同的判题策略）
 *
 * @author yanweiyi
 */
public interface JudgeStrategy {

    JudgeInfo applyJudgeStrategy(JudgeContext judgeContext);

}
