package com.yanweiyi.micodebackend.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yanweiyi
 */
@Data
public class UserVO {

    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 性别（0-男，1-女）
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色（user / admin / ban）
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

}
