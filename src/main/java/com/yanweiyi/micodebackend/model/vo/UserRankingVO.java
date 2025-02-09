package com.yanweiyi.micodebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingVO extends UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交总数
     */
    private int totalSubmissions;

    /**
     * 通过数
     */
    private int acceptedSubmissions;

    /**
     * 增加总提交数
     */
    public void incrementTotalSubmissions() {
        this.totalSubmissions++;
    }

    /**
     * 增加通过数
     */
    public void incrementAcceptedSubmissions() {
        this.acceptedSubmissions++;
    }

    /**
     * 计算通过率
     */
    public double getAcceptanceRate() {
        return totalSubmissions == 0 ? 0 : (double) acceptedSubmissions / totalSubmissions;
    }
}