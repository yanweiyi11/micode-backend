package com.yanweiyi.micodebackend.judge.strategy.model;

import com.yanweiyi.micodebackend.model.entity.Question;
import lombok.Data;

import java.util.List;

/**
 * 接收判题需要传递参数的对象，策略模式中的判题方法需要什么，这里就加上什么
 *
 * @author yanweiyi
 */
@Data
public class JudgeContext {

    /**
     * 预期输出
     */
    private List<String> expectedOutputList;

    /**
     * 实际输出
     */
    private List<String> actualOutputList;

    /**
     * 执行的题目对象
     */
    private Question question;

    /**
     * 最大内存使用量（字节）
     */
    private Long maxMemoryUsed;

    /**
     * 最大执行时间（毫秒）
     */
    private Long maxTimeUsed;

    /**
     * 执行结果状态
     */
    private Integer status;
}
