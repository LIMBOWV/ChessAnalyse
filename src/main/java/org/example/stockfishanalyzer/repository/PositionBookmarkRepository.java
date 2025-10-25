package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.PositionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 局面书签数据访问接口
 */
@Repository
public interface PositionBookmarkRepository extends JpaRepository<PositionBookmark, Long> {

    /**
     * 查找用户的所有书签
     */
    List<PositionBookmark> findByUserId(Long userId);

    /**
     * 查找用户在某个棋局的所有书签
     */
    List<PositionBookmark> findByUserIdAndGameId(Long userId, Long gameId);

    /**
     * 查找某个棋局的所有书签
     */
    List<PositionBookmark> findByGameId(Long gameId);

    /**
     * 删除用户的所有书签
     */
    void deleteByUserId(Long userId);

    /**
     * 删除棋局的所有书签
     */
    void deleteByGameId(Long gameId);
}

