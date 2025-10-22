package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePgnRepository extends JpaRepository<GamePgn, Long> {

    List<GamePgn> findByUserId(Long userId);

    List<GamePgn> findByAnalysisStatus(AnalysisStatus status);

    List<GamePgn> findByUserIdOrderByUploadedAtDesc(Long userId);
}
