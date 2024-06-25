package com.yanweiyi.micodebackend.judge.codesandbox.impl;

import com.yanweiyi.micodebackend.judge.codesandbox.CodeSandbox;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 接入第三方代码沙箱
 *
 * @author yanweiyi
 */
@Deprecated
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        return null;
    }
}
