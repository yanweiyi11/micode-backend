package com.yanweiyi.micodebackend.judge.codesandbox;

import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * @author yanweiyi
 */
public interface CodeSandbox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);

}
