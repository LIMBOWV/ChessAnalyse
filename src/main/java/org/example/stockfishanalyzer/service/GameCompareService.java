package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.GameCompareResponse;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 棋局对比分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameCompareService {
    
    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;
    
    /**
     * 对比两个棋局
     */
    public GameCompareResponse compareGames(Long gameId1, Long gameId2) {
        log.info("开始对比棋局: {} vs {}", gameId1, gameId2);
        
        // 获取两个棋局的基本信息
        GamePgn game1 = gamePgnRepository.findById(gameId1)
                .orElseThrow(() -> new RuntimeException("棋局1不存在: " + gameId1));
        GamePgn game2 = gamePgnRepository.findById(gameId2)
                .orElseThrow(() -> new RuntimeException("棋局2不存在: " + gameId2));
        
        // 获取分析结果
        List<AnalysisResult> results1 = analysisResultRepository.findByGameIdOrderByMoveNumberAsc(gameId1);
        List<AnalysisResult> results2 = analysisResultRepository.findByGameIdOrderByMoveNumberAsc(gameId2);
        
        // 构建响应
        return GameCompareResponse.builder()
                .game1(buildGameBasicInfo(game1, results1.size()))
                .game2(buildGameBasicInfo(game2, results2.size()))
                .statistics(buildCompareStatistics(results1, results2))
                .scoreCurves(buildScoreCurves(results1, results2))
                .keyDifferences(findKeyDifferences(results1, results2))
                .build();
    }
    
    /**
     * 构建游戏基本信息
     */
    private GameCompareResponse.GameBasicInfo buildGameBasicInfo(GamePgn game, int totalMoves) {
        return GameCompareResponse.GameBasicInfo.builder()
                .gameId(game.getId())
                .whiteName(game.getWhitePlayer())
                .blackName(game.getBlackPlayer())
                .gameResult(game.getGameResult())
                .gameDate(game.getGameDate())
                .event("Chess Game")  // 默认值，因为 GamePgn 没有 event 字段
                .totalMoves(totalMoves)
                .build();
    }
    
    /**
     * 构建对比统计数据
     */
    private GameCompareResponse.CompareStatistics buildCompareStatistics(
            List<AnalysisResult> results1, List<AnalysisResult> results2) {
        
        return GameCompareResponse.CompareStatistics.builder()
                .game1Stats(calculateGameStats(results1))
                .game2Stats(calculateGameStats(results2))
                .build();
    }
    
    /**
     * 计算单个游戏的统计数据
     */
    private GameCompareResponse.GameStats calculateGameStats(List<AnalysisResult> results) {
        if (results.isEmpty()) {
            return GameCompareResponse.GameStats.builder()
                    .averageAccuracy(BigDecimal.ZERO)
                    .brilliantMoves(0)
                    .goodMoves(0)
                    .inaccuracies(0)
                    .mistakes(0)
                    .blunders(0)
                    .bestMoveRate(0)
                    .build();
        }
        
        int brilliant = 0, good = 0, inaccuracy = 0, mistake = 0, blunder = 0, best = 0;
        
        for (AnalysisResult result : results) {
            MoveClassification classification = result.getMoveClassification();
            if (classification != null) {
                switch (classification) {
                    case BRILLIANT -> brilliant++;
                    case GOOD -> good++;
                    case INACCURACY -> inaccuracy++;
                    case MISTAKE -> mistake++;
                    case BLUNDER -> blunder++;
                    case BEST -> best++;
                }
            }
        }
        
        int totalMoves = results.size();
        int bestMoveRate = (best + brilliant) * 100 / totalMoves;
        
        // 计算平均精准度 (基于走法质量分布)
        BigDecimal accuracy = calculateAccuracy(brilliant, good, inaccuracy, mistake, blunder, totalMoves);
        
        return GameCompareResponse.GameStats.builder()
                .averageAccuracy(accuracy)
                .brilliantMoves(brilliant)
                .goodMoves(good)
                .inaccuracies(inaccuracy)
                .mistakes(mistake)
                .blunders(blunder)
                .bestMoveRate(bestMoveRate)
                .build();
    }
    
    /**
     * 计算精准度
     */
    private BigDecimal calculateAccuracy(int brilliant, int good, int inaccuracy, int mistake, int blunder, int total) {
        if (total == 0) return BigDecimal.ZERO;
        
        // 精准度计算公式: (妙手*100 + 好棋*90 + 不精准*70 + 失误*40 + 漏着*10) / 总步数
        int score = brilliant * 100 + good * 90 + inaccuracy * 70 + mistake * 40 + blunder * 10;
        return BigDecimal.valueOf(score)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 构建评分曲线数据
     */
    private GameCompareResponse.ScoreCurves buildScoreCurves(
            List<AnalysisResult> results1, List<AnalysisResult> results2) {
        
        int maxMoves = Math.max(results1.size(), results2.size());
        List<Integer> moveNumbers = new ArrayList<>();
        List<Integer> game1Scores = new ArrayList<>();
        List<Integer> game2Scores = new ArrayList<>();
        
        for (int i = 0; i < maxMoves; i++) {
            moveNumbers.add(i + 1);
            
            // 游戏1的评分
            if (i < results1.size()) {
                game1Scores.add(parseScore(results1.get(i).getScore()));
            } else {
                game1Scores.add(null);
            }
            
            // 游戏2的评分
            if (i < results2.size()) {
                game2Scores.add(parseScore(results2.get(i).getScore()));
            } else {
                game2Scores.add(null);
            }
        }
        
        return GameCompareResponse.ScoreCurves.builder()
                .moveNumbers(moveNumbers)
                .game1Scores(game1Scores)
                .game2Scores(game2Scores)
                .build();
    }
    
    /**
     * 查找关键差异点 (评分差异最大的前5步)
     */
    private List<GameCompareResponse.KeyDifference> findKeyDifferences(
            List<AnalysisResult> results1, List<AnalysisResult> results2) {
        
        List<GameCompareResponse.KeyDifference> differences = new ArrayList<>();
        int minMoves = Math.min(results1.size(), results2.size());
        
        for (int i = 0; i < minMoves; i++) {
            AnalysisResult r1 = results1.get(i);
            AnalysisResult r2 = results2.get(i);
            
            Integer score1 = parseScore(r1.getScore());
            Integer score2 = parseScore(r2.getScore());
            
            if (score1 != null && score2 != null) {
                int diff = Math.abs(score1 - score2);
                
                if (diff > 50) {  // 只记录差异大于0.5分的情况
                    differences.add(GameCompareResponse.KeyDifference.builder()
                            .moveNumber(i + 1)
                            .game1Move(r1.getMoveSan())
                            .game1Score(score1)
                            .game2Move(r2.getMoveSan())
                            .game2Score(score2)
                            .scoreDifference(diff)
                            .reason(generateDifferenceReason(r1.getMoveClassification(), r2.getMoveClassification(), diff))
                            .build());
                }
            }
        }
        
        // 按差异值降序排序，取前5个
        return differences.stream()
                .sorted((a, b) -> b.getScoreDifference().compareTo(a.getScoreDifference()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    /**
     * 解析评分字符串为整数 (厘兵值)
     */
    private Integer parseScore(String scoreStr) {
        if (scoreStr == null || scoreStr.isEmpty()) return null;
        
        try {
            // 处理将军 (#) 和胜势 (M) 的情况
            if (scoreStr.startsWith("#") || scoreStr.startsWith("M")) {
                return scoreStr.charAt(0) == '#' ? 10000 : 5000;
            }
            // 去除 "+" 号并转换
            return Integer.parseInt(scoreStr.replace("+", ""));
        } catch (NumberFormatException e) {
            log.warn("无法解析评分: {}", scoreStr);
            return null;
        }
    }
    
    /**
     * 生成差异原因描述
     */
    private String generateDifferenceReason(MoveClassification class1, MoveClassification class2, int diff) {
        if (class1 == null || class2 == null) {
            return String.format("评分差异达 %.1f 分", diff / 100.0);
        }
        
        return String.format("游戏1: %s, 游戏2: %s (差异 %.1f 分)", 
                getClassificationChinese(class1), 
                getClassificationChinese(class2), 
                diff / 100.0);
    }
    
    /**
     * 获取走法分类的中文描述
     */
    private String getClassificationChinese(MoveClassification classification) {
        return switch (classification) {
            case BRILLIANT -> "妙手";
            case GOOD -> "好棋";
            case BEST -> "最佳";
            case INACCURACY -> "不精准";
            case MISTAKE -> "失误";
            case BLUNDER -> "漏着";
        };
    }
}
