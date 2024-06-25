package com.yanweiyi.micodebackend.exception;

import com.yanweiyi.micodebackend.common.ApiResponse;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author yanweiyi
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> businessExceptionHandler(BusinessException businessException) {
        log.error("BusinessException", businessException);
        return ResultUtils.fail(businessException);
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> runtimeExceptionHandler(RuntimeException businessException) {
        log.error("RuntimeException", businessException);
        return ResultUtils.fail(ApiStatusCode.SYSTEM_ERROR);
    }
}
