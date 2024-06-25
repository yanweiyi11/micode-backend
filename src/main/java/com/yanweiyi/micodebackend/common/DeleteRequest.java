package com.yanweiyi.micodebackend.common;

import lombok.Data;

/**
 * 通用删除请求
 *
 * @author yanweiyi
 */
@Data
public class DeleteRequest {

    /**
     * 要删除的记录 id
     */
    private Long id;

}
