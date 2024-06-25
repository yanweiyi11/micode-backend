package com.yanweiyi.micodebackend.judge.model.dto;

import lombok.Data;

/**
 * 判题用例
 *
 * @author yanweiyi
 */
@Data
public class JudgeCase {

    /**
     * 输入用例
     */
    private String input;

    /**
     * 输出用例
     */
    private String output;

}
