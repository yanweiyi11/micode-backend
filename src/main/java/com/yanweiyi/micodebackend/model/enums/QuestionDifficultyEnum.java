package com.yanweiyi.micodebackend.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目难度枚举
 */
@Getter
public enum QuestionDifficultyEnum {

    EASY("简单", "简单"),
    MEDIUM("中等", "中等"),
    HARD("困难", "困难");

    private final String text;
    private final String value;

    QuestionDifficultyEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public Boolean equalsValue(String eqValue) {
        return value.equals(eqValue);
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static QuestionDifficultyEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (QuestionDifficultyEnum difficultyEnum : values()) {
            if (difficultyEnum.value.equals(value)) {
                return difficultyEnum;
            }
        }
        return null;
    }

}
