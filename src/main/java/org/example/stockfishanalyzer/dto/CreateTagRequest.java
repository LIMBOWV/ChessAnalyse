package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建标签请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {
    
    private String tagName;
    private String tagColor;
}
