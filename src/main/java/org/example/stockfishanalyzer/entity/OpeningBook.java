package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 开局库实体类
 * 存储国际象棋常见开局定式，支持开局识别与分析
 */
@Entity
@Table(name = "tbl_opening_book")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpeningBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ECO 代码（Encyclopedia of Chess Openings）
     * 例如：C50, E60, B12
     */
    @Column(name = "eco_code", unique = true, length = 10)
    private String ecoCode;

    /**
     * 开局名称
     * 例如：Italian Game, Sicilian Defense
     */
    @Column(name = "opening_name", nullable = false, length = 100)
    private String openingName;

    /**
     * 开局变种
     * 例如：Giuoco Piano, Najdorf Variation
     */
    @Column(name = "variation_name", length = 100)
    private String variationName;

    /**
     * UCI 格式走法序列
     * 例如：e2e4 e7e5 g1f3 b8c6
     */
    @Column(name = "moves_uci", columnDefinition = "TEXT")
    private String movesUci;

    /**
     * SAN 格式走法序列（人类可读）
     * 例如：1. e4 e5 2. Nf3 Nc6
     */
    @Column(name = "moves_san", columnDefinition = "TEXT")
    private String movesSan;

    /**
     * 开局后的局面 FEN
     */
    @Column(name = "fen_position", length = 100)
    private String fenPosition;

    /**
     * 使用频率/流行度
     * 用于统计和排序
     */
    @Column(name = "popularity", nullable = false)
    private Integer popularity = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

