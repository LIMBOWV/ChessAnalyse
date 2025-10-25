package org.example.stockfishanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {
    private String theme;
    private String language;
    private String boardTheme;
    private String pieceSet;
    private Integer analysisDepth;
    private Integer engineThreads;
    private Boolean notificationsEnabled;
    private Boolean autoAnalyze;
}
