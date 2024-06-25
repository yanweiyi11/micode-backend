package com.yanweiyi.micodebackend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author yanweiyi
 */
@Data
public class UserTagVO {
    /**
     * 用户标签编号
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 创建此标签的用户编号
     */
    private Long userId;

    /**
     * 父标签编号
     */
    private Long parentId;

    /**
     * 是否为父标签（0-不是父标签，1-是父标签）
     */
    private Integer isParent;

    /**
     * 创建时间
     */
    private Date createTime;
}