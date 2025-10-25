package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDTO {
    private Long id;
    private Long userId;
    private String theme;           // light, dark
    private String language;        // zh-CN, en-US
    private String boardTheme;      // brown, blue, green, gray
    private String pieceSet;        // default, alpha, merida
    private Integer analysisDepth;  // 10-30
    private Integer engineThreads;  // 1-8
    private Boolean notificationsEnabled;
    private Boolean autoAnalyze;
}
