package com.yanweiyi.micodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanweiyi.micodebackend.model.dto.user.UserQueryRequest;
import com.yanweiyi.micodebackend.model.dto.user.UserUpdateRequest;
import com.yanweiyi.micodebackend.model.entity.User;
import com.yanweiyi.micodebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yanweiyi
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-05-30 21:59:43
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String username, String password, String checkPassword);

    /**
     * 用户登录
     */
    UserVO userLogin(String username, String password, HttpServletRequest request);

    /**
     * 将用户对象转换为用户视图对象
     */
    UserVO findUserVO(User user);

    /**
     * 将用户对象列表转换为用户视图对象列表
     */
    List<UserVO> findUserVOList(List<User> userList);

    /**
     * 判断用户是否登录
     */
    Boolean checkLogin(HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户，如果为空，不会报错
     */
    User getLoginUserNotErr(HttpServletRequest request);

    /**
     * 判断当前用户是否为管理员（根据用户对象）
     */
    boolean isAdmin(User user);

    /**
     * 判断当前用户是否为管理员（根据当前 request 对象）
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 根据查询请求构建查询条件
     */
    LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest);

    /**
     * 更新用户
     */
    boolean updateUser(UserUpdateRequest updateRequest, HttpServletRequest request);
}
