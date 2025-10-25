package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.OpeningListResponse;
import org.example.stockfishanalyzer.service.OpeningAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 开局分析控制器
 */
@RestController
@RequestMapping("/api/openings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OpeningController {

    private final OpeningAnalysisService openingAnalysisService;

    /**
     * 获取用户的开局统计
     * GET /api/openings/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<OpeningListResponse> getUserOpeningStats(@PathVariable Long userId) {
        OpeningListResponse stats = openingAnalysisService.getUserOpeningStats(userId);
        return ResponseEntity.ok(stats);
    }
}

