package com.yanweiyi.micodebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanweiyi.micodebackend.model.dto.question.QuestionDetailQueryRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionDetail;
import org.apache.ibatis.annotations.Param;

/**
 * @author yanweiyi
 * @description 针对表【question(题目表)】的数据库操作Mapper
 * @createDate 2024-05-30 21:59:43
 * @Entity com.yanweiyi.micodebackend.model.entity.Question
 */
public interface QuestionMapper extends BaseMapper<Question> {

    IPage<QuestionDetail> selectQuestionDetailByPage(Page<?> page,
                                                     @Param("queryRequest") QuestionDetailQueryRequest queryRequest);

    QuestionDetail selectQuestionDetailById(@Param("questionId") Long questionId);
}



