package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stockfish 分析结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockfishAnalysisResult {

    /**
     * 评分（如 "+120" 表示白方优势 1.2 子，"-50" 表示黑方优势 0.5 子，"M5" 表示 5 步将死）
     */
    private String score;

    /**
     * AI 推荐的最佳走法（UCI 格式，如 e2e4）
     */
    private String bestMove;

    /**
     * 是否为将死局面
     */
    private boolean isMate;

    /**
     * 将死步数（如果是将死局面）
     */
    private Integer mateIn;
}
