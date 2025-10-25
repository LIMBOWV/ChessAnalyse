package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.UpdateSettingsRequest;
import org.example.stockfishanalyzer.dto.UserSettingsDTO;
import org.example.stockfishanalyzer.service.UserSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final UserSettingsService userSettingsService;
    
    /**
     * 获取用户设置
     */
    @GetMapping
    public ResponseEntity<UserSettingsDTO> getUserSettings(@RequestParam Long userId) {
        UserSettingsDTO settings = userSettingsService.getUserSettings(userId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 更新用户设置
     */
    @PutMapping
    public ResponseEntity<UserSettingsDTO> updateUserSettings(
            @RequestParam Long userId,
            @RequestBody UpdateSettingsRequest request) {
        UserSettingsDTO settings = userSettingsService.updateUserSettings(userId, request);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 重置为默认设置
     */
    @PostMapping("/reset")
    public ResponseEntity<UserSettingsDTO> resetToDefault(@RequestParam Long userId) {
        UserSettingsDTO settings = userSettingsService.resetToDefault(userId);
        return ResponseEntity.ok(settings);
    }
}
