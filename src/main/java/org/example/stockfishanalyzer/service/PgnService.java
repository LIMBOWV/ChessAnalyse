package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.PgnUploadResponse;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.enums.AnalysisStatus;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.util.PgnParser;
import org.example.stockfishanalyzer.util.SimpleChessEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * PGN 服务
 * 职责：处理 PGN 文件的上传、解析和入库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgnService {

    private final GamePgnRepository gamePgnRepository;
    private final GameAnalysisService analysisService;

    /**
     * 上传并解析 PGN 文件
     *
     * @param pgnContent PGN 内容
     * @param userId 用户 ID
     * @return 上传响应
     */
    @Transactional
    public PgnUploadResponse uploadPgn(String pgnContent, Long userId) {
        try {
            log.info("开始处理 PGN 上传，用户 ID: {}", userId);

            // 解析 PGN
            PgnParser.PgnGame game = PgnParser.parse(pgnContent);

            // 提取棋局信息
            String whitePlayer = game.getTag("White");
            String blackPlayer = game.getTag("Black");
            String result = game.getTag("Result");
            String date = game.getTag("Date");

            whitePlayer = whitePlayer != null ? whitePlayer : "Unknown";
            blackPlayer = blackPlayer != null ? blackPlayer : "Unknown";
            result = result != null ? result : "*";
            date = date != null ? date : "????.??.??";

            // 将 SAN 走法转换为 UCI 格式
            List<String> uciMoves = convertSanToUci(game.getMoves());

            log.info("解析 PGN 完成 - 白方: {}, 黑方: {}, 结果: {}, 走法数: {}",
                     whitePlayer, blackPlayer, result, uciMoves.size());

            // 保存棋局到数据库
            GamePgn gamePgn = new GamePgn();
            gamePgn.setUserId(userId);
            gamePgn.setPgnContent(pgnContent);
            gamePgn.setWhitePlayer(whitePlayer);
            gamePgn.setBlackPlayer(blackPlayer);
            gamePgn.setGameResult(result);
            gamePgn.setGameDate(date);
            gamePgn.setAnalysisStatus(AnalysisStatus.PENDING);

            gamePgn = gamePgnRepository.save(gamePgn);
            log.info("棋局已保存，ID: {}", gamePgn.getId());

            // 触发异步分析
            analysisService.analyzeGameAsync(gamePgn.getId(), uciMoves);

            return new PgnUploadResponse(
                    gamePgn.getId(),
                    "PGN 上传成功，分析已开始",
                    uciMoves.size()
            );

        } catch (Exception e) {
            log.error("处理 PGN 上传时发生错误", e);
            throw new RuntimeException("PGN 上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 SAN 走法列表转换为 UCI 格式
     */
    private List<String> convertSanToUci(List<String> sanMoves) {
        List<String> uciMoves = new ArrayList<>();
        SimpleChessEngine engine = new SimpleChessEngine();

        for (String san : sanMoves) {
            String uci = engine.sanToUci(san);
            uciMoves.add(uci);
            engine.makeUciMove(uci);
        }

        return uciMoves;
    }

    /**
     * 获取用户的所有棋局
     */
    @Transactional(readOnly = true)
    public List<GamePgn> getUserGames(Long userId) {
        return gamePgnRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    /**
     * 获取棋局详情
     */
    @Transactional(readOnly = true)
    public GamePgn getGame(Long gameId) {
        return gamePgnRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("棋局不存在: " + gameId));
    }
}
