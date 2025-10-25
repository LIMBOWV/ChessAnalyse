package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsDTO {
    private int totalTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int todayTasks;
    private int weekTasks;
    private double weekCompletionRate;
    
    // 优先级分布
    private int highPriorityTasks;
    private int mediumPriorityTasks;
    private int lowPriorityTasks;
    
    // 类型分布
    private int studyOpeningTasks;
    private int analyzeGameTasks;
    private int practiceTasks;
}
