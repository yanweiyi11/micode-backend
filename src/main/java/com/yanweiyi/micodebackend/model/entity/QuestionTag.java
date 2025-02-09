package com.yanweiyi.micodebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目标签表
 *
 * @TableName question_tag
 */
@Data
@TableName(value = "question_tag")
public class QuestionTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 父标签id
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