package com.yanweiyi.micodebackend.common;

import com.yanweiyi.micodebackend.exception.BusinessException;

/**
 * 返回工具类
 *
 * @author yanweiyi
 */
public class ResultUtils {

    /**
     * 响应成功，携带数据
     *
     * @param data 数据
     * @return ApiResponse 实例
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiStatusCode apiStatusCode = ApiStatusCode.SUCCESS;
        return new ApiResponse<>(apiStatusCode.getCode(), apiStatusCode.getMessage(), data);
    }

    /**
     * 响应成功，不携带数据
     *
     * @return ApiResponse 实例
     */
    public static ApiResponse<Void> success() {
        ApiStatusCode apiStatusCode = ApiStatusCode.SUCCESS;
        return new ApiResponse<>(apiStatusCode.getCode(), apiStatusCode.getMessage(), null);
    }

    /**
     * 响应失败
     *
     * @param apiStatusCode 状态码
     * @return ApiResponse 实例
     */
    public static ApiResponse<Void> fail(ApiStatusCode apiStatusCode) {
        return new ApiResponse<>(apiStatusCode.getCode(), apiStatusCode.getMessage());
    }

    /**
     * 响应失败，自定义错误消息
     *
     * @param apiStatusCode 状态码
     * @param message       错误消息
     * @return ApiResponse 实例
     */
    public static ApiResponse<Void> fail(ApiStatusCode apiStatusCode, String message) {
        return new ApiResponse<>(apiStatusCode.getCode(), message);
    }

    /**
     * 响应失败，使用 BusinessException
     *
     * @param businessException 业务异常
     * @return ApiResponse 实例
     */
    public static ApiResponse<Void> fail(BusinessException businessException) {
        return new ApiResponse<>(businessException.getCode(), businessException.getMessage());
    }
}
