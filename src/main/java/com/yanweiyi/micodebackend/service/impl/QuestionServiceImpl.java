package com.yanweiyi.micodebackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.constant.SortOrderConstant;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeCase;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeConfig;
import com.yanweiyi.micodebackend.judge.model.dto.JudgeInfo;
import com.yanweiyi.micodebackend.judge.model.enums.JudgeInfoMessageEnum;
import com.yanweiyi.micodebackend.mapper.QuestionMapper;
import com.yanweiyi.micodebackend.model.dto.question.QuestionDetailQueryRequest;
import com.yanweiyi.micodebackend.model.dto.question.QuestionQueryRequest;
import com.yanweiyi.micodebackend.model.entity.Question;
import com.yanweiyi.micodebackend.model.entity.QuestionDetail;
import com.yanweiyi.micodebackend.model.entity.QuestionSubmit;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.enums.QuestionSubmitStatusEnum;
import com.yanweiyi.micodebackend.model.vo.QuestionDetailVO;
import com.yanweiyi.micodebackend.model.vo.QuestionVO;
import com.yanweiyi.micodebackend.service.QuestionService;
import com.yanweiyi.micodebackend.service.QuestionSubmitService;
import com.yanweiyi.micodebackend.service.QuestionTagService;
import com.yanweiyi.micodebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
    private QuestionMapper questionMapper;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private QuestionTagService questionTagService;

    @Override
    public QuestionDetailVO findQuestionDetailVO(Long id, HttpServletRequest request) {
        // 使用 DB 类查询数据库
        QuestionDetail questionDetail = questionMapper.selectQuestionDetailById(id);
        if (questionDetail == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "题目不存在");
        }
        // DB 类转换为 VO 类
        QuestionDetailVO questionDetailVO = QuestionDetail.toQuestionDetailVO(questionDetail);
        // 检查用户是否为管理员，如果是就直接返回，否则检查用户是否以答对题目，如果未完成就不返回答案
        if (userService.isAdmin(request)) {
            return questionDetailVO;
        }
        JudgeInfo judgeInfo = JSONUtil.toBean(questionDetail.getJudgeInfoStr(), JudgeInfo.class);
        if (!JudgeInfoMessageEnum.ACCEPTED.equalsValue(judgeInfo.getResult())) {
            questionDetailVO.setAnswer(null);
        }
        return questionDetailVO;
    }

    @Override
    public Page<QuestionDetailVO> findQuestionDetailVOByPage(QuestionDetailQueryRequest queryRequest, HttpServletRequest request) {
        // 初始化分页对象
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();
        Page<QuestionDetail> questionDetailPage = new Page<>(page, size);

        // 执行分页查询
        IPage<QuestionDetail> questionDetailIPage = questionMapper.selectQuestionDetailByPage(questionDetailPage, queryRequest);

        // 获取当前登录用户
        User loginUser = userService.getLoginUserNotErr(request);

        // 对查询结果进行后处理
        List<QuestionDetail> questionDetailList = questionDetailIPage.getRecords();
        if (!userService.isAdmin(request)) {
            // 对非管理用户未通过判题的题目隐藏答案
            questionDetailList.forEach(questionDetail -> {
                if (loginUser != null) {
                    JudgeInfo judgeInfo = JSONUtil.toBean(questionDetail.getJudgeInfoStr(), JudgeInfo.class);
                    if (!JudgeInfoMessageEnum.ACCEPTED.equalsValue(judgeInfo.getResult())) {
                        questionDetail.setAnswer(null);
                    }
                } else {
                    questionDetail.setAnswer(null);
                }
            });
        }

        // 转换为视图对象并设置分页信息
        Page<QuestionDetailVO> questionDetailVOPage = new Page<>(page, size, questionDetailIPage.getTotal());
        questionDetailVOPage.setRecords(QuestionDetail.toQuestionDetailVOList(questionDetailList));

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
            questionVO.setId(question.getId());
            questionVO.setTitle(question.getTitle());
            questionVO.setContent(question.getContent());
            questionVO.setAnswer(question.getAnswer());
            questionVO.setDifficulty(question.getDifficulty());
            questionVO.setSubmitNum(question.getSubmitNum());
            questionVO.setAcceptedNum(question.getAcceptedNum());
            questionVO.setUserId(question.getUserId());
            questionVO.setCreateTime(question.getCreateTime());

            // 将标签的JSON字符串转换为列表
            questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
            // 将判题案例的JSON字符串转换为对象
            questionVO.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
            // 将判题配置的JSON字符串转换为对象
            questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
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
        Long userId = loginUser.getId(); // 当前登录用户ID
        Long creatorId = question.getUserId(); // 题目创建者ID

        // 构建查询条件，查找当前用户对该题目的提交记录，且状态为成功
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionSubmit::getUserId, userId).eq(QuestionSubmit::getQuestionId, id).eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.SUCCEED.getValue());

        List<QuestionSubmit> questionSubmitList = questionSubmitService.list(queryWrapper);

        // 检查访问权限：题目创建者、管理员、成功完成题目的用户可以访问
        if (!creatorId.equals(userId) && !userService.isAdmin(loginUser) && questionSubmitList.isEmpty()) {
            // 如果不满足以上任一条件，则抛出无权限错误
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        // 如果验证通过，返回查询到的题目信息，并转换成视图对象
        QuestionVO questionVO = new QuestionVO();
        questionVO.setId(question.getId());
        questionVO.setTitle(question.getTitle());
        questionVO.setContent(question.getContent());
        questionVO.setAnswer(question.getAnswer());
        questionVO.setDifficulty(question.getDifficulty());
        questionVO.setSubmitNum(question.getSubmitNum());
        questionVO.setAcceptedNum(question.getAcceptedNum());
        questionVO.setUserId(question.getUserId());
        questionVO.setCreateTime(question.getCreateTime());
        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        questionVO.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
        questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
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




