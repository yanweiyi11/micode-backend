package com.yanweiyi.micodebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @TableName user
 */
@Data
@TableName(value = "user")
public class User implements Serializable {
    /**
     * 用户编号
     * -- GETTER --
     * 用户编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     * -- GETTER --
     * 用户名
     */
    private String username;

    /**
     * 密码
     * -- GETTER --
     * 密码
     */
    private String password;

    /**
     * 头像
     * -- GETTER --
     * 头像
     */
    private String avatarUrl;

    /**
     * 用户简介
     * -- GETTER --
     * 用户简介
     */
    private String userProfile;

    /**
     * 性别（0-男，1-女）
     * -- GETTER --
     * 性别（0-男，1-女）
     */
    private Integer gender;

    /**
     * 邮箱
     * -- GETTER --
     * 邮箱
     */
    private String email;

    /**
     * 标签（Json 数组）
     * -- GETTER --
     * 标签（Json 数组）
     */
    private String tags;

    /**
     * 用户角色（user / admin / ban）
     * -- GETTER --
     * 用户角色（user / admin / ban）
     */
    private String userRole;

    /**
     * 创建时间
     * -- GETTER --
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     * -- GETTER --
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     * -- GETTER --
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}