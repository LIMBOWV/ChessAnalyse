package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 棋局对比分析响应 DTO
 * 用于返回两个棋局的对比数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCompareResponse {
    
    // 游戏1基本信息
    private GameBasicInfo game1;
    
    // 游戏2基本信息
    private GameBasicInfo game2;
    
    // 对比统计数据
    private CompareStatistics statistics;
    
    // 评分曲线数据 (用于图表)
    private ScoreCurves scoreCurves;
    
    // 关键差异点列表
    private List<KeyDifference> keyDifferences;
    
    /**
     * 游戏基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameBasicInfo {
        private Long gameId;
        private String whiteName;
        private String blackName;
        private String gameResult;
        private String gameDate;
        private String event;
        private Integer totalMoves;
    }
    
    /**
     * 对比统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompareStatistics {
        // 游戏1统计
        private GameStats game1Stats;
        // 游戏2统计
        private GameStats game2Stats;
    }
    
    /**
     * 单个游戏的统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameStats {
        private BigDecimal averageAccuracy;  // 平均精准度
        private Integer brilliantMoves;      // 妙手数
        private Integer goodMoves;           // 好棋数
        private Integer inaccuracies;        // 不精准数
        private Integer mistakes;            // 失误数
        private Integer blunders;            // 漏着数
        private Integer bestMoveRate;        // 最佳走法准确率 (%)
    }
    
    /**
     * 评分曲线数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreCurves {
        private List<Integer> moveNumbers;    // 步数序列 [1, 2, 3, ...]
        private List<Integer> game1Scores;    // 游戏1评分序列
        private List<Integer> game2Scores;    // 游戏2评分序列
    }
    
    /**
     * 关键差异点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyDifference {
        private Integer moveNumber;          // 步数
        private String game1Move;            // 游戏1的走法
        private Integer game1Score;          // 游戏1的评分
        private String game2Move;            // 游戏2的走法
        private Integer game2Score;          // 游戏2的评分
        private Integer scoreDifference;     // 评分差异 (绝对值)
        private String reason;               // 差异原因描述
    }
}
