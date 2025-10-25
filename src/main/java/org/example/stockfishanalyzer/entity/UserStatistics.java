package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户统计实体类
 * 聚合用户的对局数据，支持数据可视化和分析
 */
@Entity
@Table(name = "tbl_user_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联用户（一对一关系）
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 总对局数
     */
    @Column(name = "total_games", nullable = false)
    private Integer totalGames = 0;

    /**
     * 胜局数
     */
    @Column(name = "win_count", nullable = false)
    private Integer winCount = 0;

    /**
     * 和局数
     */
    @Column(name = "draw_count", nullable = false)
    private Integer drawCount = 0;

    /**
     * 负局数
     */
    @Column(name = "loss_count", nullable = false)
    private Integer lossCount = 0;

    /**
     * 平均准确度（百分比）
     */
    @Column(name = "avg_accuracy", precision = 5, scale = 2)
    private BigDecimal avgAccuracy = BigDecimal.ZERO;

    /**
     * 总妙手数
     */
    @Column(name = "total_brilliants", nullable = false)
    private Integer totalBrilliants = 0;

    /**
     * 总好棋数
     */
    @Column(name = "total_goods", nullable = false)
    private Integer totalGoods = 0;

    /**
     * 总失误数
     */
    @Column(name = "total_mistakes", nullable = false)
    private Integer totalMistakes = 0;

    /**
     * 总漏着数
     */
    @Column(name = "total_blunders", nullable = false)
    private Integer totalBlunders = 0;

    /**
     * 最擅长的开局 ID
     */
    @Column(name = "favorite_opening_id")
    private Long favoriteOpeningId;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

