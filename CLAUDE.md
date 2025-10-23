# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 的国际象棋复盘分析系统（代号：Zeus），核心功能是通过后端集成 Stockfish AI 引擎，对用户上传的 PGN 棋谱进行异步分析，并提供可视化复盘功能。这是一个私有化部署方案，解决了现有在线服务的隐私问题和浏览器端分析的性能瓶颈。

## 核心架构特点

### 技术栈
- **后端**: Spring Boot 3.5.6 + Spring Data JPA + Spring Async
- **数据库**: MySQL 8.x
- **AI 引擎**: Stockfish (通过进程间通信 IPC/UCI 协议调用)
- **前端**: Vue 3 + chessboard.js + chess.js + ECharts (已完成)
- **构建工具**: Maven + Maven Wrapper

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

### 快速启动（推荐）
```bash
# 使用启动脚本（自动检查 MySQL 和 Stockfish）
./start.sh

# 启动后访问前端界面
open http://localhost:9090/
```

### 构建与运行
```bash
# 编译项目
./mvnw clean compile

# 打包项目
./mvnw clean package

# 运行应用（需要先启动 MySQL）
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

### 数据库管理
```bash
# macOS 启动/停止 MySQL
brew services start mysql
brew services stop mysql

# 创建数据库（首次使用）
mysql -u root -p -e "CREATE DATABASE Chess CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 连接数据库查看数据
mysql -u root -p Chess
```

### Stockfish 安装
```bash
# macOS
brew install stockfish
which stockfish  # 查看安装路径，更新到 application.properties

# Linux
apt-get install stockfish  # 或从源码编译
```

## 关键代码位置

### 核心服务
- **Stockfish 引擎服务**: `src/main/java/org/example/stockfishanalyzer/service/StockfishService.java`
  - UCI 协议实现、进程管理、评分解析
- **游戏分析服务**: `src/main/java/org/example/stockfishanalyzer/service/GameAnalysisService.java`
  - 异步分析流程、状态管理
- **走法分类服务**: `src/main/java/org/example/stockfishanalyzer/service/MoveClassificationService.java`
  - 启发式分类算法
- **PGN 服务**: `src/main/java/org/example/stockfishanalyzer/service/PgnService.java`
  - PGN 上传业务逻辑

### 工具类
- **PGN 解析器**: `src/main/java/org/example/stockfishanalyzer/util/PgnParser.java`
  - 正则表达式解析 PGN
- **象棋引擎**: `src/main/java/org/example/stockfishanalyzer/util/SimpleChessEngine.java`
  - SAN/UCI 转换（如已实现）

### 数据层
- **实体类**: `src/main/java/org/example/stockfishanalyzer/entity/`
  - `User.java`, `GamePgn.java`, `AnalysisResult.java`
- **Repository**: `src/main/java/org/example/stockfishanalyzer/repository/`
  - JPA 数据访问接口

### 控制器
- **PGN 控制器**: `src/main/java/org/example/stockfishanalyzer/controller/PgnController.java`
  - REST API 端点

### 配置
- **应用配置**: `src/main/resources/application.properties`
  - 数据库、Stockfish、异步线程池配置
- **异步配置**: `src/main/java/org/example/stockfishanalyzer/config/AsyncConfiguration.java`
  - `@EnableAsync` 配置
- **CORS 配置**: `src/main/java/org/example/stockfishanalyzer/config/WebConfig.java`
  - 跨域请求配置（允许前端访问 API）

### 枚举类
- **分析状态**: `src/main/java/org/example/stockfishanalyzer/enums/AnalysisStatus.java`
  - `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- **走法分类**: `src/main/java/org/example/stockfishanalyzer/enums/MoveClassification.java`
  - `BRILLIANT`, `BEST`, `GOOD`, `INACCURACY`, `MISTAKE`, `BLUNDER`

### API 端点
所有端点基础路径: `/api/pgn`

1. **上传 PGN**: `POST /api/pgn/upload?userId={userId}`
   - Request Body: PGN 文本内容 (Content-Type: text/plain)
   - Response: `PgnUploadResponse` (包含 gameId, 分析状态等)

2. **获取用户棋局列表**: `GET /api/pgn/games?userId={userId}`
   - Response: `List<GamePgn>`

3. **获取棋局详情**: `GET /api/pgn/game/{gameId}`
   - Response: `GamePgn`

4. **获取棋局所有分析结果**: `GET /api/pgn/analysis/{gameId}`
   - Response: `List<AnalysisResultDto>`

5. **获取特定步数分析**: `GET /api/pgn/analysis/{gameId}/{moveNumber}`
   - Response: `AnalysisResultDto`

## 代码架构与流程

### 分层架构
```
Controller Layer (PgnController)
    ↓
Service Layer (PgnService, GameAnalysisService)
    ↓
Utility/Engine Layer (StockfishService, MoveClassificationService, PgnParser)
    ↓
Repository Layer (JPA Repositories)
    ↓
Entity Layer (User, GamePgn, AnalysisResult)
```

### 典型分析流程
1. **PGN 上传**: `PgnController` 接收 PGN 文件
2. **解析 PGN**: `PgnParser` 提取标签和走法（SAN 格式）
3. **创建棋局记录**: 保存到 `tbl_game_pgn`，状态为 `PENDING`
4. **触发异步分析**: `GameAnalysisService.analyzeGameAsync()` 在后台线程执行
5. **逐步分析**:
   - 对每一步棋，调用 `StockfishService` 分析当前局面
   - 获取最佳走法和评分
   - 通过 `MoveClassificationService` 对实际走法分类
6. **保存结果**: 批量保存到 `tbl_analysis_result`
7. **更新状态**: 棋局状态更新为 `COMPLETED`

## 关键技术实现要点

### 1. Stockfish IPC 集成 (核心难点)
**实现位置**: `StockfishService.java`

- **进程管理**:
  - `@PostConstruct` 启动 Stockfish 进程，`@PreDestroy` 优雅关闭
  - 使用 `ProcessBuilder` 启动外部进程
  - 进程生命周期与 Spring 容器绑定

- **风险**: 进程 I/O 缓冲区阻塞问题
  - **问题**: 如果不及时读取 Stockfish 输出，缓冲区会满导致进程阻塞
  - **解决方案**: 使用 `ExecutorService.newSingleThreadExecutor()` 异步读取输出

- **UCI 协议交互**:
  - 初始化: `uci` → 等待 `uciok` → `isready` → 等待 `readyok`
  - 分析: `position fen <fen>` 或 `position startpos moves <moves>` → `go movetime 1000` → 解析 `info`/`bestmove`
  - 重要：每次分析前需要发送 `isready` 确保引擎就绪

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
    - `spring.task.execution.thread-name-prefix=async-` (线程名前缀)

- **重要**: `@Async` 方法必须通过 Spring 代理调用才能生效
  - ✅ 正确：从另一个 Bean 调用异步方法
  - ❌ 错误：在同一个类中调用 `this.analyzeGameAsync()` 不会异步执行

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

- **输入**:
  - `actualMoveScore`: 用户实际走法的评分（厘兵）
  - `bestMoveScore`: AI 最佳走法评分（厘兵）
  - `secondBestScore`: 次佳走法评分（可选，用于妙手判定）
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

- **解析流程**:
  1. 使用正则提取标签: `\[(Event|Site|White|...)\s+"([^"]*)"\]`
  2. 移除走法序号、注释 `{comment}`、变着 `(variation)`、注解符号 `!?`
  3. 使用正则提取走法（SAN 格式）

- **走法格式转换**:
  - **SAN (Standard Algebraic Notation)**: `Nf3`, `O-O`, `exd5+` (人类可读)
  - **UCI (Universal Chess Interface)**: `e2e4`, `e1g1`, `e4d5` (引擎使用)
  - `SimpleChessEngine` 负责 SAN → UCI 转换，需要维护完整棋盘状态

- **已知限制**:
  - `PgnParser.sanToUci()` 未完全实现（需要完整的走法合法性验证）
  - 复杂的 PGN 特性（变着、注释、NAG）未完全支持

## 开发注意事项

### 环境配置

#### 必需的环境依赖
1. **Java 17**: 项目使用 Java 17，确保 `java -version` 显示 17+
2. **MySQL 8.x**: 本地或远程 MySQL 服务器
3. **Stockfish 引擎**: UCI 兼容的国际象棋引擎

#### Stockfish 安装和配置
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

#### MySQL 数据库配置
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

#### 首次启动检查清单
- [ ] MySQL 服务运行中（`brew services list` 或 `systemctl status mysql`）
- [ ] 数据库 `Chess` 已创建
- [ ] `application.properties` 中的数据库密码正确
- [ ] Stockfish 已安装且路径配置正确
- [ ] 端口 9090 未被占用

### 性能优化
- **分析时间控制**: 配置 `stockfish.analysis.movetime=1000`（毫秒），确保单步分析时间可控
- **批量保存**: `GameAnalysisService` 使用 `saveAll()` 批量保存分析结果
- **分析结果缓存**: 通过 `existsByGameId()` 检查避免重复分析
- **数据库索引**: 确保以下索引存在：
  - `tbl_game_pgn.user_id`
  - `tbl_analysis_result(game_id, move_number)` 复合索引
- **异步线程池**: 根据服务器性能调整 `application.properties` 中的线程池配置

### 测试注意事项
- **单元测试**: 项目使用 H2 内存数据库进行测试（见 `pom.xml`）
- **Stockfish 依赖**: `StockfishService.init()` 会检查引擎文件是否存在，测试环境可跳过
- **集成测试**: 需要先启动 MySQL 并创建测试数据库

### 调试和日志
- **日志级别**: 使用 Lombok 的 `@Slf4j` 注解，支持 SLF4J 日志
- **重要日志位置**:
  - `GameAnalysisService`: 分析流程的开始、进度、完成/失败
  - `StockfishService`: UCI 命令交互、引擎输出
  - `PgnService`: PGN 上传和解析
- **调试技巧**:
  ```properties
  # 在 application.properties 中开启 SQL 日志
  spring.jpa.show-sql=true
  spring.jpa.properties.hibernate.format.sql=true

  # 调整日志级别
  logging.level.org.example.stockfishanalyzer=DEBUG
  logging.level.org.hibernate.SQL=DEBUG
  ```
- **监控端点** (Spring Actuator):
  - `http://localhost:9090/actuator/health` - 健康检查
  - `http://localhost:9090/actuator/info` - 应用信息

### 常见问题排查

#### 1. Stockfish 初始化失败
**症状**: 日志显示 "Stockfish 引擎初始化失败"
```bash
# 检查路径
which stockfish
# 更新 application.properties 中的 stockfish.engine.path
```

#### 2. 分析状态一直是 PENDING
**可能原因**: Stockfish 未正确启动 / 异步线程池配置问题
**排查**: 检查后端日志，查找异常堆栈信息

#### 3. 数据库连接失败
**症状**: 启动时报 "Communications link failure"
```bash
# 检查 MySQL 服务状态
brew services list  # macOS
# 测试数据库连接
mysql -u root -p -h localhost Chess
```

#### 4. 启动脚本检查失败
**症状**: start.sh 提示 Stockfish 未找到
```bash
# 检查 Stockfish 实际安装路径
which stockfish
# 更新 application.properties 中的路径
# 或安装：brew install stockfish
```

### 前端开发要点
- **主界面**: `src/main/resources/static/index.html` - 完整的单页面应用
- **技术栈**: Vue 3 (CDN) + chessboard.js + chess.js + ECharts
- **访问方式**: 启动后端后直接访问 `http://localhost:9090/`
- **核心功能**:
  - PGN 上传（拖放/选择/粘贴）
  - 交互式棋盘可视化
  - 走法分类图标展示（✨妙手 / 😱漏着 / 😊好棋等）
  - 评估曲线图表（ECharts）
- **API 调用**: 所有请求通过 `/api/pgn` 端点，已配置 CORS 支持
- **详细文档**: 见 `README_FRONTEND.md` 和 `FRONTEND_STATUS.md`

## 项目当前状态

✅ **MVP 已完成** - 项目核心功能已实现并可使用

已完成模块：
- ✅ Spring Boot 基础框架搭建（Spring Boot 3.5.6）
- ✅ Maven 依赖配置（Spring Data JPA, MySQL, Lombok, H2, Actuator）
- ✅ 数据库 JPA Entity 定义（User, GamePgn, AnalysisResult）
- ✅ Repository 层（UserRepository, GamePgnRepository, AnalysisResultRepository）
- ✅ Stockfish IPC 服务封装（StockfishService with UCI protocol）
- ✅ PGN 解析工具（PgnParser）
- ✅ 启发式走法分类算法（MoveClassificationService）
- ✅ 异步分析服务（GameAnalysisService with @Async）
- ✅ 异步配置（AsyncConfiguration）
- ✅ 控制器和服务层（PgnController, PgnService）
- ✅ 简化的棋盘引擎（SimpleChessEngine）
- ✅ CORS 跨域配置（WebConfig）
- ✅ 前端 Vue 3 应用（完整界面，包括棋盘、分析展示、评估曲线）
- ✅ 快速启动脚本（start.sh）

待优化模块：
1. ⚠️ SAN 到 UCI 走法格式转换（PgnParser.sanToUci 未完全实现，当前使用 SimpleChessEngine）
2. ⚠️ 完善的错误处理和异常管理
3. ⚠️ 单元测试和集成测试覆盖率
4. ⚠️ 生产环境优化（如连接池、缓存策略等）

## 参考文档

详细的项目需求、WBS 任务分解、风险预案见 `项目详细任务书.md`。
