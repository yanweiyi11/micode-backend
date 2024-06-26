package com.yanweiyi.micodebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.constant.SortOrderConstant;
import com.yanweiyi.micodebackend.constant.UserConstant;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.mapper.UserMapper;
import com.yanweiyi.micodebackend.model.dto.user.UserQueryRequest;
import com.yanweiyi.micodebackend.model.dto.user.UserUpdateRequest;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.enums.UserRoleEnum;
import com.yanweiyi.micodebackend.model.vo.UserVO;
import com.yanweiyi.micodebackend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanweiyi
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-05-30 21:59:43
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final List<String> AVATAR_LIST = Arrays.asList("https://nnnu.oss-cn-guangzhou.aliyuncs.com/note/ikun1.png", "https://nnnu.oss-cn-guangzhou.aliyuncs.com/note/ikun2.png", "https://nnnu.oss-cn-guangzhou.aliyuncs.com/note/ikun3.png");

    @Override
    public long userRegister(String username, String password, String checkPassword) {
        if (StringUtils.isAnyBlank(username, password, checkPassword)) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "请填写完整表单");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "两次密码输入不一致");
        }
        if (username.length() < 5) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "用户名长度过短");
        }
        if (password.length() < 6) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "密码长度过短");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User oneUser = this.getOne(queryWrapper);
        if (oneUser != null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "用户名已存在");
        }
        synchronized (username.intern()) {
            int randomInt = RandomUtil.randomInt(0, 3);
            String defaultAvatarUrl = AVATAR_LIST.get(randomInt);
            User user = new User();
            user.setUsername(username);
            user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
            user.setAvatarUrl(defaultAvatarUrl);
            user.setUserRole(UserRoleEnum.USER.getValue());
            boolean isSaved = this.save(user);
            if (!isSaved) {
                throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
            }
            return user.getId();
        }
    }

    @Override
    public UserVO userLogin(String username, String password, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "请填写完整表单");
        }
        if (username.length() < 4) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "用户名长度过短");
        }
        if (password.length() < 4) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "密码长度过短");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getPassword, encryptPassword);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 记住用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.findUserVO(user);
    }

    @Override
    public boolean updateUser(UserUpdateRequest updateRequest, HttpServletRequest request) {
        Long updateId = updateRequest.getId();
        String username = updateRequest.getUsername();
        String password = updateRequest.getPassword();
        String avatarUrl = updateRequest.getAvatarUrl();
        String userProfile = updateRequest.getUserProfile();
        Integer gender = updateRequest.getGender();
        String email = updateRequest.getEmail();
        List<String> tags = updateRequest.getTags();
        String userRole = updateRequest.getUserRole();

        // 仅管理员和本人可更改
        User loginUser = this.getLoginUserOrThrow(request);
        if (!loginUser.getId().equals(updateId) && !this.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }

        if (username.length() < 5) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "用户名长度过短");
        }

        User user = new User();
        BeanUtils.copyProperties(updateRequest, user);

        if (StringUtils.isNotBlank(password)) {
            if (password.length() < 6) {
                throw new BusinessException(ApiStatusCode.PARAMS_ERROR, "密码长度过短");
            }
            user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }
        if (CollectionUtil.isNotEmpty(tags)) {
            user.setTags(JSONUtil.toJsonStr(tags));
        }
        return this.updateById(user);
    }

    /**
     * 检查用户是否登录
     */
    public Boolean checkLogin(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        return loginUser != null && loginUser.getId() != null;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUserOrThrow(HttpServletRequest request) {
        // 从 session 中获取用户对象
        User loginUser = getLoginUser(request);
        // 判断是否已登录
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ApiStatusCode.NOT_LOGIN_ERROR);
        }
        // 从数据库重查用户信息，保证最新用户数据
        loginUser = this.getById(loginUser.getId());
        if (loginUser == null) {
            throw new BusinessException(ApiStatusCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    }

    @Override
    public boolean isAdmin(User user) {
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User loginUser = this.getLoginUserOrThrow(request);
        return UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
    }

    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        String searchKey = queryRequest.getSearchKey();
        Integer gender = queryRequest.getGender();
        List<String> tags = queryRequest.getTags();
        String userRole = queryRequest.getUserRole();
        String sort = queryRequest.getSort();
        String order = queryRequest.getOrder();

        if (StringUtils.isNotBlank(searchKey)) {
            queryWrapper.nested(wrapper -> {
                if (NumberUtil.isNumber(searchKey)) {
                    wrapper.like(User::getId, Long.parseLong(searchKey)).or();
                }
                wrapper.like(User::getUsername, searchKey);
                wrapper.or().like(User::getUserProfile, searchKey);
            });
        }

        queryWrapper.eq(ObjectUtil.isNotNull(gender), User::getGender, gender);
        if (CollectionUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like(User::getTags, tag);
            }
        }
        queryWrapper.eq(StringUtils.isNotBlank(userRole), User::getUserRole, userRole);

        // 根据 sort 和 order 参数排序
        if (SortOrderConstant.SORT_ORDER_ASC.equals(order)) {
            queryWrapper.orderByAsc(getSortField(sort));
        } else if (SortOrderConstant.SORT_ORDER_DESC.equals(order)) {
            queryWrapper.orderByDesc(getSortField(sort));
        }
        return queryWrapper;
    }

    /**
     * 根据传入的 sort 字符串获取对应的排序字段
     */
    private SFunction<User, ?> getSortField(String sort) {
        switch (sort) {
            case "updateTime":
                return User::getUpdateTime;
            case "createTime":
                return User::getCreateTime;
            default:
                return null;
        }
    }

    @Override
    public UserVO findUserVO(User user) {
        if (user == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        String tagsStr = user.getTags();
        if (StringUtils.isNotBlank(tagsStr)) {
            List<String> tagsJson = JSONUtil.toList(JSONUtil.parseArray(tagsStr), String.class);
            userVO.setTags(tagsJson);
        }
        return userVO;
    }

    @Override
    public List<UserVO> findUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::findUserVO).collect(Collectors.toList());
    }

}




