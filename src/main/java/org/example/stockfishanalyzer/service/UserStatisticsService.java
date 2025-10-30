package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.UserStatisticsDto;
import org.example.stockfishanalyzer.entity.UserStatistics;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.repository.OpeningBookRepository;
import org.example.stockfishanalyzer.repository.UserStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户统计服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final OpeningBookRepository openingBookRepository;

    /**
     * 获取或创建用户统计
     */
    public UserStatisticsDto getUserStatistics(Long userId) {
        Optional<UserStatistics> statsOpt = userStatisticsRepository.findByUserId(userId);

        if (statsOpt.isEmpty()) {
            // 首次访问，创建初始统计
            return createInitialStatistics(userId);
        }

        UserStatistics stats = statsOpt.get();
        return convertToDto(stats);
    }

    /**
     * 刷新用户统计数据（重新计算）
     */
    @Transactional
    public UserStatisticsDto refreshStatistics(Long userId) {
        log.info("开始刷新用户 {} 的统计数据", userId);

        UserStatistics stats = userStatisticsRepository.findByUserId(userId)
                .orElse(new UserStatistics());

        stats.setUserId(userId);

        // 1. 统计总对局数
        Long totalGames = gamePgnRepository.countByUserId(userId);
        stats.setTotalGames(totalGames.intValue());

        // 2. 统计胜负平（从PGN的gameResult字段）
        Integer winCount = gamePgnRepository.countByUserIdAndGameResultContaining(userId, "1-0");
        Integer lossCount = gamePgnRepository.countByUserIdAndGameResultContaining(userId, "0-1");
        Integer drawCount = gamePgnRepository.countByUserIdAndGameResultContaining(userId, "1/2-1/2");

        stats.setWinCount(winCount);
        stats.setLossCount(lossCount);
        stats.setDrawCount(drawCount);

        // 3. 统计走法分类
        Integer brilliants = analysisResultRepository.countByGameIdInAndMoveClassification(
                gamePgnRepository.findIdsByUserId(userId),
                org.example.stockfishanalyzer.enums.MoveClassification.BRILLIANT
        );
        Integer goods = analysisResultRepository.countByGameIdInAndMoveClassification(
                gamePgnRepository.findIdsByUserId(userId),
                org.example.stockfishanalyzer.enums.MoveClassification.GOOD
        );
        Integer mistakes = analysisResultRepository.countByGameIdInAndMoveClassification(
                gamePgnRepository.findIdsByUserId(userId),
                org.example.stockfishanalyzer.enums.MoveClassification.MISTAKE
        );
        Integer blunders = analysisResultRepository.countByGameIdInAndMoveClassification(
                gamePgnRepository.findIdsByUserId(userId),
                org.example.stockfishanalyzer.enums.MoveClassification.BLUNDER
        );

        stats.setTotalBrilliants(brilliants);
        stats.setTotalGoods(goods);
        stats.setTotalMistakes(mistakes);
        stats.setTotalBlunders(blunders);

        // 4. 计算平均准确度（简化版：基于走法质量）
        int totalMoves = brilliants + goods + mistakes + blunders;
        if (totalMoves > 0) {
            double accuracy = ((brilliants * 1.0 + goods * 0.8) / totalMoves) * 100;
            stats.setAvgAccuracy(BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP));
        } else {
            stats.setAvgAccuracy(BigDecimal.ZERO);
        }

        // 5. 更新时间
        stats.setLastUpdated(LocalDateTime.now());

        UserStatistics saved = userStatisticsRepository.save(stats);
        log.info("用户 {} 统计数据刷新完成", userId);

        return convertToDto(saved);
    }

    /**
     * 创建初始统计
     */
    private UserStatisticsDto createInitialStatistics(Long userId) {
        return UserStatisticsDto.builder()
                .userId(userId)
                .totalGames(0)
                .winCount(0)
                .drawCount(0)
                .lossCount(0)
                .winRate(0.0)
                .drawRate(0.0)
                .lossRate(0.0)
                .avgAccuracy(BigDecimal.ZERO)
                .totalBrilliants(0)
                .totalGoods(0)
                .totalMistakes(0)
                .totalBlunders(0)
                .totalMoves(0)
                .brilliantRate(0.0)
                .goodRate(0.0)
                .mistakeRate(0.0)
                .blunderRate(0.0)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 转换为DTO并计算衍生字段
     */
    private UserStatisticsDto convertToDto(UserStatistics stats) {
        int totalGames = stats.getTotalGames();
        int totalMoves = stats.getTotalBrilliants() + stats.getTotalGoods()
                        + stats.getTotalMistakes() + stats.getTotalBlunders();

        UserStatisticsDto dto = UserStatisticsDto.builder()
                .id(stats.getId())
                .userId(stats.getUserId())
                .totalGames(totalGames)
                .winCount(stats.getWinCount())
                .drawCount(stats.getDrawCount())
                .lossCount(stats.getLossCount())
                .avgAccuracy(stats.getAvgAccuracy())
                .totalBrilliants(stats.getTotalBrilliants())
                .totalGoods(stats.getTotalGoods())
                .totalMistakes(stats.getTotalMistakes())
                .totalBlunders(stats.getTotalBlunders())
                .totalMoves(totalMoves)
                .favoriteOpeningId(stats.getFavoriteOpeningId())
                .lastUpdated(stats.getLastUpdated())
                .build();

        // 计算胜率
        if (totalGames > 0) {
            dto.setWinRate(calculateRate(stats.getWinCount(), totalGames));
            dto.setDrawRate(calculateRate(stats.getDrawCount(), totalGames));
            dto.setLossRate(calculateRate(stats.getLossCount(), totalGames));
        } else {
            dto.setWinRate(0.0);
            dto.setDrawRate(0.0);
            dto.setLossRate(0.0);
        }

        // 计算走法质量率
        if (totalMoves > 0) {
            dto.setBrilliantRate(calculateRate(stats.getTotalBrilliants(), totalMoves));
            dto.setGoodRate(calculateRate(stats.getTotalGoods(), totalMoves));
            dto.setMistakeRate(calculateRate(stats.getTotalMistakes(), totalMoves));
            dto.setBlunderRate(calculateRate(stats.getTotalBlunders(), totalMoves));
        } else {
            dto.setBrilliantRate(0.0);
            dto.setGoodRate(0.0);
            dto.setMistakeRate(0.0);
            dto.setBlunderRate(0.0);
        }

        // 获取最擅长开局名称
        if (stats.getFavoriteOpeningId() != null) {
            openingBookRepository.findById(stats.getFavoriteOpeningId())
                    .ifPresent(opening -> dto.setFavoriteOpeningName(opening.getOpeningName()));
        }

        return dto;
    }

    /**
     * 计算百分比
     */
    private Double calculateRate(int count, int total) {
        if (total == 0) return 0.0;
        return Math.round(count * 10000.0 / total) / 100.0;  // 保留2位小数
    }
}

