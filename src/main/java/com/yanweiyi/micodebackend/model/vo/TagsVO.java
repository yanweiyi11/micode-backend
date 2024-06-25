package com.yanweiyi.micodebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author yanweiyi
 */
@Data
@AllArgsConstructor
public class TagsVO {

    private String parentName;

    private List<String> childNameList;
}
