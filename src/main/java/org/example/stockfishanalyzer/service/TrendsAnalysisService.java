package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.TrendsAnalysisResponse;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史趋势分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrendsAnalysisService {
    
    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;
    
    /**
     * 获取用户的历史趋势分析
     */
    public TrendsAnalysisResponse getTrends(Long userId, String period) {
        log.info("获取用户 {} 的历史趋势，周期: {}", userId, period);
        
        // 获取用户的所有棋局
        List<GamePgn> allGames = gamePgnRepository.findByUserId(userId);
        
        if (allGames.isEmpty()) {
            return buildEmptyResponse(userId, period);
        }
        
        // 根据周期分组数据
        Map<String, List<GamePgn>> groupedGames = groupGamesByPeriod(allGames, period);
        
        // 构建响应
        return TrendsAnalysisResponse.builder()
                .userId(userId)
                .period(period)
                .accuracyTrend(buildAccuracyTrend(groupedGames))
                .winRateTrend(buildWinRateTrend(groupedGames))
                .gameFrequency(buildGameFrequency(groupedGames))
                .moveQualityRadar(buildMoveQualityRadar(allGames))
                .keyMetrics(buildKeyMetrics(groupedGames))
                .build();
    }
    
    /**
     * 根据周期分组棋局
     */
    private Map<String, List<GamePgn>> groupGamesByPeriod(List<GamePgn> games, String period) {
        DateTimeFormatter formatter;
        
        switch (period.toLowerCase()) {
            case "month" -> formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            case "quarter" -> formatter = DateTimeFormatter.ofPattern("yyyy-'Q'Q");
            case "year" -> formatter = DateTimeFormatter.ofPattern("yyyy");
            default -> formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        }
        
        Map<String, List<GamePgn>> grouped = new LinkedHashMap<>();
        
        for (GamePgn game : games) {
            String key = formatPeriod(game.getUploadedAt(), period);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(game);
        }
        
        return grouped;
    }
    
    /**
     * 格式化时间周期
     */
    private String formatPeriod(LocalDateTime dateTime, String period) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        return switch (period.toLowerCase()) {
            case "month" -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "quarter" -> {
                int quarter = (dateTime.getMonthValue() - 1) / 3 + 1;
                yield dateTime.getYear() + "-Q" + quarter;
            }
            case "year" -> String.valueOf(dateTime.getYear());
            default -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        };
    }
    
    /**
     * 构建精准度趋势
     */
    private TrendsAnalysisResponse.AccuracyTrend buildAccuracyTrend(Map<String, List<GamePgn>> groupedGames) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> accuracyData = new ArrayList<>();
        
        for (Map.Entry<String, List<GamePgn>> entry : groupedGames.entrySet()) {
            labels.add(entry.getKey());
            
            // 计算该期间的平均精准度
            BigDecimal avgAccuracy = calculatePeriodAccuracy(entry.getValue());
            accuracyData.add(avgAccuracy);
        }
        
        return TrendsAnalysisResponse.AccuracyTrend.builder()
                .labels(labels)
                .accuracyData(accuracyData)
                .build();
    }
    
    /**
     * 计算期间平均精准度
     */
    private BigDecimal calculatePeriodAccuracy(List<GamePgn> games) {
        if (games.isEmpty()) return BigDecimal.ZERO;
        
        List<Long> gameIds = games.stream().map(GamePgn::getId).collect(Collectors.toList());
        
        int totalBrilliant = countMovesByClassification(gameIds, MoveClassification.BRILLIANT);
        int totalGood = countMovesByClassification(gameIds, MoveClassification.GOOD);
        int totalInaccuracy = countMovesByClassification(gameIds, MoveClassification.INACCURACY);
        int totalMistake = countMovesByClassification(gameIds, MoveClassification.MISTAKE);
        int totalBlunder = countMovesByClassification(gameIds, MoveClassification.BLUNDER);
        int totalBest = countMovesByClassification(gameIds, MoveClassification.BEST);
        
        int totalMoves = totalBrilliant + totalGood + totalInaccuracy + totalMistake + totalBlunder + totalBest;
        
        if (totalMoves == 0) return BigDecimal.ZERO;
        
        // 精准度计算: (妙手*100 + 最佳*100 + 好棋*90 + 不精准*70 + 失误*40 + 漏着*10) / 总步数
        int score = totalBrilliant * 100 + totalBest * 100 + totalGood * 90 + 
                    totalInaccuracy * 70 + totalMistake * 40 + totalBlunder * 10;
        
        return BigDecimal.valueOf(score)
                .divide(BigDecimal.valueOf(totalMoves), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 统计指定分类的走法数量
     */
    private int countMovesByClassification(List<Long> gameIds, MoveClassification classification) {
        if (gameIds.isEmpty()) return 0;
        Integer count = analysisResultRepository.countByGameIdInAndMoveClassification(gameIds, classification);
        return count != null ? count : 0;
    }
    
    /**
     * 构建胜率趋势
     */
    private TrendsAnalysisResponse.WinRateTrend buildWinRateTrend(Map<String, List<GamePgn>> groupedGames) {
        List<String> labels = new ArrayList<>();
        List<Integer> winCounts = new ArrayList<>();
        List<Integer> drawCounts = new ArrayList<>();
        List<Integer> lossCounts = new ArrayList<>();
        List<BigDecimal> winRates = new ArrayList<>();
        
        for (Map.Entry<String, List<GamePgn>> entry : groupedGames.entrySet()) {
            labels.add(entry.getKey());
            
            int wins = 0, draws = 0, losses = 0;
            
            for (GamePgn game : entry.getValue()) {
                String result = game.getGameResult();
                if (result != null) {
                    if (result.contains("1-0")) wins++;
                    else if (result.contains("0-1")) losses++;
                    else if (result.contains("1/2")) draws++;
                }
            }
            
            winCounts.add(wins);
            drawCounts.add(draws);
            lossCounts.add(losses);
            
            int total = wins + draws + losses;
            BigDecimal winRate = total > 0 
                    ? BigDecimal.valueOf(wins * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            winRates.add(winRate);
        }
        
        return TrendsAnalysisResponse.WinRateTrend.builder()
                .labels(labels)
                .winCounts(winCounts)
                .drawCounts(drawCounts)
                .lossCounts(lossCounts)
                .winRates(winRates)
                .build();
    }
    
    /**
     * 构建对局频率
     */
    private TrendsAnalysisResponse.GameFrequency buildGameFrequency(Map<String, List<GamePgn>> groupedGames) {
        List<String> labels = new ArrayList<>();
        List<Integer> gameCounts = new ArrayList<>();
        
        for (Map.Entry<String, List<GamePgn>> entry : groupedGames.entrySet()) {
            labels.add(entry.getKey());
            gameCounts.add(entry.getValue().size());
        }
        
        return TrendsAnalysisResponse.GameFrequency.builder()
                .labels(labels)
                .gameCounts(gameCounts)
                .build();
    }
    
    /**
     * 构建走法质量雷达图
     */
    private TrendsAnalysisResponse.MoveQualityRadar buildMoveQualityRadar(List<GamePgn> allGames) {
        List<Long> gameIds = allGames.stream().map(GamePgn::getId).collect(Collectors.toList());
        
        int brilliant = countMovesByClassification(gameIds, MoveClassification.BRILLIANT);
        int good = countMovesByClassification(gameIds, MoveClassification.GOOD);
        int inaccuracy = countMovesByClassification(gameIds, MoveClassification.INACCURACY);
        int mistake = countMovesByClassification(gameIds, MoveClassification.MISTAKE);
        int blunder = countMovesByClassification(gameIds, MoveClassification.BLUNDER);
        
        int total = brilliant + good + inaccuracy + mistake + blunder;
        
        List<BigDecimal> values = new ArrayList<>();
        if (total > 0) {
            values.add(BigDecimal.valueOf(brilliant * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            values.add(BigDecimal.valueOf(good * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            values.add(BigDecimal.valueOf(inaccuracy * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            values.add(BigDecimal.valueOf(mistake * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            values.add(BigDecimal.valueOf(blunder * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        } else {
            values.addAll(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        
        return TrendsAnalysisResponse.MoveQualityRadar.builder()
                .indicators(Arrays.asList("妙手", "好棋", "不精准", "失误", "漏着"))
                .values(values)
                .build();
    }
    
    /**
     * 构建关键指标
     */
    private TrendsAnalysisResponse.KeyMetrics buildKeyMetrics(Map<String, List<GamePgn>> groupedGames) {
        if (groupedGames.isEmpty()) {
            return TrendsAnalysisResponse.KeyMetrics.builder()
                    .bestPeriod(null)
                    .worstPeriod(null)
                    .improvement(TrendsAnalysisResponse.ImprovementMetric.builder()
                            .accuracyChange(BigDecimal.ZERO)
                            .winRateChange(BigDecimal.ZERO)
                            .trend("stable")
                            .build())
                    .build();
        }
        
        // 计算每个期间的指标
        List<PeriodStats> periodStats = new ArrayList<>();
        
        for (Map.Entry<String, List<GamePgn>> entry : groupedGames.entrySet()) {
            periodStats.add(calculatePeriodStats(entry.getKey(), entry.getValue()));
        }
        
        // 找出最佳和最差期间
        PeriodStats best = periodStats.stream()
                .max(Comparator.comparing(ps -> ps.winRate))
                .orElse(null);
        
        PeriodStats worst = periodStats.stream()
                .min(Comparator.comparing(ps -> ps.winRate))
                .orElse(null);
        
        // 计算进步幅度
        TrendsAnalysisResponse.ImprovementMetric improvement = calculateImprovement(periodStats);
        
        return TrendsAnalysisResponse.KeyMetrics.builder()
                .bestPeriod(best != null ? convertToPeriodMetric(best) : null)
                .worstPeriod(worst != null ? convertToPeriodMetric(worst) : null)
                .improvement(improvement)
                .build();
    }
    
    /**
     * 计算期间统计数据
     */
    private PeriodStats calculatePeriodStats(String period, List<GamePgn> games) {
        int wins = 0, total = 0;
        
        for (GamePgn game : games) {
            String result = game.getGameResult();
            if (result != null && !result.equals("*")) {
                total++;
                if (result.contains("1-0")) wins++;
            }
        }
        
        BigDecimal winRate = total > 0 
                ? BigDecimal.valueOf(wins * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal accuracy = calculatePeriodAccuracy(games);
        
        return new PeriodStats(period, winRate, accuracy, total);
    }
    
    /**
     * 计算进步幅度
     */
    private TrendsAnalysisResponse.ImprovementMetric calculateImprovement(List<PeriodStats> periodStats) {
        if (periodStats.size() < 2) {
            return TrendsAnalysisResponse.ImprovementMetric.builder()
                    .accuracyChange(BigDecimal.ZERO)
                    .winRateChange(BigDecimal.ZERO)
                    .trend("stable")
                    .build();
        }
        
        // 比较第一期和最后一期
        PeriodStats first = periodStats.get(0);
        PeriodStats last = periodStats.get(periodStats.size() - 1);
        
        BigDecimal accuracyChange = last.accuracy.subtract(first.accuracy);
        BigDecimal winRateChange = last.winRate.subtract(first.winRate);
        
        String trend;
        if (accuracyChange.compareTo(BigDecimal.valueOf(5)) > 0 || winRateChange.compareTo(BigDecimal.valueOf(5)) > 0) {
            trend = "improving";
        } else if (accuracyChange.compareTo(BigDecimal.valueOf(-5)) < 0 || winRateChange.compareTo(BigDecimal.valueOf(-5)) < 0) {
            trend = "declining";
        } else {
            trend = "stable";
        }
        
        return TrendsAnalysisResponse.ImprovementMetric.builder()
                .accuracyChange(accuracyChange)
                .winRateChange(winRateChange)
                .trend(trend)
                .build();
    }
    
    /**
     * 转换为PeriodMetric
     */
    private TrendsAnalysisResponse.PeriodMetric convertToPeriodMetric(PeriodStats stats) {
        return TrendsAnalysisResponse.PeriodMetric.builder()
                .period(stats.period)
                .winRate(stats.winRate)
                .accuracy(stats.accuracy)
                .totalGames(stats.totalGames)
                .build();
    }
    
    /**
     * 构建空响应
     */
    private TrendsAnalysisResponse buildEmptyResponse(Long userId, String period) {
        return TrendsAnalysisResponse.builder()
                .userId(userId)
                .period(period)
                .accuracyTrend(TrendsAnalysisResponse.AccuracyTrend.builder()
                        .labels(new ArrayList<>())
                        .accuracyData(new ArrayList<>())
                        .build())
                .winRateTrend(TrendsAnalysisResponse.WinRateTrend.builder()
                        .labels(new ArrayList<>())
                        .winCounts(new ArrayList<>())
                        .drawCounts(new ArrayList<>())
                        .lossCounts(new ArrayList<>())
                        .winRates(new ArrayList<>())
                        .build())
                .gameFrequency(TrendsAnalysisResponse.GameFrequency.builder()
                        .labels(new ArrayList<>())
                        .gameCounts(new ArrayList<>())
                        .build())
                .moveQualityRadar(TrendsAnalysisResponse.MoveQualityRadar.builder()
                        .indicators(Arrays.asList("妙手", "好棋", "不精准", "失误", "漏着"))
                        .values(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                        .build())
                .keyMetrics(TrendsAnalysisResponse.KeyMetrics.builder()
                        .improvement(TrendsAnalysisResponse.ImprovementMetric.builder()
                                .accuracyChange(BigDecimal.ZERO)
                                .winRateChange(BigDecimal.ZERO)
                                .trend("stable")
                                .build())
                        .build())
                .build();
    }
    
    /**
     * 内部类：期间统计数据
     */
    private static class PeriodStats {
        String period;
        BigDecimal winRate;
        BigDecimal accuracy;
        int totalGames;
        
        PeriodStats(String period, BigDecimal winRate, BigDecimal accuracy, int totalGames) {
            this.period = period;
            this.winRate = winRate;
            this.accuracy = accuracy;
            this.totalGames = totalGames;
        }
    }
}
