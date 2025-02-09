package com.yanweiyi.micodebackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.yanweiyi.micodebackend.mapper.QuestionSubmitMapper;
import com.yanweiyi.micodebackend.model.dto.questionsubmit.QuestionSubmitDetailQueryRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitStatusEnum;
import com.yanweiyi.micodebackend.model.vo.*;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionSubmitService;
import com.yanweiyi.micodebackend.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yanweiyi
 * @description 针对表【question_submit(题目提交表)】的数据库操作Service实现
 * @createDate 2024-05-30 21:59:43
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit> implements QuestionSubmitService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    @Lazy
    private QuestionSubmitService questionSubmitService;

    @Resource
    private RedisTemplate<String, List<UserRankingVO>> redisTemplate;

    private static final String LEADERBOARD_CACHE_KEY = "micode:leaderboard:top20";

    /**
     * 根据传入的 sort 字符串获取对应的排序字段
     */
    private SFunction<QuestionSubmit, ?> getSortField(String sort) {
        switch (sort) {
            case "updateTime":
                return QuestionSubmit::getUpdateTime;
            case "createTime":
                return QuestionSubmit::getCreateTime;
            default:
                return null;
        }
    }

    @Override
    public QuestionSubmitDetailVO findQuestionSubmitDetailVO(Long id, HttpServletRequest request) {
        QuestionSubmit questionSubmit = this.getById(id);
        // 检查记录是否存在
        if (questionSubmit == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        // 转换为视图对象
        QuestionSubmitDetailVO questionSubmitDetailVO = new QuestionSubmitDetailVO();
        // 使用 BeanUtils.copyProperties 报错
        questionSubmitDetailVO.setId(questionSubmit.getId());
        questionSubmitDetailVO.setLanguage(questionSubmit.getLanguage());
        questionSubmitDetailVO.setCode(questionSubmit.getCode());
        questionSubmitDetailVO.setStatus(questionSubmit.getStatus());
        questionSubmitDetailVO.setCreateTime(questionSubmit.getCreateTime());

        // 查询题目
        QuestionVO questionVO = questionService.findQuestionVOById(questionSubmit.getQuestionId(), request);
        // 查询用户
        User user = userService.getById(questionSubmit.getUserId());
        UserVO userVO = userService.findUserVO(user);
        // 赋值给视图对象中的对应属性
        questionSubmitDetailVO.setQuestionVO(questionVO);
        questionSubmitDetailVO.setUserVO(userVO);
        questionSubmitDetailVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        // 返回视图对象
        return questionSubmitDetailVO;
    }

    @Override
    public Page<QuestionSubmitDetailVO> findQuestionSubmitDetailVOByPage(QuestionSubmitDetailQueryRequest queryRequest, HttpServletRequest request) {
        // 从请求中获取查询参数
        String searchKey = queryRequest.getSearchKey();
        String judgeInfoResult = queryRequest.getJudgeInfoResult();
        List<String> languageList = queryRequest.getLanguageList();
        Integer status = queryRequest.getStatus();
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();
        String sort = queryRequest.getSort();
        String order = queryRequest.getOrder();

        // 构建题目提交查询Wrapper
        LambdaQueryWrapper<QuestionSubmit> questionSubmitWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(languageList)) {
            // 如果语言列表非空，添加语言过滤条件
            for (String language : languageList) {
                questionSubmitWrapper.eq(StringUtils.isNotEmpty(language), QuestionSubmit::getLanguage, language);
            }
        }
        // 状态条件过滤
        questionSubmitWrapper.eq(ObjectUtil.isNotEmpty(status), QuestionSubmit::getStatus, status);
        // 判题结果过滤
        questionSubmitWrapper.like(StringUtils.isNotEmpty(judgeInfoResult), QuestionSubmit::getJudgeInfo, judgeInfoResult);

        // 排序条件
        if (SortOrderConstant.SORT_ORDER_ASC.equals(order)) {
            questionSubmitWrapper.orderByAsc(getSortField(sort));
        } else if (SortOrderConstant.SORT_ORDER_DESC.equals(order)) {
            questionSubmitWrapper.orderByDesc(getSortField(sort));
        }

        // 用户和题目的查询Wrapper
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();

        List<Question> questionList;
        if (StringUtils.isNotEmpty(searchKey)) {
            // 如果搜索关键字非空，进行相关搜索
            if (NumberUtil.isNumber(searchKey)) {
                // 如果是数字，则按题目ID进行过滤
                questionSubmitWrapper.like(QuestionSubmit::getQuestionId, Long.parseLong(searchKey));
            }

            // 按用户名搜索
            userWrapper.like(User::getUsername, searchKey);
            List<User> userList = userService.list(userWrapper);
            if (!userList.isEmpty()) {
                // 如果找到用户
                for (User user : userList) {
                    questionSubmitWrapper.eq(QuestionSubmit::getUserId, user.getId());
                }
            }

            // 按题目标题搜索
            questionWrapper.like(Question::getTitle, searchKey);
            questionList = questionService.list(questionWrapper);
            if (!questionList.isEmpty()) {
                // 如果找到题目
                for (Question question : questionList) {
                    questionSubmitWrapper.eq(QuestionSubmit::getQuestionId, question.getId());
                }
            }
        }

        // 分页查询题目提交记录
        Page<QuestionSubmit> questionSubmitPage = this.page(new Page<>(page, size), questionSubmitWrapper);
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitDetailVO> questionSubmitDetailVOPage = new Page<>(page, size, questionSubmitPage.getTotal());

        if (questionSubmitList.isEmpty()) {
            // 如果未查到记录，直接返回空记录的分页对象
            questionSubmitDetailVOPage.setRecords(new ArrayList<>());
            return questionSubmitDetailVOPage;
        }

        List<QuestionSubmitDetailVO> questionSubmitDetailVOList = new ArrayList<>();
        for (QuestionSubmit questionSubmit : questionSubmitList) {
            // 为每条记录构建视图对象
            QuestionSubmitDetailVO questionSubmitDetailVO = new QuestionSubmitDetailVO();
            BeanUtils.copyProperties(questionSubmit, questionSubmitDetailVO);

            questionSubmitDetailVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
            questionSubmitDetailVO.setQuestionVO(questionService.findQuestionVOById(questionSubmit.getQuestionId(), request));

            User user = userService.getById(questionSubmit.getUserId());
            questionSubmitDetailVO.setUserVO(userService.findUserVO(user));

            questionSubmitDetailVOList.add(questionSubmitDetailVO);
        }
        questionSubmitDetailVOPage.setRecords(questionSubmitDetailVOList);
        return questionSubmitDetailVOPage;
    }

    @Override
    public List<QuestionSubmitVO> findQuestionSubmitVOByQuestionId(Long questionId, HttpServletRequest request) {
        LambdaQueryWrapper<QuestionSubmit> questionSubmitWrapper = new LambdaQueryWrapper<>();
        questionSubmitWrapper.eq(QuestionSubmit::getQuestionId, questionId);
        // 查询用户自己的答题记录
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return new ArrayList<>();
        }
        questionSubmitWrapper.eq(QuestionSubmit::getUserId, loginUser.getId());
        questionSubmitWrapper.orderByDesc(QuestionSubmit::getCreateTime);
        List<QuestionSubmit> questionSubmitList = this.list(questionSubmitWrapper);
        // 转换为视图对象
        return questionSubmitList.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
            questionSubmitVO.setLanguage(questionSubmit.getLanguage());
            questionSubmitVO.setCode(questionSubmit.getCode());
            questionSubmitVO.setStatus(questionSubmit.getStatus());
            questionSubmitVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
            questionSubmitVO.setQuestionId(questionSubmit.getQuestionId());
            questionSubmitVO.setUserId(questionSubmit.getUserId());
            questionSubmitVO.setCreateTime(questionSubmit.getCreateTime());
            return questionSubmitVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserRankingVO> findTopRankedUsers() {
        // 尝试从 Redis 缓存中获取排行榜数据
        List<UserRankingVO> cachedRankings = redisTemplate.opsForValue().get(LEADERBOARD_CACHE_KEY);
        if (!CollectionUtils.isEmpty(cachedRankings)) {
            return cachedRankings;
        }

        // 获取所有用户的提交记录
        List<QuestionSubmit> allSubmissions = questionSubmitService.list();

        // 计算每个用户的通过率
        List<UserRankingVO> rankings = getRankings(allSubmissions);

        // 计算排名
        rankings.sort(Comparator.comparingDouble(UserRankingVO::getAcceptanceRate).reversed());

        // 查询用户信息
        List<UserRankingVO> result = rankings.size() > 20 ? rankings.subList(0, 20) : rankings;
        Set<Long> userIds = result.stream()
                .map(UserRankingVO::getId)
                .collect(Collectors.toSet());
        List<User> userList = userService.lambdaQuery()
                .in(User::getId, userIds)
                .list();
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        result.forEach(user -> {
            User tempUser  = userMap.get(user.getId());
            BeanUtils.copyProperties(tempUser , user);
        });

        // 将结果存入 Redis 缓存，设置过期时间为 10 分钟
        redisTemplate.opsForValue().set(LEADERBOARD_CACHE_KEY, result, 10, TimeUnit.MINUTES);

        return result;
    }

    @NotNull
    private static List<UserRankingVO> getRankings(List<QuestionSubmit> allSubmissions) {
        Map<Long, UserRankingVO> userRankingMap = new HashMap<>();
        for (QuestionSubmit submission : allSubmissions) {
            if (!userRankingMap.containsKey(submission.getUserId())) {
                UserRankingVO userRankingVO = new UserRankingVO();
                userRankingVO.setId(submission.getUserId());
                userRankingMap.put(submission.getUserId(), userRankingVO);
            }
            UserRankingVO userRanking = userRankingMap.get(submission.getUserId());
            userRanking.incrementTotalSubmissions();
            if (QuestionSubmitStatusEnum.SUCCEED.equalsValue(submission.getStatus())) {
                userRanking.incrementAcceptedSubmissions();
            }
        }

        return new ArrayList<>(userRankingMap.values());
    }
}
