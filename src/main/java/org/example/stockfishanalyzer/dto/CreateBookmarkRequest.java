package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建书签请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookmarkRequest {
    
    private Long gameId;
    private Integer moveNumber;
    private String fenPosition;
    private String note;
}
