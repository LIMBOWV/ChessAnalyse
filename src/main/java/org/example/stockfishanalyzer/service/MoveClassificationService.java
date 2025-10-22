package org.example.stockfishanalyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 启发式走法分类算法服务（项目核心创新点）
 *
 * 职责：基于 Stockfish 返回的原始评分，通过启发式规则引擎自动标注每步棋的分类
 *
 * 算法核心：
 * 1. 计算"评估损失" (Evaluation Loss): Delta = Score_A - Score_B
 * 2. 基于可配置的阈值对 Delta 进行分类
 * 3. 特殊处理"妙手"判定：最佳走法 + 远优于次佳
 */
@Slf4j
@Service
public class MoveClassificationService {

    // 可配置的分类阈值（单位：厘兵，centipawn）
    @Value("${chess.classification.blunder-threshold:300}")
    private int blunderThreshold;

    @Value("${chess.classification.mistake-threshold:150}")
    private int mistakeThreshold;

    @Value("${chess.classification.inaccuracy-threshold:50}")
    private int inaccuracyThreshold;

    @Value("${chess.classification.brilliant-threshold:100}")
    private int brilliantThreshold;

    /**
     * 对走法进行分类
     *
     * @param actualMoveScore 用户实际走法的评分（厘兵）
     * @param bestMoveScore AI 认为的最佳走法评分（厘兵）
     * @param secondBestScore AI 认为的次佳走法评分（可选，用于判定妙手）
     * @return 走法分类
     */
    public MoveClassification classifyMove(int actualMoveScore, int bestMoveScore, Integer secondBestScore) {
        // 计算评估损失（从当前方的角度）
        int evaluationLoss = Math.abs(bestMoveScore - actualMoveScore);

        log.debug("走法分类 - 实际评分: {}, 最佳评分: {}, 次佳评分: {}, 损失: {}",
                  actualMoveScore, bestMoveScore, secondBestScore, evaluationLoss);

        // 判断是否为最佳走法
        boolean isBestMove = evaluationLoss < 10; // 容忍 10 厘兵的误差

        // 妙手判定：必须是最佳走法，且远优于次佳
        if (isBestMove && secondBestScore != null) {
            int advantageOverSecond = Math.abs(bestMoveScore - secondBestScore);
            if (advantageOverSecond >= brilliantThreshold) {
                log.debug("判定为妙手！优于次佳 {} 厘兵", advantageOverSecond);
                return MoveClassification.BRILLIANT;
            }
        }

        // 最佳走法（但不够妙手标准）
        if (isBestMove) {
            return MoveClassification.BEST;
        }

        // 根据评估损失分类
        if (evaluationLoss >= blunderThreshold) {
            return MoveClassification.BLUNDER;
        } else if (evaluationLoss >= mistakeThreshold) {
            return MoveClassification.MISTAKE;
        } else if (evaluationLoss >= inaccuracyThreshold) {
            return MoveClassification.INACCURACY;
        } else {
            return MoveClassification.GOOD;
        }
    }

    /**
     * 将评分字符串转换为厘兵整数
     *
     * @param scoreStr 评分字符串（如 "+120", "-50", "M5"）
     * @return 厘兵值（将死视为 10000 或 -10000）
     */
    public int parseScoreToCentipawns(String scoreStr) {
        if (scoreStr == null || scoreStr.isEmpty()) {
            return 0;
        }

        // 处理将死评分（如 "M5" 或 "M-5"）
        if (scoreStr.startsWith("M")) {
            String mateValue = scoreStr.substring(1);
            try {
                int mateIn = Integer.parseInt(mateValue);
                // 正数表示己方将死对方，负数表示对方将死己方
                return mateIn > 0 ? 10000 : -10000;
            } catch (NumberFormatException e) {
                log.warn("无法解析将死评分: {}", scoreStr);
                return 0;
            }
        }

        // 处理普通评分（如 "+120", "-50"）
        try {
            return Integer.parseInt(scoreStr.replace("+", ""));
        } catch (NumberFormatException e) {
            log.warn("无法解析评分: {}", scoreStr);
            return 0;
        }
    }

    /**
     * 完整的走法分类流程（带评分解析）
     */
    public MoveClassification classifyMove(String actualScoreStr, String bestScoreStr, String secondBestScoreStr) {
        int actualScore = parseScoreToCentipawns(actualScoreStr);
        int bestScore = parseScoreToCentipawns(bestScoreStr);
        Integer secondBestScore = secondBestScoreStr != null ? parseScoreToCentipawns(secondBestScoreStr) : null;

        return classifyMove(actualScore, bestScore, secondBestScore);
    }
}
