package com.yanweiyi.micodebackend.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.codesandbox.CodeSandbox;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程调用代码沙箱
 *
 * @author yanweiyi
 */
public class RemoteCodeSandbox implements CodeSandbox {

    /**
     * 鉴权请求头和密钥
     */
    private static final String AUTH_REQUEST_HEADER = "nnnu";
    private static final String AUTH_REQUEST_SECRET = "231510029";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String url = "http://159.75.93.145:11090/executeCode";
        // String url = "http://192.168.126.3:11090/executeCode";
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
