# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 的国际象棋复盘分析系统（代号：Zeus），核心功能是通过后端集成 Stockfish AI 引擎，对用户上传的 PGN 棋谱进行异步分析，并提供可视化复盘功能。这是一个毕业设计项目，目标是实现 8 表 15 页面的完整系统。

### 技术栈
- **后端**: Spring Boot 3.5.6 + Spring Data JPA + Spring Async
- **数据库**: MySQL 8.x (数据库名: `Chess`)
- **AI 引擎**: Stockfish (通过进程间通信 IPC/UCI 协议调用)
- **前端**: Vue 3 + chessboard.js + chess.js + ECharts
- **构建工具**: Maven Wrapper (`./mvnw`)
- **认证**: JWT + BCrypt 密码加密

### 关键架构设计

1. **异步分析架构**: 使用 `@Async` 实现后台异步分析，用户上传 PGN 后立即返回，分析在后台进行
   - 实现位置：`GameAnalysisService.java:42` (`analyzeGameAsync` 方法)
   - 配置：`AsyncConfiguration.java` + `application.properties` 线程池配置
   - **重要**：`@Async` 方法必须通过 Spring 代理调用才能生效（从另一个 Bean 调用）

2. **进程间通信 (IPC)**: Java 后端通过 `ProcessBuilder` 启动 Stockfish 外部进程，使用 UCI 协议通信
   - 实现位置：`StockfishService.java`
   - 生命周期：`@PostConstruct` 初始化引擎，`@PreDestroy` 清理资源
   - 单例模式：全局共享一个 Stockfish 进程（注意并发安全）

3. **分析结果缓存**: 相同棋局由不同用户上传时无需重复分析，直接从数据库返回结果
   - 实现位置：`GameAnalysisService.java:57` (`existsByGameId` 检查)
   - 性能优化：批量保存结果使用 `saveAll()`

4. **启发式走法分类算法**（自研核心创新）: 基于 Stockfish 返回的原始评分，通过启发式规则引擎自动标注每步棋
   - 实现位置：`MoveClassificationService.java`
   - 分类类型：BRILLIANT（妙手）、GOOD（好棋）、INACCURACY（不精确）、MISTAKE（失误）、BLUNDER（大漏着）
   - 阈值配置：可在 `application.properties` 中调整分类阈值

5. **JWT 认证系统**: 无状态 Token 认证，支持用户注册、登录、Token 验证
   - 实现位置：`AuthService.java`, `AuthController.java`, `JwtUtil.java`
   - Token 有效期：7 天（可在 JwtUtil 中配置）
   - 密码加密：使用 BCrypt（Spring Security Crypto）

## 常用开发命令

### 启动应用
```bash
# 使用启动脚本（推荐，包含环境检查）
./start.sh

# 或直接使用 Maven
./mvnw spring-boot:run

# 清理并重新编译
./mvnw clean install

# 跳过测试快速启动
./mvnw spring-boot:run -DskipTests
```

### 数据库操作
```bash
# 连接到 MySQL 数据库
mysql -u root -p Chess

# 查看所有表
SHOW TABLES;

# 查看特定表结构
DESCRIBE tbl_game_pgn;
DESCRIBE tbl_analysis_result;

# 查看分析任务状态
SELECT id, white_player, black_player, analysis_status FROM tbl_game_pgn;

# 查看用户统计数据
SELECT u.username, us.* FROM tbl_user_statistics us
JOIN tbl_user u ON us.user_id = u.id;
```

### 测试
```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=StockfishAnalyzerApplicationTests

# 跳过测试
./mvnw install -DskipTests

# 测试 API（使用提供的脚本）
./test-auth-apis.sh       # 测试认证 API
./test-complete.sh         # 完整功能测试
```

### 访问应用
- **前端主页**: http://localhost:9090/
- **用户认证测试页**: http://localhost:9090/test-auth.html
- **Dashboard页面**: http://localhost:9090/dashboard.html
- **开局分析页**: http://localhost:9090/openings.html
- **失误统计页**: http://localhost:9090/mistakes.html
- **健康检查**: http://localhost:9090/actuator/health
- **应用信息**: http://localhost:9090/actuator/info

## 数据库设计

### 核心表结构（已实现）
- `tbl_user`: 用户表（含 email, password, createdAt）
- `tbl_game_pgn`: 棋局表，存储 PGN 原文、对局信息、分析状态
  - 关键字段：`analysis_status` (PENDING/PROCESSING/COMPLETED/FAILED)
- `tbl_analysis_result`: 分析结果表，存储每步的 AI 评分、最佳走法、走法分类
  - 复合索引：`(game_id, move_number)` 用于快速查询

### 扩展表结构（已实现）
- `tbl_opening_book`: 开局库表（ECO 编码、开局名称、走法序列）
- `tbl_user_statistics`: 用户统计聚合表（胜率、准确度、走法分类统计）
- `tbl_game_tag` + `tbl_game_tag_relation`: 棋局标签系统（多对多关系）
- `tbl_position_bookmark`: 局面书签表（收藏关键局面）

**详细表结构和字段定义见 `项目详细任务书.md`**

## 核心技术实现

### 1. Stockfish UCI 协议交互
**实现位置**: `StockfishService.java`

- **UCI 协议交互流程**:
  - 初始化: `uci` → 等待 `uciok` → `isready` → 等待 `readyok`
  - 分析: `position fen <fen>` 或 `position startpos moves <moves>` → `go movetime 1000` → 解析 `info`/`bestmove`
  - **重要**：每次分析前需要发送 `isready` 确保引擎就绪

- **评分解析**:
  - 厘兵评分 (centipawn): `score cp 120` → "+120" (白方优势 1.2 兵)
  - 将死评分: `score mate 5` → "M5" (5步将杀)
  - 负数评分表示黑方优势

- **配置参数** (application.properties):
  - `stockfish.engine.path`: Stockfish 可执行文件路径（必需）
  - `stockfish.analysis.movetime`: 单步分析时间（毫秒，默认 1000）
  - `stockfish.analysis.depth`: 分析深度（默认 18）

### 2. 异步分析服务
**实现位置**: `GameAnalysisService.java` + `AsyncConfiguration.java`

- **异步配置**:
  - `@EnableAsync` 启用异步支持（AsyncConfiguration.java）
  - 线程池配置见 `application.properties`:
    - `spring.task.execution.pool.core-size=5` (核心线程数)
    - `spring.task.execution.pool.max-size=10` (最大线程数)
    - `spring.task.execution.pool.queue-capacity=100` (队列容量)

- **分析流程**:
  1. 更新状态为 `PROCESSING`
  2. 检查是否已有缓存结果（`existsByGameId`）- 避免重复分析
  3. 逐步构建走法序列并调用 Stockfish
  4. 批量保存结果（`saveAll` 提升性能）
  5. 更新状态为 `COMPLETED` 或 `FAILED`

- **事务管理**:
  - `@Transactional` 确保数据一致性
  - 异步方法中的事务与主线程事务隔离
  - 异常会导致事务回滚，状态更新为 `FAILED`

### 3. 走法分类算法核心逻辑
**实现位置**: `MoveClassificationService.java`

- **分类阈值**（可在 `application.properties` 配置）:
  - `blunder-threshold: 300` (大漏着)
  - `mistake-threshold: 150` (失误)
  - `inaccuracy-threshold: 50` (不精确)
  - `brilliant-threshold: 100` (妙手判定的优势阈值)

- **分类逻辑**:
  1. 计算评估损失: `evaluationLoss = |bestMoveScore - actualMoveScore|`
  2. 判断是否为最佳走法: `evaluationLoss < 10`
  3. 妙手判定: 最佳走法 + 优于次佳 ≥ 100 厘兵
  4. 根据损失值分类: BLUNDER > MISTAKE > INACCURACY > GOOD

- **评分转换**: `parseScoreToCentipawns()` 将字符串评分转为整数（厘兵）
  - 普通评分: "+120" → 120, "-50" → -50
  - 将死评分: "M5" → 10000, "M-5" → -10000
  - 无效评分: null/"invalid" → 0

### 4. PGN 处理
**实现位置**: `PgnParser.java` + `SimpleChessEngine.java`

- **PGN 格式**:
  - 标签部分：`[Event "..."]`, `[White "..."]` 等元数据
  - 走法部分：`1. e4 e5 2. Nf3 Nc6 3. Bc4` 等 SAN 格式走法

- **走法格式转换**:
  - **SAN (Standard Algebraic Notation)**: `Nf3`, `O-O`, `exd5+` (人类可读)
  - **UCI (Universal Chess Interface)**: `e2e4`, `e1g1`, `e4d5` (引擎使用)
  - `SimpleChessEngine` 负责 SAN → UCI 转换，需要维护完整棋盘状态

- **已知限制**:
  - `PgnParser.sanToUci()` 未完全实现（需要完整的走法合法性验证）
  - 复杂的 PGN 特性（变着、注释、NAG）未完全支持

### 5. 用户统计服务
**实现位置**: `UserStatisticsService.java`

- **统计指标**:
  - 总对局数、胜/平/负数
  - 平均准确度、失误/漏着/妙手总数
  - 最常用开局（如有开局数据）

- **更新时机**:
  - 分析完成后自动更新（在 GameAnalysisService 中调用）
  - 使用事务确保统计数据一致性

## 环境配置

### 必需的环境依赖
1. **Java 17**: 项目使用 Java 17，确保 `java -version` 显示 17+
2. **MySQL 8.x**: 本地或远程 MySQL 服务器
3. **Stockfish 引擎**: UCI 兼容的国际象棋引擎

### Stockfish 安装和配置
```bash
# macOS
brew install stockfish
which stockfish  # 输出：/opt/homebrew/bin/stockfish 或 /usr/local/bin/stockfish

# Linux
apt-get install stockfish
which stockfish

# 更新配置文件
# 编辑 src/main/resources/application.properties
# stockfish.engine.path=/opt/homebrew/bin/stockfish  (macOS Homebrew)
# 或 stockfish.engine.path=/usr/local/bin/stockfish (macOS Intel)
```

### MySQL 数据库配置
```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE Chess CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# 2. 更新 application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/Chess?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码

# 3. JPA 会自动创建表结构（ddl-auto=update）
```

### 首次启动检查清单
- [ ] MySQL 服务运行中（`brew services list` 或 `systemctl status mysql`）
- [ ] 数据库 `Chess` 已创建
- [ ] `application.properties` 中的数据库密码正确
- [ ] Stockfish 已安装且路径配置正确
- [ ] 端口 9090 未被占用

## 性能优化

- **分析时间控制**: 配置 `stockfish.analysis.movetime=1000`（毫秒），确保单步分析时间可控
- **批量保存**: `GameAnalysisService` 使用 `saveAll()` 批量保存分析结果
- **分析结果缓存**: 通过 `existsByGameId()` 检查避免重复分析
- **数据库索引**: 确保以下索引存在：
  - `tbl_game_pgn.user_id`
  - `tbl_analysis_result(game_id, move_number)` 复合索引
  - `tbl_user_statistics.user_id`
- **异步线程池**: 根据服务器性能调整 `application.properties` 中的线程池配置

## 调试和日志

- **日志级别**: 使用 Lombok 的 `@Slf4j` 注解，支持 SLF4J 日志
- **重要日志位置**:
  - `GameAnalysisService`: 分析流程的开始、进度、完成/失败
  - `StockfishService`: UCI 命令交互、引擎输出
  - `PgnService`: PGN 上传和解析
  - `UserStatisticsService`: 统计数据更新

- **调试技巧**:
  ```properties
  # 在 application.properties 中开启 SQL 日志
  spring.jpa.show-sql=true
  spring.jpa.properties.hibernate.format.sql=true

  # 调整日志级别
  logging.level.org.example.stockfishanalyzer=DEBUG
  logging.level.org.hibernate.SQL=DEBUG
  ```

## 常见问题排查

### 1. Stockfish 初始化失败
**症状**: 日志显示 "Stockfish 引擎初始化失败"
```bash
# 检查路径
which stockfish
# 更新 application.properties 中的 stockfish.engine.path
```

### 2. 分析状态一直是 PENDING
**可能原因**: Stockfish 未正确启动 / 异步线程池配置问题
**排查**: 检查后端日志，查找异常堆栈信息

### 3. 数据库连接失败
**症状**: 启动时报 "Communications link failure"
```bash
# 检查 MySQL 服务状态
brew services list  # macOS
# 测试数据库连接
mysql -u root -p -h localhost Chess
```

### 4. JWT Token 验证失败
**可能原因**: Token 过期或密钥不匹配
**排查**: 检查 JwtUtil 中的密钥配置，确保生成和验证使用相同密钥

## 前端开发要点

- **主界面**: `src/main/resources/static/index.html` - 完整的单页面应用
- **技术栈**: Vue 3 (CDN) + chessboard.js + chess.js + ECharts
- **访问方式**: 启动后端后直接访问 `http://localhost:9090/`
- **核心功能**:
  - PGN 上传（拖放/选择/粘贴）
  - 交互式棋盘可视化
  - 走法分类图标展示（✨妙手 / 😱漏着 / 😊好棋等）
  - 评估曲线图表（ECharts）
  - 用户统计 Dashboard（胜率、准确度、走法分类）
  - 开局分析（开局识别、胜率统计）
  - 失误统计（按类型和阶段分类）

- **API 调用**: 所有请求通过 `/api/*` 端点，已配置 CORS 支持
- **详细文档**: 见 `README_FRONTEND.md` 和 `FRONTEND_STATUS.md`

## 代码规范和最佳实践

### 后端开发规范
1. **使用 Lombok**: 简化 getter/setter、构造函数、日志等样板代码
   - `@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor` 等
2. **DTO 模式**: 控制器返回 DTO 而非直接返回 Entity，避免循环引用和隐私泄露
3. **事务管理**: 数据修改操作使用 `@Transactional`，查询使用 `@Transactional(readOnly = true)`
4. **异步操作**: 长时间运行的任务使用 `@Async`，注意必须从外部 Bean 调用
5. **异常处理**: 使用统一的异常处理机制，返回友好的错误信息

### 数据库设计规范
1. **命名规范**: 表名使用 `tbl_` 前缀，字段使用下划线命名（snake_case）
2. **字符集**: 所有表使用 `utf8mb4` 字符集以支持 Emoji 和特殊字符
3. **索引**: 为外键和频繁查询的字段添加索引
4. **时间戳**: 使用 `@CreatedDate` 和 `@LastModifiedDate` 自动管理时间戳

### 前端开发规范
1. **组件化**: 将复杂功能拆分为可复用的 Vue 组件
2. **API 调用**: 统一使用 Axios，处理错误和 Token 认证
3. **状态管理**: 使用 Pinia 或 Vuex 管理全局状态（如用户登录状态）
4. **响应式设计**: 确保页面在不同设备上都能正常显示

## 项目当前状态

### 已完成模块（✅ Day 1-4）
- ✅ Spring Boot 基础框架搭建
- ✅ MySQL 数据库设计（8 张表）
- ✅ Stockfish IPC 服务封装（UCI 协议）
- ✅ PGN 解析工具
- ✅ 启发式走法分类算法
- ✅ 异步分析服务
- ✅ JWT 认证系统（注册/登录/验证）
- ✅ 前端 Vue 3 应用（棋盘、分析展示、评估曲线）
- ✅ 用户统计 Dashboard
- ✅ 开局分析服务和页面
- ✅ 失误统计服务和页面

### 待开发模块
- ⏳ 完整的前端路由系统（Vue Router）
- ⏳ 局面收藏功能（后端 + 前端）
- ⏳ 棋局标签管理（后端 + 前端）
- ⏳ 历史趋势分析页面
- ⏳ 对比分析页面（双棋盘对比）
- ⏳ 系统设置页面
- ⏳ 分析任务管理页面

详细的开发进度和计划见 `毕设开发计划书.md` 和各天的开发报告。

## 参考文档

- **项目规划**: `项目详细任务书.md`, `毕设开发计划书.md`
- **技术文档**: `BUILD_REPORT.md`, `API_TEST_REPORT.md`
- **前端文档**: `README_FRONTEND.md`, `FRONTEND_STATUS.md`
- **开发日志**: `第一天开发报告.md` ~ `第四天开发报告.md`

---

**最后更新**: 2025-01-24
**当前进度**: 核心功能 + 认证系统 + 统计分析（Day 1-4 完成）
