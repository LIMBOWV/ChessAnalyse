package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.OpeningListResponse;
import org.example.stockfishanalyzer.dto.OpeningStatsDto;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.repository.OpeningBookRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开局分析服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpeningAnalysisService {

    private final GamePgnRepository gamePgnRepository;
    private final OpeningBookRepository openingBookRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * 获取用户的开局统计
     */
    public OpeningListResponse getUserOpeningStats(Long userId) {
        log.info("开始分析用户 {} 的开局数据", userId);

        // TODO: 这里需要在GamePgn表中添加opening_id字段来关联开局
        // 目前返回模拟数据结构

        List<OpeningStatsDto> allOpenings = new ArrayList<>();

        // 获取所有流行的开局
        List<org.example.stockfishanalyzer.entity.OpeningBook> popularOpenings =
                openingBookRepository.findTop10ByOrderByPopularityDesc();

        for (org.example.stockfishanalyzer.entity.OpeningBook opening : popularOpenings) {
            OpeningStatsDto stats = OpeningStatsDto.builder()
                    .openingId(opening.getId())
                    .ecoCode(opening.getEcoCode())
                    .openingName(opening.getOpeningName())
                    .variationName(opening.getVariationName())
                    .totalGames(0)  // TODO: 实际统计
                    .winCount(0)
                    .drawCount(0)
                    .lossCount(0)
                    .winRate(BigDecimal.ZERO)
                    .performance(BigDecimal.ZERO)
                    .avgAccuracy(BigDecimal.ZERO)
                    .totalBrilliants(0)
                    .totalMistakes(0)
                    .totalBlunders(0)
                    .build();

            allOpenings.add(stats);
        }

        // 按胜率排序
        allOpenings.sort(Comparator.comparing(OpeningStatsDto::getWinRate).reversed());

        // 设置排名
        for (int i = 0; i < allOpenings.size(); i++) {
            allOpenings.get(i).setRank(i + 1);
        }

        // 获取Top 3和最薄弱的3个
        List<OpeningStatsDto> topOpenings = allOpenings.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<OpeningStatsDto> weakOpenings = allOpenings.stream()
                .skip(Math.max(0, allOpenings.size() - 3))
                .collect(Collectors.toList());

        return OpeningListResponse.builder()
                .userId(userId)
                .totalOpenings(allOpenings.size())
                .allOpenings(allOpenings)
                .topOpenings(topOpenings)
                .weakOpenings(weakOpenings)
                .build();
    }

    /**
     * 计算百分比
     */
    private BigDecimal calculateRate(int count, int total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(count * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }
}

