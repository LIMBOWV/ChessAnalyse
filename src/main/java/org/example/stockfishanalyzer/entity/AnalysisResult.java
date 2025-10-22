package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockfishanalyzer.enums.MoveClassification;

/**
 * 分析结果实体类
 */
@Entity
@Table(name = "tbl_analysis_result",
       uniqueConstraints = @UniqueConstraint(name = "uk_game_move", columnNames = {"game_id", "move_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    @Column(name = "move_san", nullable = false, length = 20)
    private String moveSan;

    @Column(name = "score", nullable = false, length = 20)
    private String score;

    @Column(name = "best_move", nullable = false, length = 20)
    private String bestMove;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_classification", length = 20)
    private MoveClassification moveClassification;
}
