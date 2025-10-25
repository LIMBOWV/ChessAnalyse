package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.MistakeListResponse;
import org.example.stockfishanalyzer.enums.MoveClassification;
import org.example.stockfishanalyzer.service.MistakeAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 失误分析控制器
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MistakeController {

    private final MistakeAnalysisService mistakeAnalysisService;

    /**
     * 获取用户的失误统计
     * GET /api/analysis/mistakes/{userId}
     * 可选参数：type (BLUNDER/MISTAKE/INACCURACY)
     */
    @GetMapping("/mistakes/{userId}")
    public ResponseEntity<MistakeListResponse> getUserMistakes(
            @PathVariable Long userId,
            @RequestParam(required = false) String type) {

        MoveClassification classification = null;
        if (type != null) {
            try {
                classification = MoveClassification.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的类型参数
            }
        }

        MistakeListResponse mistakes = mistakeAnalysisService.getUserMistakes(userId, classification);
        return ResponseEntity.ok(mistakes);
    }
}

