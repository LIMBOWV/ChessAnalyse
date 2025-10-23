package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.StockfishAnalysisResult;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.AnalysisStatus;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏分析服务
 * 核心职责：协调异步分析流程，整合 Stockfish 引擎和走法分类算法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameAnalysisService {

    private final StockfishService stockfishService;
    private final MoveClassificationService classificationService;
    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * 异步分析棋局
     * 注意：@Async 使得此方法在独立线程池中执行，用户无需等待分析完成
     *
     * @param gameId 棋局 ID
     * @param moves 走法列表（UCI 格式）
     */
    @Async
    @Transactional
    public void analyzeGameAsync(Long gameId, List<String> moves) {
        log.info("开始异步分析棋局 ID: {}, 总步数: {}", gameId, moves.size());

        try {
            log.info("正在查询棋局 ID: {}", gameId);
            // 更新状态为"分析中"
            GamePgn game = gamePgnRepository.findById(gameId)
                    .orElseThrow(() -> new RuntimeException("棋局不存在: " + gameId));

            log.info("查询到棋局，当前状态: {}", game.getAnalysisStatus());
            game.setAnalysisStatus(AnalysisStatus.PROCESSING);
            gamePgnRepository.save(game);
            log.info("已更新状态为 PROCESSING");

            // 检查是否已有分析结果（缓存机制）
            if (analysisResultRepository.existsByGameId(gameId)) {
                log.info("棋局 {} 已有分析结果，跳过重复分析", gameId);
                game.setAnalysisStatus(AnalysisStatus.COMPLETED);
                gamePgnRepository.save(game);
                return;
            }

            log.info("开始逐步分析 {} 步棋", moves.size());
            // 逐步分析每一步棋
            List<AnalysisResult> results = new ArrayList<>();
            StringBuilder currentMoves = new StringBuilder();

            for (int i = 0; i < moves.size(); i++) {
                String move = moves.get(i);
                int moveNumber = i + 1;

                log.debug("分析第 {} 步: {}", moveNumber, move);

                // 分析当前位置（在走子之前）
                String positionMoves = currentMoves.toString().trim();
                StockfishAnalysisResult bestAnalysis = stockfishService.analyzePosition(null, positionMoves);

                // 分析实际走法后的位置
                currentMoves.append(move).append(" ");
                StockfishAnalysisResult actualAnalysis = stockfishService.analyzePosition(null, currentMoves.toString().trim());

                // 走法分类（简化版：比较实际走法与最佳走法）
                MoveClassification classification = classifyMove(actualAnalysis, bestAnalysis);

                // 保存分析结果
                AnalysisResult result = new AnalysisResult();
                result.setGameId(gameId);
                result.setMoveNumber(moveNumber);
                result.setMoveSan(move); // 注意：这里是 UCI 格式，实际应转换为 SAN

                // 处理可能为 null 的值，提供默认值
                String scoreValue = actualAnalysis.getScore();
                result.setScore(scoreValue != null ? scoreValue : "0");

                String bestMoveValue = bestAnalysis.getBestMove();
                result.setBestMove(bestMoveValue != null ? bestMoveValue : "none");

                result.setMoveClassification(classification);

                results.add(result);
            }

            // 批量保存结果
            analysisResultRepository.saveAll(results);

            // 更新状态为"已完成"
            game.setAnalysisStatus(AnalysisStatus.COMPLETED);
            gamePgnRepository.save(game);

            log.info("棋局 {} 分析完成，共分析 {} 步", gameId, results.size());

        } catch (Exception e) {
            log.error("分析棋局 {} 时发生错误", gameId, e);

            // 更新状态为"失败"
            gamePgnRepository.findById(gameId).ifPresent(game -> {
                game.setAnalysisStatus(AnalysisStatus.FAILED);
                gamePgnRepository.save(game);
            });
        }
    }

    /**
     * 简化的走法分类逻辑
     */
    private MoveClassification classifyMove(StockfishAnalysisResult actual, StockfishAnalysisResult best) {
        // 将评分转换为厘兵
        int actualScore = classificationService.parseScoreToCentipawns(actual.getScore());
        int bestScore = classificationService.parseScoreToCentipawns(best.getScore());

        // 调用分类服务
        return classificationService.classifyMove(actualScore, bestScore, null);
    }

    /**
     * 获取棋局的所有分析结果
     */
    @Transactional(readOnly = true)
    public List<AnalysisResult> getGameAnalysis(Long gameId) {
        return analysisResultRepository.findByGameIdOrderByMoveNumberAsc(gameId);
    }

    /**
     * 获取特定步数的分析结果
     */
    @Transactional(readOnly = true)
    public AnalysisResult getMoveAnalysis(Long gameId, Integer moveNumber) {
        return analysisResultRepository.findByGameIdAndMoveNumber(gameId, moveNumber)
                .orElse(null);
    }
}
