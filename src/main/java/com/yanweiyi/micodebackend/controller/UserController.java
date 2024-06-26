package com.yanweiyi.micodebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanweiyi.micodebackend.common.ApiResponse;
import com.yanweiyi.micodebackend.common.ApiStatusCode;
import com.yanweiyi.micodebackend.common.DeleteRequest;
import com.yanweiyi.micodebackend.common.ResultUtils;
import com.yanweiyi.micodebackend.constant.UserConstant;
import com.yanweiyi.micodebackend.exception.BusinessException;
import com.yanweiyi.micodebackend.model.dto.user.*;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.vo.TagsVO;
import com.yanweiyi.micodebackend.model.vo.UserVO;
import com.yanweiyi.micodebackend.service.UserService;
import com.yanweiyi.micodebackend.service.UserTagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yanweiyi
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private UserTagService userTagService;

    @PostMapping("/register")
    public ApiResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        String username = userRegisterRequest.getUsername();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(username, password, checkPassword)) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(username, password, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public ApiResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userLogin(username, password, request);
        return ResultUtils.success(userVO);
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "当前未登录");
        }
        try {
            request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
            return ResultUtils.success(Boolean.TRUE);
        } catch (Exception e) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR, "登出失败");
        }
    }

    @PostMapping("/add")
    public ApiResponse<Long> addUser(@RequestBody UserAddRequest addRequest, HttpServletRequest request) {
        if (addRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUserOrThrow(request);

        // 拷贝数据
        User user = new User();
        BeanUtils.copyProperties(addRequest, user);

        // 处理拷贝不了的 json 数据
        List<String> tagsJson = addRequest.getTags();
        user.setTags(JSONUtil.toJsonStr(tagsJson));

        // 仅管理员可操作
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }

        // 插入数据库
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }
        Long userId = user.getId();
        return ResultUtils.success(userId);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        Long userId = deleteRequest.getId();
        boolean result = userService.removeById(userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> updateUser(@RequestBody UserUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        boolean result = userService.updateUser(updateRequest, request);
        if (!result) {
            throw new BusinessException(ApiStatusCode.OPERATION_ERROR);
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    @PostMapping("/list-page")
    public ApiResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        // 仅管理员可查看
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();
        LambdaQueryWrapper<User> queryWrapper = userService.getQueryWrapper(queryRequest);
        Page<User> userPage = userService.page(new Page<>(page, size), queryWrapper);
        return ResultUtils.success(userPage);
    }

    @PostMapping("/list-vo-page")
    public ApiResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        // 仅管理员可查看
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        int page = queryRequest.getPage();
        int size = queryRequest.getSize();
        LambdaQueryWrapper<User> queryWrapper = userService.getQueryWrapper(queryRequest);
        Page<User> userPage = userService.page(new Page<>(page, size), queryWrapper);
        Page<UserVO> userVOPage = new Page<>(page, size, userPage.getTotal());
        List<UserVO> userVOList = userService.findUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    @GetMapping("/get")
    public ApiResponse<User> getUserById(@RequestParam Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        User queryUser = userService.getById(id);
        if (queryUser == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        // 仅本人和管理员可查看
        User loginUser = userService.getLoginUserOrThrow(request);
        Long loginUserId = loginUser.getId();
        Long queryUserId = queryUser.getId();
        if (!queryUserId.equals(loginUserId) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(queryUser);
    }

    @GetMapping("/get-vo")
    public ApiResponse<UserVO> getUserVOById(@RequestParam Long id) {
        if (id == null) {
            throw new BusinessException(ApiStatusCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ApiStatusCode.NOT_FOUND_ERROR);
        }
        UserVO userVO = userService.findUserVO(user);
        return ResultUtils.success(userVO);
    }

    @GetMapping("/get-tags")
    public ApiResponse<List<TagsVO>> getTags(HttpServletRequest request) {
        // 仅管理员可查看
        if (userService.isAdmin(request)) {
            List<TagsVO> tagsVOList = userTagService.findStructuredTags();
            return ResultUtils.success(tagsVOList);
        }
        throw new BusinessException(ApiStatusCode.NO_AUTH_ERROR);
    }
}
