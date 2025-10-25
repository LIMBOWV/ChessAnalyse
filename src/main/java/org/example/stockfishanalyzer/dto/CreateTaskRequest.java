package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    private String taskTitle;
    private String taskType;     // study_opening, analyze_game, practice
    private String priority;     // high, medium, low
    private Long relatedGameId;  // 可选
    private LocalDate targetDate;
    private String description;
}
