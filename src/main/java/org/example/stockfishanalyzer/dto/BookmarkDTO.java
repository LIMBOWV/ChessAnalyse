package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 书签数据传输对象
 * 包含书签信息和关联的棋局标题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDTO {
    
    private Long id;
    private Long gameId;
    private Long userId;
    private Integer moveNumber;
    private String fenPosition;
    private String note;
    private LocalDateTime createdAt;
    
    // 关联的棋局标题（从 GamePgn 查询）
    private String gameTitle;
    
    // 开局名称（如果有）
    private String openingName;
}
