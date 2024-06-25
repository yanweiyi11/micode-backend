package com.yanweiyi.micodebackend.judge.service;

import com.yanweiyi.micodebackend.model.vo.QuestionSubmitDetailVO;

/**
 * @author yanweiyi
 */
public interface JudgeService {

    /**
     * 判题服务
     *
     * @param questionSubmitId
     * @return
     */
    QuestionSubmitDetailVO doJudge(long questionSubmitId);

}
