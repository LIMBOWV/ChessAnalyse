package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "theme", length = 20)
    private String theme = "light";  // light, dark
    
    @Column(name = "language", length = 10)
    private String language = "zh-CN";  // zh-CN, en-US
    
    @Column(name = "board_theme", length = 50)
    private String boardTheme = "brown";  // brown, blue, green, gray
    
    @Column(name = "piece_set", length = 50)
    private String pieceSet = "default";  // default, alpha, merida
    
    @Column(name = "analysis_depth")
    private Integer analysisDepth = 20;  // 10-30
    
    @Column(name = "engine_threads")
    private Integer engineThreads = 4;  // 1-8
    
    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;
    
    @Column(name = "auto_analyze")
    private Boolean autoAnalyze = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
