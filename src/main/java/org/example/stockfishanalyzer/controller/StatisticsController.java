package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.UserStatisticsDto;
import org.example.stockfishanalyzer.service.UserStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final UserStatisticsService userStatisticsService;

    /**
     * 获取用户统计信息
     * GET /api/statistics/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserStatisticsDto> getUserStatistics(@PathVariable Long userId) {
        UserStatisticsDto stats = userStatisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 刷新用户统计信息（重新计算）
     * POST /api/statistics/{userId}/refresh
     */
    @PostMapping("/{userId}/refresh")
    public ResponseEntity<UserStatisticsDto> refreshStatistics(@PathVariable Long userId) {
        UserStatisticsDto stats = userStatisticsService.refreshStatistics(userId);
        return ResponseEntity.ok(stats);
    }
}

