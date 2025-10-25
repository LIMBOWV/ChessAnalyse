package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsDto {

    private Long id;
    private Long userId;

    // 对局统计
    private Integer totalGames;
    private Integer winCount;
    private Integer drawCount;
    private Integer lossCount;

    // 计算字段（使用 Double 与服务实现兼容）
    private Double winRate;        // 胜率 %
    private Double drawRate;       // 和局率 %
    private Double lossRate;       // 负率 %

    // 准确度统计
    private BigDecimal avgAccuracy;

    // 走法分类统计
    private Integer totalBrilliants;
    private Integer totalGoods;
    private Integer totalMistakes;
    private Integer totalBlunders;

    // 计算字段：总走法数
    private Integer totalMoves;
    private Double brilliantRate;  // 妙手率 %
    private Double goodRate;       // 好棋率 %
    private Double mistakeRate;    // 失误率 %
    private Double blunderRate;    // 漏着率 %

    // 开局信息
    private Long favoriteOpeningId;
    private String favoriteOpeningName;

    // 时间信息
    private LocalDateTime lastUpdated;
}
