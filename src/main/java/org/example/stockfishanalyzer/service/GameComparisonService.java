package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.GameComparisonDTO;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋局对比分析服务
 */
@Service
@RequiredArgsConstructor
public class GameComparisonService {

    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * 对比两场棋局
     */
    public GameComparisonDTO compareGames(Long gameId1, Long gameId2, Long userId) {
        // 获取两场棋局
        GamePgn game1 = gamePgnRepository.findById(gameId1)
                .orElseThrow(() -> new RuntimeException("棋局1不存在"));
        GamePgn game2 = gamePgnRepository.findById(gameId2)
                .orElseThrow(() -> new RuntimeException("棋局2不存在"));
        
        // 验证权限
        if (!game1.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问棋局1");
        }
        if (!game2.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问棋局2");
        }

        // 获取分析结果
        List<AnalysisResult> results1 = analysisResultRepository.findByGameIdOrderByMoveNumberAsc(gameId1);
        List<AnalysisResult> results2 = analysisResultRepository.findByGameIdOrderByMoveNumberAsc(gameId2);

        // 构建对比数据
        GameComparisonDTO comparison = new GameComparisonDTO();
        comparison.setGame1(buildGameInfo(game1, results1));
        comparison.setGame2(buildGameInfo(game2, results2));
        comparison.setStats(buildComparisonStats(comparison.getGame1(), comparison.getGame2()));
        comparison.setGame1Scores(buildScoreCurve(results1));
        comparison.setGame2Scores(buildScoreCurve(results2));

        return comparison;
    }

    /**
     * 构建棋局信息
     */
    private GameComparisonDTO.GameInfo buildGameInfo(GamePgn game, List<AnalysisResult> results) {
        GameComparisonDTO.GameInfo info = new GameComparisonDTO.GameInfo();
        info.setGameId(game.getId());
        info.setWhitePlayer(game.getWhitePlayer());
        info.setBlackPlayer(game.getBlackPlayer());
        info.setGameResult(game.getGameResult());
        info.setGameDate(game.getGameDate());
        info.setPgnContent(game.getPgnContent());

        // 统计走法分类
        int totalMoves = results.size();
        int blunders = 0;
        int mistakes = 0;
        int inaccuracies = 0;
        int brilliant = 0;
        int best = 0;
        int good = 0;

        for (AnalysisResult result : results) {
            MoveClassification classification = result.getMoveClassification();
            if (classification != null) {
                switch (classification) {
                    case BLUNDER:
                        blunders++;
                        break;
                    case MISTAKE:
                        mistakes++;
                        break;
                    case INACCURACY:
                        inaccuracies++;
                        break;
                    case BRILLIANT:
                        brilliant++;
                        break;
                    case BEST:
                        best++;
                        break;
                    case GOOD:
                        good++;
                        break;
                }
            }
        }

        // 简化的精准度计算：基于走法分类
        double accuracy = 0.0;
        if (totalMoves > 0) {
            int goodMoves = best + brilliant + good;
            accuracy = (goodMoves * 100.0) / totalMoves;
        }

        info.setTotalMoves(totalMoves);
        info.setBlunders(blunders);
        info.setMistakes(mistakes);
        info.setInaccuracies(inaccuracies);
        info.setBrilliantMoves(brilliant);
        info.setBestMoves(best);
        info.setAvgAccuracy(accuracy);

        return info;
    }

    /**
     * 构建对比统计
     */
    private GameComparisonDTO.ComparisonStats buildComparisonStats(
            GameComparisonDTO.GameInfo game1, 
            GameComparisonDTO.GameInfo game2) {
        
        GameComparisonDTO.ComparisonStats stats = new GameComparisonDTO.ComparisonStats();
        
        // 精准度对比
        double accuracyDiff = game1.getAvgAccuracy() - game2.getAvgAccuracy();
        stats.setAccuracyDiff(accuracyDiff);
        stats.setBetterAccuracy(accuracyDiff > 0 ? "game1" : "game2");
        
        // 走法数量对比
        stats.setTotalMovesGame1(game1.getTotalMoves());
        stats.setTotalMovesGame2(game2.getTotalMoves());
        stats.setBlundersDiff(game1.getBlunders() - game2.getBlunders());
        stats.setMistakesDiff(game1.getMistakes() - game2.getMistakes());
        stats.setBrilliantDiff(game1.getBrilliantMoves() - game2.getBrilliantMoves());
        
        // 生成结论
        String conclusion = generateConclusion(game1, game2, accuracyDiff);
        stats.setConclusion(conclusion);
        
        return stats;
    }

    /**
     * 构建评分曲线
     * 注意：由于当前 AnalysisResult 的 score 是字符串格式，这里简化处理
     */
    private List<GameComparisonDTO.ScorePoint> buildScoreCurve(List<AnalysisResult> results) {
        List<GameComparisonDTO.ScorePoint> curve = new ArrayList<>();
        
        for (AnalysisResult result : results) {
            GameComparisonDTO.ScorePoint point = new GameComparisonDTO.ScorePoint();
            point.setMoveNumber(result.getMoveNumber());
            
            // 尝试解析评分字符串
            try {
                String scoreStr = result.getScore();
                if (scoreStr != null && !scoreStr.isEmpty()) {
                    // 移除 "cp" 或 "mate" 前缀，提取数值
                    scoreStr = scoreStr.replaceAll("[^-\\d.]", "");
                    if (!scoreStr.isEmpty()) {
                        point.setScore(Double.parseDouble(scoreStr) / 100.0); // 转换为兵值
                    } else {
                        point.setScore(0.0);
                    }
                } else {
                    point.setScore(0.0);
                }
            } catch (Exception e) {
                point.setScore(0.0);
            }
            
            curve.add(point);
        }
        
        return curve;
    }

    /**
     * 生成对比结论
     */
    private String generateConclusion(
            GameComparisonDTO.GameInfo game1,
            GameComparisonDTO.GameInfo game2,
            double accuracyDiff) {
        
        StringBuilder conclusion = new StringBuilder();
        
        if (Math.abs(accuracyDiff) < 5.0) {
            conclusion.append("两场棋局的整体表现非常接近。");
        } else if (accuracyDiff > 0) {
            conclusion.append(String.format("棋局1的精准度更高（%.1f%% vs %.1f%%），", 
                game1.getAvgAccuracy(), game2.getAvgAccuracy()));
        } else {
            conclusion.append(String.format("棋局2的精准度更高（%.1f%% vs %.1f%%），", 
                game2.getAvgAccuracy(), game1.getAvgAccuracy()));
        }
        
        // 失误对比
        int totalErrors1 = game1.getBlunders() + game1.getMistakes();
        int totalErrors2 = game2.getBlunders() + game2.getMistakes();
        
        if (totalErrors1 < totalErrors2) {
            conclusion.append(String.format(" 棋局1失误更少（%d vs %d）。", totalErrors1, totalErrors2));
        } else if (totalErrors1 > totalErrors2) {
            conclusion.append(String.format(" 棋局2失误更少（%d vs %d）。", totalErrors2, totalErrors1));
        } else {
            conclusion.append(" 两场棋局失误数相同。");
        }
        
        // 妙手对比
        if (game1.getBrilliantMoves() > game2.getBrilliantMoves()) {
            conclusion.append(String.format(" 棋局1有更多妙手（%d vs %d）。", 
                game1.getBrilliantMoves(), game2.getBrilliantMoves()));
        } else if (game1.getBrilliantMoves() < game2.getBrilliantMoves()) {
            conclusion.append(String.format(" 棋局2有更多妙手（%d vs %d）。", 
                game2.getBrilliantMoves(), game1.getBrilliantMoves()));
        }
        
        return conclusion.toString();
    }
}
