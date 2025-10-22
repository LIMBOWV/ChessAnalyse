package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.AnalysisResultDto;
import org.example.stockfishanalyzer.dto.PgnUploadResponse;
import org.example.stockfishanalyzer.entity.AnalysisResult;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.service.GameAnalysisService;
import org.example.stockfishanalyzer.service.PgnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PGN 控制器
 * 提供 PGN 上传和分析结果查询的 API
 */
@Slf4j
@RestController
@RequestMapping("/api/pgn")
@RequiredArgsConstructor
public class PgnController {

    private final PgnService pgnService;
    private final GameAnalysisService analysisService;

    /**
     * 上传 PGN 文件
     *
     * POST /api/pgn/upload
     * Content-Type: text/plain
     * Body: PGN 内容
     */
    @PostMapping("/upload")
    public ResponseEntity<PgnUploadResponse> uploadPgn(
            @RequestBody String pgnContent,
            @RequestParam(defaultValue = "1") Long userId) {

        log.info("收到 PGN 上传请求，用户 ID: {}", userId);

        PgnUploadResponse response = pgnService.uploadPgn(pgnContent, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的所有棋局
     *
     * GET /api/pgn/games?userId=1
     */
    @GetMapping("/games")
    public ResponseEntity<List<GamePgn>> getUserGames(
            @RequestParam(defaultValue = "1") Long userId) {

        List<GamePgn> games = pgnService.getUserGames(userId);
        return ResponseEntity.ok(games);
    }

    /**
     * 获取棋局详情
     *
     * GET /api/pgn/game/{gameId}
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<GamePgn> getGame(@PathVariable Long gameId) {
        GamePgn game = pgnService.getGame(gameId);
        return ResponseEntity.ok(game);
    }

    /**
     * 获取棋局的所有分析结果
     *
     * GET /api/pgn/analysis/{gameId}
     */
    @GetMapping("/analysis/{gameId}")
    public ResponseEntity<List<AnalysisResultDto>> getGameAnalysis(
            @PathVariable Long gameId) {

        List<AnalysisResult> results = analysisService.getGameAnalysis(gameId);

        List<AnalysisResultDto> dtos = results.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * 获取特定步数的分析结果
     *
     * GET /api/pgn/analysis/{gameId}/{moveNumber}
     */
    @GetMapping("/analysis/{gameId}/{moveNumber}")
    public ResponseEntity<AnalysisResultDto> getMoveAnalysis(
            @PathVariable Long gameId,
            @PathVariable Integer moveNumber) {

        AnalysisResult result = analysisService.getMoveAnalysis(gameId, moveNumber);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(result));
    }

    /**
     * 转换为 DTO
     */
    private AnalysisResultDto toDto(AnalysisResult result) {
        return new AnalysisResultDto(
                result.getMoveNumber(),
                result.getMoveSan(),
                result.getScore(),
                result.getBestMove(),
                result.getMoveClassification()
        );
    }
}
