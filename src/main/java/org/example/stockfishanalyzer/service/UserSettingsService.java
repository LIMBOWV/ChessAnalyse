package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.UpdateSettingsRequest;
import org.example.stockfishanalyzer.dto.UserSettingsDTO;
import org.example.stockfishanalyzer.entity.UserSettings;
import org.example.stockfishanalyzer.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSettingsService {
    
    private final UserSettingsRepository userSettingsRepository;
    
    /**
     * 获取用户设置（如果不存在则创建默认设置）
     */
    public UserSettingsDTO getUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        return convertToDTO(settings);
    }
    
    /**
     * 更新用户设置
     */
    @Transactional
    public UserSettingsDTO updateUserSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        // 更新设置
        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getBoardTheme() != null) {
            settings.setBoardTheme(request.getBoardTheme());
        }
        if (request.getPieceSet() != null) {
            settings.setPieceSet(request.getPieceSet());
        }
        if (request.getAnalysisDepth() != null) {
            // 验证范围 10-30
            int depth = Math.max(10, Math.min(30, request.getAnalysisDepth()));
            settings.setAnalysisDepth(depth);
        }
        if (request.getEngineThreads() != null) {
            // 验证范围 1-8
            int threads = Math.max(1, Math.min(8, request.getEngineThreads()));
            settings.setEngineThreads(threads);
        }
        if (request.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getAutoAnalyze() != null) {
            settings.setAutoAnalyze(request.getAutoAnalyze());
        }
        
        settings.setUpdatedAt(LocalDateTime.now());
        
        UserSettings savedSettings = userSettingsRepository.save(settings);
        return convertToDTO(savedSettings);
    }
    
    /**
     * 重置为默认设置
     */
    @Transactional
    public UserSettingsDTO resetToDefault(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettings());
        
        settings.setUserId(userId);
        settings.setTheme("light");
        settings.setLanguage("zh-CN");
        settings.setBoardTheme("brown");
        settings.setPieceSet("default");
        settings.setAnalysisDepth(20);
        settings.setEngineThreads(4);
        settings.setNotificationsEnabled(true);
        settings.setAutoAnalyze(false);
        settings.setUpdatedAt(LocalDateTime.now());
        
        if (settings.getCreatedAt() == null) {
            settings.setCreatedAt(LocalDateTime.now());
        }
        
        UserSettings savedSettings = userSettingsRepository.save(settings);
        return convertToDTO(savedSettings);
    }
    
    /**
     * 创建默认设置
     */
    private UserSettings createDefaultSettings(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setTheme("light");
        settings.setLanguage("zh-CN");
        settings.setBoardTheme("brown");
        settings.setPieceSet("default");
        settings.setAnalysisDepth(20);
        settings.setEngineThreads(4);
        settings.setNotificationsEnabled(true);
        settings.setAutoAnalyze(false);
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
        
        return userSettingsRepository.save(settings);
    }
    
    /**
     * 转换为 DTO
     */
    private UserSettingsDTO convertToDTO(UserSettings settings) {
        UserSettingsDTO dto = new UserSettingsDTO();
        dto.setId(settings.getId());
        dto.setUserId(settings.getUserId());
        dto.setTheme(settings.getTheme());
        dto.setLanguage(settings.getLanguage());
        dto.setBoardTheme(settings.getBoardTheme());
        dto.setPieceSet(settings.getPieceSet());
        dto.setAnalysisDepth(settings.getAnalysisDepth());
        dto.setEngineThreads(settings.getEngineThreads());
        dto.setNotificationsEnabled(settings.getNotificationsEnabled());
        dto.setAutoAnalyze(settings.getAutoAnalyze());
        
        return dto;
    }
}
