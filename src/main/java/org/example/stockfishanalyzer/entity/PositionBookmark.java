package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 局面书签实体类
 * 收藏对局中的关键局面，方便后续学习和复盘
 */
@Entity
@Table(name = "tbl_position_bookmark")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联棋局 ID
     */
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    /**
     * 关联用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 收藏的步数
     */
    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    /**
     * 局面 FEN
     */
    @Column(name = "fen_position", nullable = false, length = 100)
    private String fenPosition;

    /**
     * 用户备注
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

