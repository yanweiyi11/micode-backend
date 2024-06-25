package com.yanweiyi.micodebackend.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanweiyi.micodebackend.common.ApiResponse;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.common.DeleteRequest;
import com.yanweiyi.micodebackend.common.ResultUtils;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.model.dto.question.QuestionAddRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionDetailQueryRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionQueryRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionUpdateRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.vo.QuestionDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionVO;
import com.yanweiyi.micodebackend.model.vo.TagsVO;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionTagService;
import com.yanweiyi.micodebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yanweiyi
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionTagService questionTagService;

    @PostMapping("/add")
    public ApiResponse<Long> addQuestion(@RequestBody QuestionAddRequest addRequest, HttpServletRequest request) {
        // 校验新增问题请求对象是否为 null
        if (addRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }

        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);

        // 创建对象并拷贝数据
        Question question = new Question();
        BeanUtils.copyProperties(addRequest, question);

        // 处理拷贝不了的 json 数据
        List<String> tagsJson = addRequest.getTags();
        if (CollectionUtil.isNotEmpty(tagsJson)) {
            question.setTags(JSONUtil.toJsonStr(tagsJson));
        }
        JudgeConfig judgeConfig = addRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        List<JudgeCase> judgeCaseJson = addRequest.getJudgeCase();
        if (CollectionUtil.isNotEmpty(judgeCaseJson)) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCaseJson));
        }

        // 设定题目创建者的 id
        Long userId = loginUser.getId();
        question.setUserId(userId);

        // 保存到数据库
        boolean isSaved = questionService.save(question);
        if (!isSaved) {
            // 如果数据库保存失败，抛出操作错误异常
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }

        // 获取保存后的题目 id
        Long questionId = question.getId();
        // 返回操作成功的响应，携带新增题目的 id
        return ResultUtils.success(questionId);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 检查请求数据及 id 是否为空
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }

        // 根据 id 获取题目信息
        Long questionId = deleteRequest.getId();
        Question question = questionService.getById(questionId);
        // 如果题目不存在，抛出错误
        if (question == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }

        // 检查权限：只有问题创建者和管理员能删除问题
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId(); // 当前用户 id
        Long creatorId = question.getUserId(); // 创建者 id
        if (!creatorId.equals(userId) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }

        // 执行删除操作
        boolean isRemoved = questionService.removeById(questionId);
        // 返回删除操作的结果
        return ResultUtils.success(isRemoved);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest updateRequest, HttpServletRequest request) {
        // 检查更新请求对象和请求 id 的有效性
        if (updateRequest == null || updateRequest.getId() == null || updateRequest.getId() <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }

        // 检查题目是否存在
        Long questionId = updateRequest.getId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }

        // 只有问题创建者或管理员才能更新问题
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long creatorId = question.getUserId();
        if (!creatorId.equals(userId) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }

        // 将请求更新的数据复制到题目对象
        BeanUtils.copyProperties(updateRequest, question);

        // 处理拷贝不了的 json 数据
        List<String> tagsJson = updateRequest.getTags();
        if (CollectionUtil.isNotEmpty(tagsJson)) {
            question.setTags(JSONUtil.toJsonStr(tagsJson));
        }
        JudgeConfig judgeConfig = updateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        List<JudgeCase> judgeCaseJson = updateRequest.getJudgeCase();
        if (CollectionUtil.isNotEmpty(judgeCaseJson)) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCaseJson));
        }

        // 尝试更新，如果更新失败则抛出异常
        boolean isUpdated = questionService.updateById(question);
        if (!isUpdated) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }

        // 更新成功，返回成功状态
        return ResultUtils.success(Boolean.TRUE);
    }

    @GetMapping("/get-detail")
    public ApiResponse<QuestionDetailVO> getQuestionDetailVOById(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        QuestionDetailVO questionDetailVO = questionService.findQuestionDetailVO(id, request);
        return ResultUtils.success(questionDetailVO);
    }

    @PostMapping("/list-detail-page")
    public ApiResponse<Page<QuestionDetailVO>> listQuestionDetailVOByPage(@RequestBody QuestionDetailQueryRequest queryRequest,
                                                                          HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        int size = queryRequest.getSize();
        if (size > 20) { // 防止爬虫
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        Page<QuestionDetailVO> questionDetailVOByPage = questionService.findQuestionDetailVOByPage(queryRequest, request);
        return ResultUtils.success(questionDetailVOByPage);
    }

    @GetMapping("/get-vo")
    public ApiResponse<QuestionVO> getQuestionVOById(@RequestParam Long id, HttpServletRequest request) {
        // 检查请求参数id的有效性
        if (id == null || id <= 0) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        QuestionVO questionVO = questionService.findQuestionVOById(id, request);
        return ResultUtils.success(questionVO);
    }

    @PostMapping("/list-vo-page")
    public ApiResponse<Page<QuestionVO>> listQuestionByPage(@RequestBody QuestionQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        // 仅管理员可查看
        if (userService.isAdmin(request)) {
            Page<QuestionVO> questionVOByPage = questionService.findQuestionVOByPage(queryRequest);
            return ResultUtils.success(questionVOByPage);
        }
        throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
    }

    @GetMapping("/get-tags")
    public ApiResponse<List<TagsVO>> getTags() {
        List<TagsVO> tagsVOList = questionTagService.findStructuredTags();
        if (tagsVOList == null) {
            throw new BusinessException(ApiStatusCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(tagsVOList);
    }
}
