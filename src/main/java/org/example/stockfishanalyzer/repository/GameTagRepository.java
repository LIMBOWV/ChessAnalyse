package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.GameTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 棋局标签数据访问接口
 */
@Repository
public interface GameTagRepository extends JpaRepository<GameTag, Long> {

    /**
     * 查找用户的所有标签
     */
    List<GameTag> findByUserId(Long userId);

    /**
     * 根据用户 ID 和标签名称查找
     */
    Optional<GameTag> findByUserIdAndTagName(Long userId, String tagName);

    /**
     * 检查用户是否已有该标签名称
     */
    boolean existsByUserIdAndTagName(Long userId, String tagName);

    /**
     * 删除用户的所有标签
     */
    void deleteByUserId(Long userId);
}

