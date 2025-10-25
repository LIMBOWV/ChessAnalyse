package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 带棋局列表的标签DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagWithGamesDTO {
    
    private Long id;
    private String tagName;
    private String tagColor;
    private Integer gameCount;
    
    // 关联的棋局列表（简要信息）
    private List<GameSummary> games;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSummary {
        private Long gameId;
        private String whitePlayer;
        private String blackPlayer;
        private String gameResult;
        private String gameDate;
    }
}
