package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    
    private Long id;
    private Long userId;
    private String tagName;
    private String tagColor;
    private LocalDateTime createdAt;
    
    // 使用该标签的棋局数量
    private Integer gameCount;
}
