package org.example.stockfishanalyzer.service;

import org.example.stockfishanalyzer.dto.TrendsDTO;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 趋势分析服务
 */
@Service
public class TrendsService {
    
    @Autowired
    private GamePgnRepository gamePgnRepository;
    
    @Autowired
    private AnalysisResultRepository analysisResultRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取趋势分析数据
     */
    public TrendsDTO getTrends(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户所有棋局并按日期过滤
        List<GamePgn> allGames = gamePgnRepository.findByUserIdOrderByUploadedAtDesc(userId);
        
        List<GamePgn> games = allGames.stream()
            .filter(game -> {
                try {
                    LocalDate gameDate = parseGameDate(game.getGameDate());
                    return !gameDate.isBefore(startDate) && !gameDate.isAfter(endDate);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        if (games.isEmpty()) {
            return createEmptyTrends();
        }
        
        // 按日期分组统计
        List<TrendsDTO.DataPoint> timeline = buildTimeline(games);
        
        // 开局统计
        List<TrendsDTO.OpeningStats> openings = buildOpeningStats(games);
        
        // 总体统计
        TrendsDTO.OverallStats overall = buildOverallStats(games);
        
        return new TrendsDTO(timeline, openings, overall);
    }
    
    /**
     * 解析游戏日期字符串为LocalDate
     */
    private LocalDate parseGameDate(String dateStr) {
        if (dateStr == null || dateStr.contains("?")) {
            return LocalDate.now();
        }
        // 日期格式: yyyy.MM.dd
        String normalized = dateStr.replace(".", "-");
        return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 构建时间序列
     */
    private List<TrendsDTO.DataPoint> buildTimeline(List<GamePgn> games) {
        Map<String, List<GamePgn>> gamesByDate = games.stream()
            .collect(Collectors.groupingBy(game -> {
                try {
                    LocalDate date = parseGameDate(game.getGameDate());
                    return date.format(DATE_FORMATTER);
                } catch (Exception e) {
                    return LocalDate.now().format(DATE_FORMATTER);
                }
            }));
        
        return gamesByDate.entrySet().stream()
            .map(entry -> {
                String date = entry.getKey();
                List<GamePgn> dayGames = entry.getValue();
                
                // 计算当天的统计数据
                double avgAccuracy = calculateAvgAccuracy(dayGames);
                int totalGames = dayGames.size();
                double winRate = calculateWinRate(dayGames);
                
                // 计算失误统计
                Map<String, Integer> errorStats = calculateErrorStats(dayGames);
                
                return new TrendsDTO.DataPoint(
                    date,
                    avgAccuracy,
                    totalGames,
                    winRate,
                    errorStats.getOrDefault("blunders", 0),
                    errorStats.getOrDefault("mistakes", 0),
                    errorStats.getOrDefault("inaccuracies", 0),
                    errorStats.getOrDefault("brilliant", 0)
                );
            })
            .sorted(Comparator.comparing(TrendsDTO.DataPoint::getDate))
            .collect(Collectors.toList());
    }
    
    /**
     * 构建开局统计（从PGN内容中提取开局名称）
     */
    private List<TrendsDTO.OpeningStats> buildOpeningStats(List<GamePgn> games) {
        Map<String, List<GamePgn>> gamesByOpening = games.stream()
            .collect(Collectors.groupingBy(game -> {
                String opening = extractOpeningFromPgn(game.getPgnContent());
                return opening != null ? opening : "Unknown Opening";
            }));
        
        return gamesByOpening.entrySet().stream()
            .map(entry -> {
                String opening = entry.getKey();
                List<GamePgn> openingGames = entry.getValue();
                int count = openingGames.size();
                double winRate = calculateWinRate(openingGames);
                
                return new TrendsDTO.OpeningStats(opening, count, winRate);
            })
            .sorted(Comparator.comparing(TrendsDTO.OpeningStats::getCount).reversed())
            .limit(10)  // 只返回前10个开局
            .collect(Collectors.toList());
    }
    
    /**
     * 从PGN内容中提取开局名称
     */
    private String extractOpeningFromPgn(String pgnContent) {
        if (pgnContent == null) return null;
        
        String[] lines = pgnContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("[Opening ")) {
                int start = line.indexOf("\"");
                int end = line.lastIndexOf("\"");
                if (start != -1 && end != -1 && start < end) {
                    return line.substring(start + 1, end);
                }
            }
        }
        return null;
    }
    
    /**
     * 构建总体统计
     */
    private TrendsDTO.OverallStats buildOverallStats(List<GamePgn> games) {
        TrendsDTO.OverallStats stats = new TrendsDTO.OverallStats();
        
        stats.setTotalGames(games.size());
        stats.setAvgAccuracy(calculateAvgAccuracy(games));
        stats.setOverallWinRate(calculateWinRate(games));
        
        Map<String, Integer> errorStats = calculateErrorStats(games);
        stats.setTotalBlunders(errorStats.getOrDefault("blunders", 0));
        stats.setTotalMistakes(errorStats.getOrDefault("mistakes", 0));
        stats.setTotalInaccuracies(errorStats.getOrDefault("inaccuracies", 0));
        stats.setTotalBrilliantMoves(errorStats.getOrDefault("brilliant", 0));
        
        return stats;
    }
    
    /**
     * 计算平均精准度
     */
    private double calculateAvgAccuracy(List<GamePgn> games) {
        if (games.isEmpty()) return 0.0;
        
        double totalAccuracy = 0.0;
        int count = 0;
        
        for (GamePgn game : games) {
            List<AnalysisResult> results = analysisResultRepository
                .findByGameIdOrderByMoveNumberAsc(game.getId());
            
            if (!results.isEmpty()) {
                long goodMoves = results.stream()
                    .filter(r -> r.getMoveClassification() == MoveClassification.BEST ||
                               r.getMoveClassification() == MoveClassification.BRILLIANT ||
                               r.getMoveClassification() == MoveClassification.GOOD)
                    .count();
                
                double accuracy = (double) goodMoves / results.size() * 100.0;
                totalAccuracy += accuracy;
                count++;
            }
        }
        
        return count > 0 ? totalAccuracy / count : 0.0;
    }
    
    /**
     * 计算胜率
     */
    private double calculateWinRate(List<GamePgn> games) {
        if (games.isEmpty()) return 0.0;
        
        long wins = games.stream()
            .filter(game -> "1-0".equals(game.getGameResult()) || "0-1".equals(game.getGameResult()))
            .count();
        
        return (double) wins / games.size() * 100.0;
    }
    
    /**
     * 计算失误统计
     */
    private Map<String, Integer> calculateErrorStats(List<GamePgn> games) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("blunders", 0);
        stats.put("mistakes", 0);
        stats.put("inaccuracies", 0);
        stats.put("brilliant", 0);
        
        for (GamePgn game : games) {
            List<AnalysisResult> results = analysisResultRepository
                .findByGameIdOrderByMoveNumberAsc(game.getId());
            
            for (AnalysisResult result : results) {
                switch (result.getMoveClassification()) {
                    case BLUNDER:
                        stats.put("blunders", stats.get("blunders") + 1);
                        break;
                    case MISTAKE:
                        stats.put("mistakes", stats.get("mistakes") + 1);
                        break;
                    case INACCURACY:
                        stats.put("inaccuracies", stats.get("inaccuracies") + 1);
                        break;
                    case BRILLIANT:
                        stats.put("brilliant", stats.get("brilliant") + 1);
                        break;
                    default:
                        break;
                }
            }
        }
        
        return stats;
    }
    
    /**
     * 创建空的趋势数据
     */
    private TrendsDTO createEmptyTrends() {
        TrendsDTO.OverallStats overall = new TrendsDTO.OverallStats();
        overall.setTotalGames(0);
        overall.setAvgAccuracy(0.0);
        overall.setOverallWinRate(0.0);
        overall.setTotalBlunders(0);
        overall.setTotalMistakes(0);
        overall.setTotalInaccuracies(0);
        overall.setTotalBrilliantMoves(0);
        
        return new TrendsDTO(new ArrayList<>(), new ArrayList<>(), overall);
    }
}
