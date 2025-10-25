package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.GameCompareResponse;
import org.example.stockfishanalyzer.dto.TrendsAnalysisResponse;
import org.example.stockfishanalyzer.service.GameCompareService;
import org.example.stockfishanalyzer.service.TrendsAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 高级分析控制器
 * 处理棋局对比和历史趋势分析
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdvancedAnalysisController {
    
    private final GameCompareService gameCompareService;
    private final TrendsAnalysisService trendsAnalysisService;
    
    /**
     * 对比两个棋局
     * 
     * @param gameId1 第一个棋局ID
     * @param gameId2 第二个棋局ID
     * @return 对比分析结果
     */
    @GetMapping("/compare/{gameId1}/{gameId2}")
    public ResponseEntity<GameCompareResponse> compareGames(
            @PathVariable Long gameId1,
            @PathVariable Long gameId2) {
        
        log.info("收到棋局对比请求: {} vs {}", gameId1, gameId2);
        
        try {
            GameCompareResponse response = gameCompareService.compareGames(gameId1, gameId2);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("棋局对比失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取用户的历史趋势分析
     * 
     * @param userId 用户ID
     * @param period 周期: month(月度), quarter(季度), year(年度)
     * @return 历史趋势数据
     */
    @GetMapping("/trends/{userId}")
    public ResponseEntity<TrendsAnalysisResponse> getTrends(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "month") String period) {
        
        log.info("收到历史趋势请求: userId={}, period={}", userId, period);
        
        try {
            TrendsAnalysisResponse response = trendsAnalysisService.getTrends(userId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取历史趋势失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
