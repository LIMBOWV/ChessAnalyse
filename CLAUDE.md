# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 的国际象棋复盘分析系统（代号：Zeus），核心功能是通过后端集成 Stockfish AI 引擎，对用户上传的 PGN 棋谱进行异步分析，并提供可视化复盘功能。这是一个私有化部署方案，解决了现有在线服务的隐私问题和浏览器端分析的性能瓶颈。

## 核心架构特点

### 技术栈
- **后端**: Spring Boot 3.x + Spring Data JPA + Spring Async
- **数据库**: MySQL 8.x
- **AI 引擎**: Stockfish (通过进程间通信 IPC/UCI 协议调用)
- **前端**: Vue 3 + chessboard.js (计划中)
- **构建工具**: Maven

### 关键架构设计
1. **异步分析架构**: 使用 `@Async` 实现后台异步分析，用户上传 PGN 后立即返回，分析在后台进行
2. **进程间通信 (IPC)**: Java 后端通过 `ProcessBuilder` 启动 Stockfish 外部进程，使用 UCI 协议通信
3. **分析结果缓存**: 相同棋局由不同用户上传时无需重复分析，直接从数据库返回结果

### 核心创新点
**启发式走法分类算法**（自研）: 基于 Stockfish 返回的原始评分，通过启发式规则引擎自动标注每步棋为"妙手 (Brilliant)"、"好棋 (Good)"、"失误 (Mistake)"、"大漏着 (Blunder)"等分类，实现类似 Chess.com 的游戏报告体验。

## 数据库设计

### 核心表结构
- `tbl_user`: 用户表
- `tbl_game_pgn`: 棋局表，存储 PGN 原文、对局信息、分析状态
- `tbl_analysis_result`: 分析结果表，存储每步的 AI 评分、最佳走法、走法分类

### 关系
- 一个用户可以上传多个棋局（一对多）
- 一个棋局包含多条分析结果（一对多，因为一局棋有多步）

完整的数据库 Schema SQL 见项目文档 `项目详细任务书.md` 的附录 B。

## 常用开发命令

### 构建与运行
```bash
# 编译项目
./mvnw clean compile

# 打包项目
./mvnw clean package

# 运行应用
./mvnw spring-boot:run

# 跳过测试打包
./mvnw clean package -DskipTests
```

### 测试
```bash
# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=StockfishAnalyzerApplicationTests

# 运行单个测试方法
./mvnw test -Dtest=StockfishAnalyzerApplicationTests#contextLoads
```

### 数据库
```bash
# 项目使用 MySQL 8.x，需要先创建数据库
# 连接信息在 src/main/resources/application.properties 中配置
# 初始化脚本见项目文档的附录 B
```

## 关键技术实现要点

### 1. Stockfish IPC 集成 (核心难点)
- **风险**: 进程 I/O 缓冲区阻塞问题
- **解决方案**: 必须使用独立线程异步消费 `InputStream` 和 `ErrorStream`，避免 `Process.waitFor()` 阻塞
- **UCI 协议**: 需要正确解析 `info`、`bestmove` 等输出

### 2. 异步分析服务
- 使用 `@Async` 注解实现异步任务
- 分析流程：遍历每一步 → 调用 Stockfish → 获取评分和最佳走法 → 调用走法分类算法 → 存储结果
- 分析完成后更新 `tbl_game_pgn` 的 `analysis_status` 为 `COMPLETED`

### 3. 走法分类算法核心逻辑
- **输入**: `Score_A` (AI 最佳走法分数), `Score_B` (用户实际走法分数), `Score_C` (次佳走法分数)
- **核心**: 计算评估损失 `Delta = Score_A - Score_B`
- **分类**: 基于阈值判断（如 `blunder_threshold = 3.0`, `mistake_threshold = 1.5`）
- **妙手判定**: 最佳走法 + 收益远高于次佳 + 可能涉及弃子

### 4. PGN 处理
- 需要选择鲁棒的 PGN 解析库（如 `java-pgn-parser`）
- 做好异常处理，因为网络下载的 PGN 格式可能不标准

## 开发注意事项

### 性能优化
- **分析时间控制**: 使用 `go movetime 1000` 而非 `go depth 18`，确保单步分析时间可控（避免深度分析导致等待过长）
- **数据库索引**: `tbl_game_pgn.user_id` 和 `tbl_analysis_result` 的 `(game_id, move_number)` 复合索引是性能关键

### 前端集成要点（计划中）
- 前端在展示每一步时，需同时获取并显示 `move_classification` 字段
- 使用图标直观展示分类：妙手 ✨、漏着 😱、失误 😟
- 可选：使用 ECharts 绘制全局局势评分曲线

## 项目当前状态

项目处于初始化阶段，已完成：
- Spring Boot 基础框架搭建
- Maven 依赖配置（Spring Data JPA, MySQL, Lombok）
- 基础应用入口类

待开发模块：
1. 数据库 Schema 创建和 JPA Entity 定义
2. Stockfish IPC 服务封装（核心）
3. PGN 上传与解析 API
4. 异步分析服务
5. 走法分类算法实现
6. 前端 Vue 应用

## 参考文档

详细的项目需求、WBS 任务分解、风险预案见 `项目详细任务书.md`。
