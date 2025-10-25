package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockfishanalyzer.enums.MoveClassification;

import java.util.List;
import java.util.Map;

/**
 * 失误列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MistakeListResponse {

    private Long userId;
    private Integer totalMistakes;

    // 按类型分组统计
    private Map<MoveClassification, Integer> mistakesByType;

    // 失误列表
    private List<MistakeStatsDto> mistakes;

    // 统计摘要
    private Integer totalBlunders;
    private Integer totalMistakesCount;      // MISTAKE类型的数量
    private Integer totalInaccuracies;
}
