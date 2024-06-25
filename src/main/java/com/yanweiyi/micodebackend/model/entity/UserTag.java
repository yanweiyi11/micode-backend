package com.yanweiyi.micodebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户标签表
 *
 * @TableName user_tag
 */
@Data
@TableName(value = "user_tag")
public class UserTag implements Serializable {
    /**
     * 用户标签编号
     */
    @TableId(type = IdType.AUTO)
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
     * 是否删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Boolean isParentBool() {
        if (this.isParent == 0) {
            return false;
        } else if (this.isParent == 1) {
            return true;
        }
        throw new RuntimeException("The isParent parameter is illegal");
    }
}