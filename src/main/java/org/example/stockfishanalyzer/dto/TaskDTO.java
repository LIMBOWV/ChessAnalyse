package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private Long userId;
    private String taskTitle;
    private String taskType;  // study_opening, analyze_game, practice
    private String priority;  // high, medium, low
    private String status;    // pending, in_progress, completed
    private Long relatedGameId;
    private LocalDate targetDate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // 关联棋局信息（如果有）
    private String relatedGameTitle;  // 例如：白方 vs 黑方
}
