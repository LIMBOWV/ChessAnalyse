package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByGameIdOrderByMoveNumberAsc(Long gameId);

    Optional<AnalysisResult> findByGameIdAndMoveNumber(Long gameId, Integer moveNumber);

    boolean existsByGameId(Long gameId);
}
