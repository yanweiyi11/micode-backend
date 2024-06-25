package com.yanweiyi.micodebackend.exception;

import com.yanweiyi.micodebackend.common.ApiStatusCode;
import lombok.Getter;

/**
 * 自定义异常类
 *
 * @author yanweiyi
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -2728725188303222094L;

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(ApiStatusCode apiStatusCode) {
        super(apiStatusCode.getMessage());
        this.code = apiStatusCode.getCode();
    }

    public BusinessException(ApiStatusCode apiStatusCode, String message) {
        super(message);
        this.code = apiStatusCode.getCode();
    }

}
