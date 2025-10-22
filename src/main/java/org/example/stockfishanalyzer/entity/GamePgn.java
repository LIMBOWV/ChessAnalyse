package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockfishanalyzer.enums.AnalysisStatus;

import java.time.LocalDateTime;

/**
 * 棋局 PGN 实体类
 */
@Entity
@Table(name = "tbl_game_pgn", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamePgn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pgn_content", nullable = false, columnDefinition = "TEXT")
    private String pgnContent;

    @Column(name = "white_player", length = 100)
    private String whitePlayer = "Unknown";

    @Column(name = "black_player", length = 100)
    private String blackPlayer = "Unknown";

    @Column(name = "game_result", length = 20)
    private String gameResult = "*";

    @Column(name = "game_date", length = 50)
    private String gameDate = "????.??.??";

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
