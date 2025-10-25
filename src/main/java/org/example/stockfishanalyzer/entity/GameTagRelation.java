package org.example.stockfishanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 棋局标签关联实体类
 * 多对多关系中间表：一个棋局可以有多个标签，一个标签可以关联多个棋局
 */
@Entity
@Table(name = "tbl_game_tag_relation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "tag_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTagRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联棋局 ID
     */
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    /**
     * 关联标签 ID
     */
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}

