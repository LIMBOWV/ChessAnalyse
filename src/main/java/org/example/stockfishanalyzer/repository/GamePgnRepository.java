package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePgnRepository extends JpaRepository<GamePgn, Long> {

    List<GamePgn> findByUserId(Long userId);

    List<GamePgn> findByAnalysisStatus(AnalysisStatus status);

    List<GamePgn> findByUserIdOrderByUploadedAtDesc(Long userId);

    // 统计相关方法
    Long countByUserId(Long userId);

    Integer countByUserIdAndGameResultContaining(Long userId, String gameResult);

    @Query("SELECT g.id FROM GamePgn g WHERE g.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);
}
