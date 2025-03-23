package com.yanweiyi.micodebackend.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.codesandbox.CodeSandbox;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Value;

/**
 * 远程调用代码沙箱
 *
 * @author yanweiyi
 */
public class RemoteCodeSandbox implements CodeSandbox {

    @Value("${codesandbox.remote.url}")
    private String url;

    /**
     * 鉴权请求头和密钥
     */
    private static final String AUTH_REQUEST_HEADER = "nnnu";
    private static final String AUTH_REQUEST_SECRET = "231510029";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String jsonStr = JSONUtil.toJsonStr(request);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(jsonStr)
                .execute()
                .body();
        if (StrUtil.isBlank(responseStr)) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "代码沙箱服务调用失败");
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
