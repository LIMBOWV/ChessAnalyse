package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.*;
import org.example.stockfishanalyzer.entity.User;
import org.example.stockfishanalyzer.repository.UserRepository;
import org.example.stockfishanalyzer.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务 - 处理用户注册、登录、Token 验证等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());

        // 生成 JWT Token
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername());

        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        log.info("用户登录请求: usernameOrEmail={}", request.getUsernameOrEmail());

        // 查找用户（支持用户名或邮箱登录）
        Optional<User> userOptional = userRepository.findByUsername(request.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(request.getUsernameOrEmail());
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户名或邮箱不存在");
        }

        User user = userOptional.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * 获取用户信息
     */
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setCreatedAt(user.getCreatedAt());

        return userDto;
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token, String username) {
        return jwtUtil.validateToken(token, username);
    }

    /**
     * 从 Token 中提取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }
}

