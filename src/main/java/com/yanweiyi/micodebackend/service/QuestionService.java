package com.yanweiyi.micodebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanweiyi.micodebackend.model.dto.question.QuestionDetailQueryRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionQueryRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.vo.QuestionDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yanweiyi
 * @description 针对表【question(题目表)】的数据库操作Service
 * @createDate 2024-05-30 21:59:43
 */
public interface QuestionService extends IService<Question> {

    QuestionDetailVO findQuestionDetailVO(Long id, HttpServletRequest request);

    Page<QuestionDetailVO> findQuestionDetailVOByPage(QuestionDetailQueryRequest queryRequest, HttpServletRequest request);

    QuestionVO findQuestionVOById(Long id, HttpServletRequest request);

    Page<QuestionVO> findQuestionVOByPage(QuestionQueryRequest queryRequest);
}
