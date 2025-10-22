package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockfishanalyzer.enums.MoveClassification;

/**
 * 分析结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDto {
    private Integer moveNumber;
    private String moveSan;
    private String score;
    private String bestMove;
    private MoveClassification classification;
}
