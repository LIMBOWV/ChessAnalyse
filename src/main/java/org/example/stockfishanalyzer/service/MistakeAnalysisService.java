package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.MistakeListResponse;
import org.example.stockfishanalyzer.dto.MistakeStatsDto;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.repository.AnalysisResultRepository;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 失误分析服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MistakeAnalysisService {

    private final GamePgnRepository gamePgnRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * 获取用户的失误统计
     * @param userId 用户ID
     * @param classificationType 失误类型（BLUNDER/MISTAKE/INACCURACY，可选）
     */
    public MistakeListResponse getUserMistakes(Long userId, MoveClassification classificationType) {
        log.info("开始分析用户 {} 的失误数据，类型：{}", userId, classificationType);

        // 获取用户所有棋局ID
        List<Long> gameIds = gamePgnRepository.findIdsByUserId(userId);

        if (gameIds.isEmpty()) {
            return createEmptyResponse(userId);
        }

        // 获取失误列表
        List<MistakeStatsDto> mistakes = new ArrayList<>();

        if (classificationType != null) {
            // 只查询特定类型的失误
            mistakes = getMistakesByType(gameIds, classificationType);
        } else {
            // 查询所有失误
            mistakes.addAll(getMistakesByType(gameIds, MoveClassification.BLUNDER));
            mistakes.addAll(getMistakesByType(gameIds, MoveClassification.MISTAKE));
            mistakes.addAll(getMistakesByType(gameIds, MoveClassification.INACCURACY));
        }

        // 按失误类型分组统计
        Map<MoveClassification, Integer> mistakesByType = new HashMap<>();
        int blunderCount = 0;
        int mistakeCount = 0;
        int inaccuracyCount = 0;

        for (MistakeStatsDto mistake : mistakes) {
            MoveClassification type = mistake.getClassification();
            mistakesByType.put(type, mistakesByType.getOrDefault(type, 0) + 1);

            if (type == MoveClassification.BLUNDER) blunderCount++;
            else if (type == MoveClassification.MISTAKE) mistakeCount++;
            else if (type == MoveClassification.INACCURACY) inaccuracyCount++;
        }

        return MistakeListResponse.builder()
                .userId(userId)
                .totalMistakes(mistakes.size())
                .mistakesByType(mistakesByType)
                .mistakes(mistakes)
                .totalBlunders(blunderCount)
                .totalMistakesCount(mistakeCount)
                .totalInaccuracies(inaccuracyCount)
                .build();
    }

    /**
     * 获取指定类型的失误列表
     */
    private List<MistakeStatsDto> getMistakesByType(List<Long> gameIds, MoveClassification type) {
        List<AnalysisResult> results = analysisResultRepository
                .findByGameIdInAndMoveClassification(gameIds, type);

        List<MistakeStatsDto> mistakes = new ArrayList<>();

        for (AnalysisResult result : results) {
            // 获取棋局信息
            GamePgn game = gamePgnRepository.findById(result.getGameId()).orElse(null);
            if (game == null) continue;

            // 计算评分损失
            Integer scoreDrop = calculateScoreDrop(result);

            MistakeStatsDto dto = MistakeStatsDto.builder()
                    .gameId(result.getGameId())
                    .whiteName(game.getWhitePlayer())
                    .blackName(game.getBlackPlayer())
                    .result(game.getGameResult())
                    .playedAt(game.getUploadedAt())  // 使用上传时间代替对局时间
                    .moveNumber(result.getMoveNumber())
                    .classification(result.getMoveClassification())
                    .move(result.getMoveSan())
                    .bestMove(result.getBestMove())
                    .scoreBefore(getPreviousScore(result))
                    .scoreAfter(parseScore(result.getScore()))
                    .scoreDrop(scoreDrop)
                    .fenPosition("")  // TODO: Entity 中需要添加 FEN 字段
                    .build();

            mistakes.add(dto);
        }

        // 按评分损失排序（最严重的失误在前）
        mistakes.sort((a, b) -> Integer.compare(
                Math.abs(b.getScoreDrop()),
                Math.abs(a.getScoreDrop())
        ));

        return mistakes;
    }

    /**
     * 计算评分损失
     */
    private Integer calculateScoreDrop(AnalysisResult result) {
        // 简化计算：使用最佳走法评分与实际评分的差值
        // 实际应该对比前一步的评分
        Integer currentScore = parseScore(result.getScore());
        Integer previousScore = getPreviousScore(result);

        if (currentScore == null || previousScore == null) {
            return 0;
        }

        return previousScore - currentScore;  // 正值表示评分下降
    }

    /**
     * 解析评分字符串为整数
     */
    private Integer parseScore(String scoreStr) {
        if (scoreStr == null || scoreStr.isEmpty()) {
            return 0;
        }
        try {
            // 处理类似 "+1.5" 或 "-2.3" 的格式
            return (int) (Double.parseDouble(scoreStr) * 100);
        } catch (NumberFormatException e) {
            // 尝试从字符串中提取数字部分（去除非数字字符），例如 "(+1.2)" 或 "score:+1.2"
            String cleaned = scoreStr.replaceAll("[^0-9\\-.]", "");
            if (cleaned.isEmpty()) {
                return 0;
            }
            try {
                return (int) (Double.parseDouble(cleaned) * 100);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    /**
     * 获取前一步的评分（简化版）
     */
    private Integer getPreviousScore(AnalysisResult result) {
        if (result.getMoveNumber() <= 1) {
            return 0;  // 开局评分为0
        }

        // 尝试获取前一步的评分
        return analysisResultRepository
                .findByGameIdAndMoveNumber(result.getGameId(), result.getMoveNumber() - 1)
                .map(r -> parseScore(r.getScore()))
                .orElse(0);
    }

    /**
     * 创建空响应
     */
    private MistakeListResponse createEmptyResponse(Long userId) {
        return MistakeListResponse.builder()
                .userId(userId)
                .totalMistakes(0)
                .mistakesByType(new HashMap<>())
                .mistakes(new ArrayList<>())
                .totalBlunders(0)
                .totalMistakesCount(0)
                .totalInaccuracies(0)
                .build();
    }
}
