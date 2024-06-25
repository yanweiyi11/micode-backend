package com.yanweiyi.micodebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanweiyi.micodebackend.common.ApiResponse;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.common.DeleteRequest;
import com.yanweiyi.micodebackend.common.ResultUtils;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.service.JudgeService;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitDoJudgeRequest;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitDetailQueryRequest;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitUpdateRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitStatusEnum;
import com.yanweiyi.micodebackend.model.vo.QuestionSubmitDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionSubmitVO;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionSubmitService;
import com.yanweiyi.micodebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yanweiyi
 */
@RestController
@RequestMapping("/question-submit")
public class QuestionSubmitController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeService judgeService;

    @PostMapping("/do-judge")
    public ApiResponse<Long> doJudge(@RequestBody QuestionSubmitDoJudgeRequest doJudgeRequest, HttpServletRequest request) {
        if (doJudgeRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(doJudgeRequest, questionSubmit);
        // 判断题目是否存在
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        // 设置初始化状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        // 默认设置空的判题信息
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(new HashMap<>())); // “{}”
        questionSubmit.setUserId(loginUser.getId());
        boolean isSaved = questionSubmitService.save(questionSubmit);
        if (!isSaved) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }
        // 执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        return ResultUtils.success(questionSubmitId);
    }

    @PostMapping("/add")
    public ApiResponse<Long> addQuestionSubmit(@RequestBody QuestionSubmitAddRequest addRequest, HttpServletRequest request) {
        if (addRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(addRequest, questionSubmit);

        User loginUser = userService.getLoginUser(request);
        questionSubmit.setUserId(loginUser.getId());

        JudgeConfig judgeInfoJson = addRequest.getJudgeInfo();
        String judgeInfoStr = JSONUtil.toJsonStr(judgeInfoJson);
        questionSubmit.setJudgeInfo(judgeInfoStr);

        // 插入数据库
        boolean isSaved = questionSubmitService.save(questionSubmit);
        if (!isSaved) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }
        Long questionSubmitId = questionSubmit.getId();
        return ResultUtils.success(questionSubmitId);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteQuestionSubmit(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        // 判断记录是否存在
        Long questionSubmitId = deleteRequest.getId();
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId(); // 当前登录用户 id
        Long creatorId = questionSubmit.getUserId(); // 创建者 id
        if (!creatorId.equals(userId) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        boolean isRemoved = questionSubmitService.removeById(questionSubmitId);
        return ResultUtils.success(isRemoved);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> updateQuestionSubmit(@RequestBody QuestionSubmitUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getId() == null || updateRequest.getId() <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        Long questionSubmitId = updateRequest.getId();
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可更改
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long creatorId = questionSubmit.getUserId();
        if (!creatorId.equals(userId) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        BeanUtils.copyProperties(updateRequest, questionSubmit);

        JudgeInfo judgeConfigJson = updateRequest.getJudgeInfo();
        if (judgeConfigJson != null) {
            String judgeConfigStr = JSONUtil.toJsonStr(judgeConfigJson);
            questionSubmit.setJudgeInfo(judgeConfigStr);
        }

        boolean isUpdated = questionSubmitService.updateById(questionSubmit);
        if (!isUpdated) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    @GetMapping("/get-detail-vo")
    public ApiResponse<QuestionSubmitDetailVO> getQuestionSubmitDetailVOById(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        QuestionSubmitDetailVO questionSubmitDetailVO = questionSubmitService.findQuestionSubmitDetailVO(id, request);
        return ResultUtils.success(questionSubmitDetailVO);
    }

    @PostMapping("/list-detail-vo-page")
    public ApiResponse<Page<QuestionSubmitDetailVO>> listQuestionSubmitDetailVOByPage(@RequestBody QuestionSubmitDetailQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        int size = queryRequest.getSize();
        if (size > 20) { // 防止爬虫
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        Page<QuestionSubmitDetailVO> questionSubmitDetailVOByPage = questionSubmitService.findQuestionSubmitDetailVOByPage(queryRequest, request);
        return ResultUtils.success(questionSubmitDetailVOByPage);
    }

    @PostMapping("/list-vo-user")
    public ApiResponse<List<QuestionSubmitVO>> listQuestionSubmitVOByQuestionId(@RequestParam Long questionId, HttpServletRequest request) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitService.findQuestionSubmitVOByQuestionId(questionId, request);
        return ResultUtils.success(questionSubmitVOList);
    }
}
