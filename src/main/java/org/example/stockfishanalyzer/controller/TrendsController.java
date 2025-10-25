package org.example.stockfishanalyzer.controller;

import org.example.stockfishanalyzer.dto.TrendsDTO;
import org.example.stockfishanalyzer.service.TrendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 趋势分析控制器
 */
@RestController
@RequestMapping("/api/trends")
@CrossOrigin(origins = "*")
public class TrendsController {
    
    @Autowired
    private TrendsService trendsService;
    
    /**
     * 获取趋势分析数据
     * 
     * @param userId 用户ID
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return 趋势分析数据
     */
    @GetMapping
    public TrendsDTO getTrends(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return trendsService.getTrends(userId, startDate, endDate);
    }
}
