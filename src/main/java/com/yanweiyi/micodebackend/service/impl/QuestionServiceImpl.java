package com.yanweiyi.micodebackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.constant.SortOrderConstant;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.model.enums.JudgeInfoResultEnum;
import com.yanweiyi.micodebackend.mapper.QuestionMapper;
import com.yanweiyi.micodebackend.mapper.QuestionSubmitMapper;
import com.yanweiyi.micodebackend.model.dto.question.QuestionDetailQueryRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionQueryRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitStatusEnum;
import com.yanweiyi.micodebackend.model.vo.QuestionDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionVO;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionSubmitService;
import com.yanweiyi.micodebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yanweiyi
 * @description 针对表【question(题目表)】的数据库操作Service实现
 * @createDate 2024-05-30 21:59:43
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    public QuestionDetailVO findQuestionDetailVO(Long id, HttpServletRequest request) {
        Question question = this.getById(id);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        questionDetailVO.copyByQuestion(question);

        User loginUser = userService.getLoginUserOrThrow(request);
        // 判断用户是否是管理员和题目创建者，如果不是需要置空答案
        if (!userService.isAdmin(loginUser) && !question.getUserId().equals(loginUser.getId())) {
            questionDetailVO.setAnswer(null);
        }
        // 获取当前用户最新的对此题目的判题信息和提交状态
        LambdaQueryWrapper<QuestionSubmit> questionSubmitWrapper = new LambdaQueryWrapper<>();
        questionSubmitWrapper.eq(QuestionSubmit::getQuestionId, id);
        questionSubmitWrapper.eq(QuestionSubmit::getUserId, loginUser.getId());
        questionSubmitWrapper.orderByDesc(QuestionSubmit::getCreateTime);
        List<QuestionSubmit> questionSubmitList = questionSubmitService.list(questionSubmitWrapper);
        // 如果用户没有提交过，就直接返回了
        if (questionSubmitList.isEmpty()) {
            return questionDetailVO;
        }
        QuestionSubmit questionSubmit = questionSubmitList.get(0);
        questionDetailVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        questionDetailVO.setStatus(questionSubmit.getStatus());
        return questionDetailVO;
    }

    @Override
    public Page<QuestionDetailVO> findQuestionDetailVOByPage(QuestionDetailQueryRequest queryRequest, HttpServletRequest request) {
        // 初始化分页对象
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 需要查询题目的基本信息
        Page<Question> questionPage = this.page(new Page<>(page, size), null);
        List<Question> questionList = questionPage.getRecords();
        Map<Long, QuestionSubmit> questionSubmitMap = null;
        if (loginUser != null) {
            List<QuestionSubmit> questionSubmitList = questionSubmitMapper.selectLastQuestionSubmitList(loginUser.getId());
            questionSubmitMap = questionSubmitList.stream()
                    .collect(Collectors.toMap(QuestionSubmit::getQuestionId, questionSubmit -> questionSubmit));
        }
        List<QuestionDetailVO> questionDetailVOList = new ArrayList<>();
        for (Question question : questionList) {
            QuestionDetailVO questionDetailVO = new QuestionDetailVO();
            questionDetailVO.copyByQuestion(question);
            if (loginUser == null) {
                // 如果用户未登录
                questionDetailVO.setAnswer(null);
                questionDetailVO.setJudgeInfo(null);
                questionDetailVO.setStatus(null);
            } else {
                // 如果用户已登录，并且已提交过此题目
                QuestionSubmit questionSubmit = questionSubmitMap.get(question.getId());
                if (questionSubmit != null) {
                    // 设置给对应题目的提交状态、提交状态
                    questionDetailVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
                    questionDetailVO.setStatus(questionSubmit.getStatus());
                    // 如果用户不是管理员和题目创建者，需要把没有写对的题目答案置空
                    JudgeInfo judgeInfo = JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class);
                    String result = judgeInfo.getResult();
                    if (!userService.isAdmin(loginUser) &&
                            !question.getUserId().equals(loginUser.getId()) &&
                            !JudgeInfoResultEnum.ACCEPTED.equalsValue(result)) {
                        questionDetailVO.setAnswer(null);
                    }
                } else {
                    // 如果用户未提交过此题目
                    if (!userService.isAdmin(loginUser) && !question.getUserId().equals(loginUser.getId())) {
                        // 如果不是管理员和题目创建者则置空答案
                        questionDetailVO.setAnswer(null);
                    }
                    // 将判题状态和提交状态置空
                    questionDetailVO.setJudgeInfo(null);
                    questionDetailVO.setStatus(null);
                }
            }
            questionDetailVOList.add(questionDetailVO);
        }

        // 转换为视图对象并设置分页信息
        Page<QuestionDetailVO> questionDetailVOPage = new Page<>(page, size, questionPage.getTotal());
        questionDetailVOPage.setRecords(questionDetailVOList);

        return questionDetailVOPage;
    }

    @Override
    public Page<QuestionVO> findQuestionVOByPage(QuestionQueryRequest queryRequest) {
        // 从请求中取得查询关键字
        String searchKey = queryRequest.getSearchKey();
        // 获取过滤标签列表
        List<String> tags = queryRequest.getTags();
        // 获取难度等级
        String difficulty = queryRequest.getDifficulty();
        // 获取分页参数
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();
        // 获取排序字段及排序顺序
        String sort = queryRequest.getSort();
        String order = queryRequest.getOrder();

        // 创建条件构造器
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(searchKey)) {
            // 为搜索关键字创建OR条件查询
            questionWrapper.nested(wrapper -> {
                if (NumberUtil.isNumber(searchKey)) {
                    wrapper.like(Question::getId, Long.parseLong(searchKey)).or();
                }
                wrapper.or().like(Question::getTitle, searchKey);
                wrapper.or().like(Question::getContent, searchKey);
            });
        }
        if (CollectionUtil.isNotEmpty(tags)) {
            // 遍历标签列表，为每个标签创建LIKE查询条件
            for (String tag : tags) {
                questionWrapper.like(Question::getTags, tag);
            }
        }
        // 为难度等级添加查询条件
        questionWrapper.eq(StringUtils.isNotEmpty(difficulty), Question::getDifficulty, difficulty);
        // 设置排序方式
        if (SortOrderConstant.SORT_ORDER_ASC.equals(order)) {
            questionWrapper.orderByAsc(getSortField(sort));
        } else if (SortOrderConstant.SORT_ORDER_DESC.equals(order)) {
            questionWrapper.orderByDesc(getSortField(sort));
        }

        // 执行分页查询
        Page<Question> questionPage = this.page(new Page<>(page, size), questionWrapper);
        List<Question> questionList = questionPage.getRecords();

        // 将查询结果转换为VO列表
        List<QuestionVO> questionVOList = new ArrayList<>();
        for (Question question : questionList) {
            QuestionVO questionVO = new QuestionVO();
            // 使用 BeanUtil.copyProperties 报错
            questionVO.copyByQuestion(question);
            questionVOList.add(questionVO);
        }
        // 创建VO分页对象并设置查询结果
        Page<QuestionVO> questionVOPage = new Page<>(page, size, questionPage.getTotal());
        questionVOPage.setRecords(questionVOList);

        return questionVOPage;
    }

    @Override
    public QuestionVO findQuestionVOById(Long id, HttpServletRequest request) {
        // 根据id查询题目信息
        Question question = this.getById(id);
        // 如果题目不存在，抛出错误
        if (question == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }

        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        List<QuestionSubmit> questionSubmitList = new ArrayList<>();
        if (loginUser != null) {
            Long userId = loginUser.getId(); // 当前登录用户ID
            // 构建查询条件，查找当前用户对该题目的提交记录，且状态为成功
            LambdaQueryWrapper<QuestionSubmit> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionSubmit::getUserId, userId).eq(QuestionSubmit::getQuestionId, id).eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.SUCCEED.getValue());
            questionSubmitList = questionSubmitService.list(queryWrapper);
        }
        Long creatorId = question.getUserId(); // 题目创建者ID

        // 检查访问权限：题目创建者、管理员、成功完成题目的用户可以访问
        if (loginUser == null || !creatorId.equals(loginUser.getId()) && !userService.isAdmin(loginUser) && questionSubmitList.isEmpty()) {
            // 如果不满足以上任一条件，则置空答案
            question.setAnswer(null);
        }
        // 如果验证通过，返回查询到的题目信息，并转换成视图对象
        QuestionVO questionVO = new QuestionVO();
        questionVO.copyByQuestion(question);
        return questionVO;
    }

    /**
     * 根据传入的 sort 字符串获取对应的排序字段
     */
    private SFunction<Question, ?> getSortField(String sort) {
        switch (sort) {
            case "updateTime":
                return Question::getUpdateTime;
            case "createTime":
                return Question::getCreateTime;
            default:
                return null;
        }
    }

}




