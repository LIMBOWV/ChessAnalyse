package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户统计数据访问接口
 */
@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    /**
     * 根据用户 ID 查找统计信息
     */
    Optional<UserStatistics> findByUserId(Long userId);

    /**
     * 检查用户是否已有统计记录
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据用户 ID 删除统计信息
     */
    void deleteByUserId(Long userId);
}

