package com.yanweiyi.micodebackend.model.dto.user;

import com.yanweiyi.micodebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yanweiyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest {
    /**
     * 通用搜索词（搜索用户名、编号、个人介绍）
     */
    private String searchKey;

    /**
     * 性别（0-男，1-女）
     */
    private Integer gender;

    /**
     * 标签（Json 数组）
     */
    private List<String> tags;

    /**
     * 用户角色（user / admin / ban）
     */
    private String userRole;

}
