package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 开局统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningStatsDto {

    private Long openingId;
    private String ecoCode;
    private String openingName;
    private String variationName;

    // 对局统计
    private Integer totalGames;
    private Integer winCount;
    private Integer drawCount;
    private Integer lossCount;

    // 胜率
    private BigDecimal winRate;
    private BigDecimal performance;     // 综合表现分数

    // 准确度
    private BigDecimal avgAccuracy;

    // 走法质量
    private Integer totalBrilliants;
    private Integer totalMistakes;
    private Integer totalBlunders;

    // 排名
    private Integer rank;               // 该开局在用户所有开局中的排名
}

