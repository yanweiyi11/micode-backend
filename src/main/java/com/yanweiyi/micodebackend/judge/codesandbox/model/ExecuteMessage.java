package com.yanweiyi.micodebackend.judge.codesandbox.model;

import lombok.Data;

/**
 * 封装执行单次程序返回的信息
 *
 * @author yanweiyi
 */
@Data
public class ExecuteMessage {

    /**
     * 程序输出
     */
    private String output;

    /**
     * 程序执行过程中的错误输出
     */
    private String errorOutput;

    /**
     * 程序执行消耗的内存量，单位为字节
     */
    private Long memoryUsed;

    /**
     * 程序执行消耗的时间，单位为毫秒
     */
    private Long timeUsed;

    /**
     * 程序是否执行超时
     */
    private Boolean isTimeout;

    /**
     * 程序是否内存溢出
     */
    private Boolean isMemoryOverflow;
}

