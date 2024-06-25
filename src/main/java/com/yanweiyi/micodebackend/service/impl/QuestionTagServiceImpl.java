package com.yanweiyi.micodebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanweiyi.micodebackend.mapper.QuestionTagMapper;
import com.yanweiyi.micodebackend.model.entity.QuestionTag;
import com.yanweiyi.micodebackend.model.vo.TagsVO;
import com.yanweiyi.micodebackend.service.QuestionTagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yanweiyi
 * @description 针对表【question_tag(题目标签表)】的数据库操作Service实现
 * @createDate 2024-06-04 21:36:01
 */
@Service
public class QuestionTagServiceImpl extends ServiceImpl<QuestionTagMapper, QuestionTag> implements QuestionTagService {

    @Override
    public Map<String, List<String>> findStructuredTags(Object...obj) {
        List<QuestionTag> questionTagList = this.list();

        // 首先，创建一个映射 parentId: tagName 的父标签 Map
        Map<Long, String> parentTags = questionTagList.stream()
                .filter(QuestionTag::isParentBool) // 使用 isParentBool() 方法过滤出父标签
                .collect(Collectors.toMap(QuestionTag::getId, QuestionTag::getTagName));

        // 然后，基于 parentTags 的映射关系，建立一个 Map 以存储最终的结果
        Map<String, List<String>> tagMap = new HashMap<>();

        // 初始填充 tagMap，确保所有父标签都有对应的空列表
        parentTags.values().forEach(tagName -> tagMap.put(tagName, new ArrayList<>()));

        // 遍历 questionTagList 填充 tagMap 中子标签的列表
        for (QuestionTag tag : questionTagList) {
            // 跳过父标签，并仅处理子标签（isParent == 0）
            if (!tag.isParentBool()) {
                String parentName = parentTags.get(tag.getParentId());
                if (parentName != null) { // 如果找到对应的父标签
                    tagMap.get(parentName).add(tag.getTagName());
                }
            }
        }
        return tagMap;
    }

    @Override
    public List<TagsVO> findStructuredTags() {
        List<QuestionTag> questionTagList = this.list();

        // 首先，创建一个映射 parentId: tagName 的父标签 Map
        Map<Long, String> parentTags = questionTagList.stream()
                .filter(QuestionTag::isParentBool) // 使用 isParentBool() 方法过滤出父标签
                .collect(Collectors.toMap(QuestionTag::getId, QuestionTag::getTagName));

        // 为每个父标签创建一个 TagsVOList 实例并收集所有父标签
        List<TagsVO> tagsVOList = parentTags.values().stream()
                .map(s -> new TagsVO(s, new ArrayList<>()))
                .collect(Collectors.toList());

        // 填充 TagsVOList 中子标签的列表
        for (QuestionTag tag : questionTagList) {
            // 跳过父标签，并仅处理子标签（isParent == 0）
            if (!tag.isParentBool()) {
                String parentName = parentTags.get(tag.getParentId());
                // 找到对应的 TagsVOList 并添加子标签
                tagsVOList.stream()
                        .filter(ts -> ts.getParentName().equals(parentName))
                        .findFirst()
                        .ifPresent(ts -> ts.getChildNameList().add(tag.getTagName()));
            }
        }
        return tagsVOList;
    }
}




