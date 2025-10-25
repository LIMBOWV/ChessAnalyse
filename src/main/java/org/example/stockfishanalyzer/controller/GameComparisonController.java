package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.GameComparisonDTO;
import org.example.stockfishanalyzer.service.GameComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 棋局对比分析控制器
 */
@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
public class GameComparisonController {

    private final GameComparisonService comparisonService;

    /**
     * 对比两场棋局
     * GET /api/comparison?gameId1=1&gameId2=2&userId=1
     */
    @GetMapping
    public ResponseEntity<GameComparisonDTO> compareGames(
            @RequestParam Long gameId1,
            @RequestParam Long gameId2,
            @RequestParam Long userId) {
        GameComparisonDTO comparison = comparisonService.compareGames(gameId1, gameId2, userId);
        return ResponseEntity.ok(comparison);
    }
}
