package com.yanweiyi.micodebackend.common;

import lombok.Data;

/**
 * 返回值对象
 *
 * @author yanweiyi
 */
@Data
public class ApiResponse<T> {

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
