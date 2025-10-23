package org.example.stockfishanalyzer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.*;
import org.example.stockfishanalyzer.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理用户注册、登录、个人信息等请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/profile?userId={userId}
     * 或从 Header 中的 Authorization Token 提取
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            Long actualUserId = userId;

            // 如果没有传 userId，尝试从 Token 中提取
            if (actualUserId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                actualUserId = authService.getUserIdFromToken(token);
            }

            if (actualUserId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "未提供用户 ID 或 Token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            UserDto userDto = authService.getUserProfile(actualUserId);
            return ResponseEntity.ok(userDto);
        } catch (RuntimeException e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 验证 Token
     * POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token 格式错误");
                return ResponseEntity.ok(response);
            }

            String token = authHeader.substring(7);
            Long userId = authService.getUserIdFromToken(token);
            UserDto user = authService.getUserProfile(userId);

            boolean isValid = authService.validateToken(token, user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("userId", userId);
            response.put("username", user.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token 无效或已过期");
            return ResponseEntity.ok(response);
        }
    }
}

