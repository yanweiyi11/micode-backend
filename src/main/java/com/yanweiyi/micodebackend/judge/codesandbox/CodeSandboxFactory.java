package com.yanweiyi.micodebackend.judge.codesandbox;

import com.yanweiyi.micodebackend.judge.codesandbox.impl.RemoteCodeSandbox;
import com.yanweiyi.micodebackend.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂（根据字符串创建指定的代码沙箱实例）
 *
 * @author yanweiyi
 */
public class CodeSandboxFactory {

    /**
     * 获取代码沙箱对象（使用了工厂模式）
     *
     * @param type
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                throw new IllegalArgumentException("Unable to find code sandbox named '" + type + "'");
        }
    }

}
