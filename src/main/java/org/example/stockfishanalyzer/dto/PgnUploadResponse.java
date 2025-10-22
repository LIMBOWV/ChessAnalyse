package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PGN 上传响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PgnUploadResponse {
    private Long gameId;
    private String message;
    private Integer totalMoves;
}
