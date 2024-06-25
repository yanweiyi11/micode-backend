package com.yanweiyi.micodebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanweiyi.micodebackend.model.entity.QuestionTag;
import com.yanweiyi.micodebackend.model.vo.TagsVO;

import java.util.List;
import java.util.Map;

/**
 * @author yanweiyi
 * @description 针对表【question_tag(题目标签表)】的数据库操作Service
 * @createDate 2024-06-04 21:36:01
 */
public interface QuestionTagService extends IService<QuestionTag> {

    @Deprecated
    Map<String, List<String>> findStructuredTags(Object...obj);

    List<TagsVO> findStructuredTags();

}
