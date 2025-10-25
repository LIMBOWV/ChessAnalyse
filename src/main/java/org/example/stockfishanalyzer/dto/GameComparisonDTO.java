package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 棋局对比分析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameComparisonDTO {
    
    // 棋局1的基本信息
    private GameInfo game1;
    
    // 棋局2的基本信息
    private GameInfo game2;
    
    // 对比统计
    private ComparisonStats stats;
    
    // 评分曲线数据（用于图表）
    private List<ScorePoint> game1Scores;
    private List<ScorePoint> game2Scores;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameInfo {
        private Long gameId;
        private String whitePlayer;
        private String blackPlayer;
        private String gameResult;
        private String gameDate;
        private String pgnContent;
        private Double avgAccuracy;      // 平均精准度
        private Integer totalMoves;      // 总步数
        private Integer blunders;        // 漏着数
        private Integer mistakes;        // 失误数
        private Integer inaccuracies;    // 不精准数
        private Integer brilliantMoves;  // 妙手数
        private Integer bestMoves;       // 最佳走法数
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonStats {
        // 精准度对比
        private Double accuracyDiff;     // 精准度差值（game1 - game2）
        private String betterAccuracy;   // 哪局精准度更高
        
        // 走法质量对比
        private Integer totalMovesGame1;
        private Integer totalMovesGame2;
        private Integer blundersDiff;
        private Integer mistakesDiff;
        private Integer brilliantDiff;
        
        // 结论
        private String conclusion;       // 对比结论
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScorePoint {
        private Integer moveNumber;      // 步数
        private Double score;            // 评分（厘兵）
    }
}
