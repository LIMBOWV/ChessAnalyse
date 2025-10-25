package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 棋局标签实体类
 * 用户自定义标签，用于分类管理棋局
 */
@Entity
@Table(name = "tbl_game_tag",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tag_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 标签名称
     * 例如：重要比赛、练习局、失误复盘
     */
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    /**
     * 标签颜色（Hex 格式）
     * 例如：#667eea, #f44336
     */
    @Column(name = "tag_color", length = 10)
    private String tagColor = "#667eea";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

