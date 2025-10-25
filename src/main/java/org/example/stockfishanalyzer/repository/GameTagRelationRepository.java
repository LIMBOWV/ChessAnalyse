package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.GameTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 棋局标签关联数据访问接口
 */
@Repository
public interface GameTagRelationRepository extends JpaRepository<GameTagRelation, Long> {

    /**
     * 查找棋局的所有标签关联
     */
    List<GameTagRelation> findByGameId(Long gameId);

    /**
     * 查找标签关联的所有棋局
     */
    List<GameTagRelation> findByTagId(Long tagId);

    /**
     * 检查棋局是否已有该标签
     */
    boolean existsByGameIdAndTagId(Long gameId, Long tagId);

    /**
     * 删除棋局的某个标签
     */
    void deleteByGameIdAndTagId(Long gameId, Long tagId);

    /**
     * 删除棋局的所有标签
     */
    void deleteByGameId(Long gameId);

    /**
     * 删除某个标签的所有关联
     */
    void deleteByTagId(Long tagId);
}

