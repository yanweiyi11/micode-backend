package com.yanweiyi.micodebackend.common;

import com.yanweiyi.micodebackend.constant.SortOrderConstant;
import lombok.Data;

/**
 * 通用分页参数
 *
 * @author yanweiyi
 */
@Data
public class PageRequest {

    /**
     * 当前页（默认值 0）
     */
    private int page = 0;

    /**
     * 页面大小（默认值 10）
     */
    private int size = 10;

    /**
     * 排序字段（默认值 更新时间）
     */
    private String sort = "updateTime";

    /**
     * 排序方式（默认值 降序排序）
     */
    private String order = SortOrderConstant.SORT_ORDER_DESC;

}
