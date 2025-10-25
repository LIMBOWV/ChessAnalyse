package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 历史趋势分析响应 DTO
 * 用于返回用户的历史趋势数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendsAnalysisResponse {
    
    private Long userId;
    private String period;  // "month", "quarter", "year"
    
    // 精准度趋势
    private AccuracyTrend accuracyTrend;
    
    // 胜率趋势
    private WinRateTrend winRateTrend;
    
    // 对局频率
    private GameFrequency gameFrequency;
    
    // 走法质量雷达图数据
    private MoveQualityRadar moveQualityRadar;
    
    // 关键指标
    private KeyMetrics keyMetrics;
    
    /**
     * 精准度趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccuracyTrend {
        private List<String> labels;           // 时间标签 ["2025-01", "2025-02", ...]
        private List<BigDecimal> accuracyData; // 精准度数据 [85.5, 87.2, ...]
    }
    
    /**
     * 胜率趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinRateTrend {
        private List<String> labels;        // 时间标签
        private List<Integer> winCounts;    // 胜利场数
        private List<Integer> drawCounts;   // 平局场数
        private List<Integer> lossCounts;   // 失败场数
        private List<BigDecimal> winRates;  // 胜率 (%)
    }
    
    /**
     * 对局频率数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameFrequency {
        private List<String> labels;        // 时间标签
        private List<Integer> gameCounts;   // 对局数量
    }
    
    /**
     * 走法质量雷达图数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveQualityRadar {
        private List<String> indicators;    // ["妙手", "好棋", "不精准", "失误", "漏着"]
        private List<BigDecimal> values;    // 各类走法的占比 [5.2, 45.3, 30.1, 15.2, 4.2]
    }
    
    /**
     * 关键指标卡片
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMetrics {
        private PeriodMetric bestPeriod;      // 最佳表现期
        private PeriodMetric worstPeriod;     // 最差表现期
        private ImprovementMetric improvement; // 进步幅度
    }
    
    /**
     * 单期指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodMetric {
        private String period;              // 期间 "2025-03"
        private BigDecimal winRate;         // 胜率
        private BigDecimal accuracy;        // 精准度
        private Integer totalGames;         // 总对局数
    }
    
    /**
     * 进步幅度指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImprovementMetric {
        private BigDecimal accuracyChange;   // 精准度变化 (%)
        private BigDecimal winRateChange;    // 胜率变化 (%)
        private String trend;                // "improving", "stable", "declining"
    }
}
