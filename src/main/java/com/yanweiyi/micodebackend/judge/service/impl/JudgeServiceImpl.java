package com.yanweiyi.micodebackend.judge.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.codesandbox.CodeSandbox;
import com.yanweiyi.micodebackend.judge.codesandbox.CodeSandboxFactory;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.yanweiyi.micodebackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.yanweiyi.micodebackend.judge.codesandbox.model.enums.ExecuteInfoEnum;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.model.enums.JudgeInfoResultEnum;
import com.yanweiyi.micodebackend.judge.service.JudgeService;
import com.yanweiyi.micodebackend.judge.strategy.JudgeStrategy;
import com.yanweiyi.micodebackend.judge.strategy.impl.DefaultJudgeStrategy;
import com.yanweiyi.micodebackend.judge.strategy.impl.PythonJudgeStrategy;
import com.yanweiyi.micodebackend.judge.strategy.model.JudgeContext;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitLanguageEnum;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitStatusEnum;
import com.yanweiyi.micodebackend.model.vo.QuestionSubmitDetailVO;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yanweiyi
 */
@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Value("${codesandbox.type}")
    private String type;

    private static final Map<String, JudgeStrategy> JUDGE_STRATEGY_MAP = new HashMap<>();

    // 注册各种语言的判题策略
    static {
        JUDGE_STRATEGY_MAP.put(QuestionSubmitLanguageEnum.JAVA.getValue(), new DefaultJudgeStrategy());
        JUDGE_STRATEGY_MAP.put(QuestionSubmitLanguageEnum.PYTHON.getValue(), new PythonJudgeStrategy());
    }

    /**
     * 判题服务
     *
     * @param questionSubmitId
     */
    @Override
    public QuestionSubmitDetailVO doJudge(long questionSubmitId) {
        // 根据提交 ID 获取提交信息和对应题目
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 题目的提交数 + 1
        synchronized (this) {
            Integer submitNum = question.getSubmitNum();
            question.setSubmitNum(submitNum + 1);
        }

        // 检查提交状态，如果不是等待状态则抛出异常
        Integer submitStatus = questionSubmit.getStatus();
        if (!QuestionSubmitStatusEnum.WAITING.equalsValue(submitStatus)) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "题目正在判题中");
        }

        // 设置提交状态为判题中，并更新数据库
        questionSubmit.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean isUpdated = questionSubmitService.updateById(questionSubmit);
        if (!isUpdated) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "判题状态更新错误");
        }

        // 准备执行代码所需的数据
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        if (QuestionSubmitLanguageEnum.getEnumByValue(language) == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "语言不存在");
        }

        // 将输入用例从 JSON 字符串转换为对象列表
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList;
        try {
            judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        } catch (Exception e) {
            log.error("judgeCase String '{}' convertTo judgeCase error: {}", judgeCaseStr, e.getMessage());
            throw new BusinessException(ApiStatusCode.SYSTEM_ERROR);
        }
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        // 构建执行代码请求并通过沙箱执行
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage(language);
        executeCodeRequest.setInputList(inputList);
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);

        // 执行代码沙箱
        ExecuteCodeResponse executeCodeResponse;
        executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        // 获取执行结果
        List<String> actualOutputList = executeCodeResponse.getOutputList();
        String errorMessage = executeCodeResponse.getErrorMessage();
        List<Long> memoryUsedList = executeCodeResponse.getMemoryUsedList();
        List<Long> timeUsedList = executeCodeResponse.getTimeUsedList();
        Integer status = executeCodeResponse.getStatus();

        // 获取预设的输出用例
        List<String> expectedOutputList = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        // 初始化返回对象
        QuestionSubmitDetailVO questionSubmitDetailVO = new QuestionSubmitDetailVO();
        if (ExecuteInfoEnum.SUCCESS.equalsValue(status)) { // 执行成功则开始判题
            // 构建判题上下文准备评估执行输出
            if (CollectionUtil.isEmpty(memoryUsedList) || CollectionUtil.isEmpty(timeUsedList) || ObjectUtil.isEmpty(status)) {
                // 确保必要的执行信息没有缺失
                throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
            }
            JudgeContext judgeContext = new JudgeContext();
            judgeContext.setExpectedOutputList(expectedOutputList);
            judgeContext.setActualOutputList(actualOutputList);
            judgeContext.setQuestion(question);
            judgeContext.setStatus(status);

            // 计算最大内存使用量与最大执行时间
            Long maxMemoryUsed = Collections.max(memoryUsedList);
            Long maxTimeUsed = Collections.max(timeUsedList);
            judgeContext.setMaxMemoryUsed(maxMemoryUsed);
            judgeContext.setMaxTimeUsed(maxTimeUsed);

            // 使用判题策略评估执行结果，并设置判题信息
            JudgeStrategy judgeStrategy = JUDGE_STRATEGY_MAP.get(language);
            JudgeInfo resultJudgeInfo = judgeStrategy.applyJudgeStrategy(judgeContext);

            String judgeResult = resultJudgeInfo.getResult();
            if (judgeResult.equals(JudgeInfoResultEnum.ACCEPTED.getValue())) {
                // 题目的通过数量 + 1
                synchronized (this) {
                    Integer acceptedNum = question.getAcceptedNum();
                    question.setAcceptedNum(acceptedNum + 1);
                }
            }
            questionSubmit.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(resultJudgeInfo));
            BeanUtils.copyProperties(questionSubmit, questionSubmitDetailVO);
            questionSubmitDetailVO.setJudgeInfo(resultJudgeInfo);
        } else { // 执行不成功
            JudgeInfo failJudgeInfo = getFailJudgeInfo(errorMessage, status);
            questionSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(failJudgeInfo));
            BeanUtils.copyProperties(questionSubmit, questionSubmitDetailVO);
            questionSubmitDetailVO.setJudgeInfo(failJudgeInfo);
        }

        // 更新提交状态与判题信息
        isUpdated = questionSubmitService.updateById(questionSubmit);
        if (!isUpdated) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "提交信息更新错误");
        }

        // 更新题目，修改了提交数和通过数
        isUpdated = questionService.updateById(question);
        if (!isUpdated) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "题目信息更新错误");
        }

        // 返回含有判题结果的视图对象
        return questionSubmitDetailVO;
    }

    /**
     * 获取执行不成功的判题信息
     */
    @NotNull
    private static JudgeInfo getFailJudgeInfo(String errorMessage, Integer status) {
        JudgeInfo failJudgeInfo = new JudgeInfo();
        failJudgeInfo.setTimeUsed(0L);
        failJudgeInfo.setMemoryUsed(0L);
        failJudgeInfo.setErrorMessage(errorMessage);
        // 分析失败原因
        if (ExecuteInfoEnum.COMPILE_ERROR.equalsValue(status)) {
            failJudgeInfo.setResult(JudgeInfoResultEnum.COMPILE_ERROR.getValue());
        } else if (ExecuteInfoEnum.EXECUTION_TIMEOUT.equalsValue(status)) {
            failJudgeInfo.setResult(JudgeInfoResultEnum.TIME_LIMIT_EXCEEDED.getValue());
        } else if (ExecuteInfoEnum.EXECUTION_ERROR.equalsValue(status)) {
            failJudgeInfo.setResult(JudgeInfoResultEnum.RUNTIME_ERROR.getValue());
        } else if (ExecuteInfoEnum.SYSTEM_ERROR.equalsValue(status)) {
            failJudgeInfo.setResult(JudgeInfoResultEnum.SYSTEM_ERROR.getValue());
        } else if (ExecuteInfoEnum.MEMORY_OVERFLOW.equalsValue(status)) {
            failJudgeInfo.setResult(JudgeInfoResultEnum.MEMORY_LIMIT_EXCEEDED.getValue());
        }
        return failJudgeInfo;
    }
}
