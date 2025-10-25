package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockfishanalyzer.enums.MoveClassification;

import java.time.LocalDateTime;

/**
 * 失误统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MistakeStatsDto {

    private Long gameId;
    private String whiteName;
    private String blackName;
    private String result;
    private LocalDateTime playedAt;

    // 失误详情
    private Integer moveNumber;
    private MoveClassification classification;
    private String move;                // 实际走法（SAN格式）
    private String bestMove;            // 最佳走法（SAN格式）

    // 评分变化
    private Integer scoreBefore;
    private Integer scoreAfter;
    private Integer scoreDrop;          // 评分损失

    // 局面信息
    private String fenPosition;
    private String openingName;

    // 失误原因分析（可选，未来扩展）
    private String mistakeType;         // 如：战术失误、开局错误、残局失误
}

