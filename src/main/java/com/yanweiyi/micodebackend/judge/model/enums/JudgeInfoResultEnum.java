package com.yanweiyi.micodebackend.judge.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 *
 * @author yanweiyi
 */
@Getter
public enum JudgeInfoResultEnum {

    ACCEPTED("执行通过"),
    COMPILE_ERROR("编译失败"),
    MEMORY_LIMIT_EXCEEDED("内存溢出"),
    TIME_LIMIT_EXCEEDED("执行超时"),
    RUNTIME_ERROR("运行错误"),
    SYSTEM_ERROR("系统错误"),
    WRONG_ANSWER("答案错误"),
    DANGEROUS_OPERATION("危险操作"),
    PRESENTATION_ERROR("展示错误"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出"),
    WAITING("等待中");

    private final String value;

    JudgeInfoResultEnum(String value) {
        this.value = value;
    }

    public Boolean equalsValue(String eqValue) {
        return value.equals(eqValue);
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(JudgeInfoResultEnum::getValue).collect(Collectors.toList());
    }

    /**
     * 根据 message 获取枚举
     */
    public static JudgeInfoResultEnum getEnumByMessage(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        for (JudgeInfoResultEnum anEnum : JudgeInfoResultEnum.values()) {
            if (anEnum.value.equals(message)) {
                return anEnum;
            }
        }
        return null;
    }

}