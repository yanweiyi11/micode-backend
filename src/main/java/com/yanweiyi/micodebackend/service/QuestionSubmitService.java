package com.yanweiyi.micodebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitDetailQueryRequest;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.vo.QuestionSubmitDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yanweiyi
 * @description 针对表【question_submit(题目提交表)】的数据库操作Service
 * @createDate 2024-05-30 21:59:43
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    QuestionSubmitDetailVO findQuestionSubmitDetailVO(Long id, HttpServletRequest request);

    Page<QuestionSubmitDetailVO> findQuestionSubmitDetailVOByPage(QuestionSubmitDetailQueryRequest queryRequest, HttpServletRequest request);

    List<QuestionSubmitVO> findQuestionSubmitVOByQuestionId(Long questionId, HttpServletRequest request);
}
