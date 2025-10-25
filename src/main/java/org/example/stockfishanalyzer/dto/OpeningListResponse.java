package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 开局分析列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningListResponse {

    private Long userId;
    private Integer totalOpenings;          // 总开局数

    private List<OpeningStatsDto> allOpenings;     // 所有开局
    private List<OpeningStatsDto> topOpenings;     // 最擅长的开局 Top 3
    private List<OpeningStatsDto> weakOpenings;    // 最薄弱的开局 Top 3
}

