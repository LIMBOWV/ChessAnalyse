package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "task_title", nullable = false, length = 200)
    private String taskTitle;
    
    @Column(name = "task_type", length = 50)
    private String taskType;  // study_opening, analyze_game, practice
    
    @Column(name = "priority", length = 20)
    private String priority;  // high, medium, low
    
    @Column(name = "status", length = 20)
    private String status;    // pending, in_progress, completed
    
    @Column(name = "related_game_id")
    private Long relatedGameId;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
