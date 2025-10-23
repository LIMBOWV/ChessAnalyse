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

### 2. 数据库扩展方案（8表）

#### 表4: tbl_opening_book - 开局库表
**用途**: 存储常见开局定式，支持开局识别与分析
```text
- id (PK)
- eco_code (VARCHAR, 如 "C50")
- opening_name (VARCHAR, 如 "Italian Game")
- variation_name (VARCHAR, 如 "Giuoco Piano")
- moves_uci (TEXT, UCI格式走法序列)
- moves_san (TEXT, SAN格式走法序列)
- fen_position (VARCHAR, 开局后局面FEN)
- popularity (INT, 使用频率)
```

#### 表5: tbl_user_statistics - 用户统计表
**用途**: 聚合用户的对局数据，支持数据可视化
```text
- id (PK)
- user_id (FK -> tbl_user)
- total_games (INT, 总对局数)
- win_count (INT)
- draw_count (INT)
- loss_count (INT)
- avg_accuracy (DECIMAL, 平均准确度%)
- total_blunders (INT, 总漏着数)
- total_mistakes (INT, 总失误数)
- total_brilliants (INT, 总妙手数)
- favorite_opening_id (FK -> tbl_opening_book, 最擅长开局)
- last_updated (TIMESTAMP)
```

#### 表6: tbl_game_tag - 棋局标签表（多对多关系）
**用途**: 用户自定义标签分类管理棋局
```text
- id (PK)
- tag_name (VARCHAR, UNIQUE, 如 "重要比赛")
- user_id (FK -> tbl_user)
- color (VARCHAR, 标签颜色)
- created_at (TIMESTAMP)
```

#### 表7: tbl_game_tag_relation - 棋局标签关联表
**用途**: 多对多关系中间表
```text
- id (PK)
- game_id (FK -> tbl_game_pgn)
- tag_id (FK -> tbl_game_tag)
```

#### 表8: tbl_position_bookmark - 局面书签表
**用途**: 收藏对局中的关键局面
```text
- id (PK)
- game_id (FK -> tbl_game_pgn)
- user_id (FK -> tbl_user)
- move_number (INT, 收藏的步数)
- fen_position (VARCHAR, 局面FEN)
- note (TEXT, 用户备注)
- created_at (TIMESTAMP)
```

#### 备选表（如时间充裕可选）:
- **tbl_analysis_task_log** - 分析任务日志表（系统监控、性能分析）
- **tbl_tactical_puzzle** - 战术题库表（从失误局面生成练习题）

---

## 🖥️ 前端页面扩展方案（15页面）

### 已完成的页面（3个）✅
1. **棋局列表页** (`/games`) - 展示所有对局
2. **棋局详情/复盘页** (`/game/:id`) - 核心棋盘交互
3. **PGN上传页** (`/upload`) - 上传棋谱

### 核心功能增强 🌟
**"第二条时间线"功能** - 在复盘页面中实现
- **功能说明**: 当用户查看某步的AI最佳建议时，可以点击"尝试AI建议"按钮
- **实现效果**: 
  - 从当前步骤开始，采用AI的最佳走法继续分析
  - 显示双棋盘对比（左边：实际走法，右边：AI建议走法）
  - 实时对比两条时间线的评分曲线
  - 支持在AI时间线上继续分析后续步骤
- **技术亮点**:
  - 动态棋局分支管理
  - 实时调用Stockfish分析新局面
  - ECharts双曲线对比可视化
  - 前端状态管理（保存两条时间线的数据）

### 待开发的页面（12个）🚧

#### 模块1: 用户系统（3页面）
**优先级: 高** - 基础功能，必须实现

4. **用户注册页** (`/register`)
   - 表单验证（用户名、邮箱、密码强度）
   - 注册成功自动跳转登录

5. **用户登录页** (`/login`)
   - JWT Token认证
   - 记住登录状态（LocalStorage）

6. **个人中心页** (`/profile`)
   - 展示用户信息
   - 修改密���、头像上传（可选）
   - 账号统计概览

#### 模块2: 数据分析（5页面）
**优先级: 高** - 核心亮点，体现技术深度

7. **用户统计Dashboard** (`/dashboard`)
   - ECharts 数据可视化
   - 胜率饼图、准确度趋势图
   - 走法分类统计（妙手/失误/漏着柱状图）

    - **支持"实际走法 vs AI建议"对比**（第二条时间线）
      - 从任意步骤分支出AI建议的走法序列
      - 实时分析AI时间线的后续局面
      - 显示两条时间线的关键差异点
8. **开局分析页** (`/openings`)
   - 展示所有开局列表（从 tbl_opening_book）
   - 用户在各开局体系下的胜率表格
   - 推荐擅长/薄弱的开局

9. **失误统计页** (`/mistakes`)
   - 汇总所有对局的 BLUNDER 和 MISTAKE
   - 按失误类型、开局阶段分类
   - 点击可跳转到具体失误局面

10. **对比分析页** (`/compare`)
    - 对比两个棋局的分析结果
    - 双棋盘并排展示
    - 评分曲线对比（ECharts 折线图）

11. **历史趋势页** (`/trends`)
    - 时间维度分析（月度、季度）
    - 准确度、胜率随时间变化
    - 进步曲线可视化

#### 模块3: 学习功能（2页面）
**优先级: 中** - 增加实用性

12. **局面收藏页** (`/bookmarks`)
    - 展示所有收藏的局面（tbl_position_bookmark）
    - 显示棋盘缩略图 + 备注
    - 点击跳转到原对局的该步

13. **标签管理页** (`/tags`)
    - 创建、编辑、删除标签（tbl_game_tag）
    - 按标签筛选棋局
    - 标签云展示（使用频率）

#### 模块4: 系统管理（2页面）
**优先级: 中** - 体现系统完整性

14. **系统设置页** (`/settings`)
    - Stockfish 引擎参数配置（深度、时间）
    - 界面主题切换（亮色/暗色）
    - 棋盘样式选择

15. **分析任务管理页** (`/tasks`)
    - 查看所有分析任务状态（PENDING/PROCESSING/COMPLETED/FAILED）
    - 失败任务可重试
    - 分析队列可视化

#### 备选页面（如需要凑数）:
16. **关于页面** (`/about`) - 项目介绍、技术栈说明
17. **帮助中心** (`/help`) - 使用说明、FAQ
18. **404错误页** (`/404`) - 友好的错误提示

---

## 🎯 开发优先级与时间分配

### 第一阶段（2天）- 必做核心
- [ ] 实现用户认证系统（注册/登录/个人中心）
- [ ] 新增 tbl_user_statistics 表
- [ ] 开发用户统计 Dashboard（ECharts集成）
- [ ] 完善现有3个页面（优化UI、添加功能）

### 第二阶段（2天）- 数据扩展
- [ ] 实现开局库表 + 开局分析页
- [ ] 实现标签系统（表 + 页面）
- [ ] 实现局面收藏功能
- [ ] 开发失误统计页

### 第三阶段（2天）- 高级功能
- [ ] 对比分析页（双棋盘）
- [ ] 历史趋势分析页
- [ ] 系统设置页
- [ ] 分析任务管理页

### 第四阶段（1天）- 收尾优化
- [ ] UI/UX 美化
- [ ] 响应式布局适配
- [ ] 测试与Bug修复
- [ ] 文档撰写

---

## 📐 技术实现要点

### 前端技术栈
- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router 4
- **状态管理**: Pinia（推荐）或 Vuex
- **UI组件库**: Element Plus 或 Ant Design Vue
- **图表库**: ECharts 5.x
- **棋盘库**: chessboard.js + chess.js（已集成）
- **HTTP客户端**: Axios

### 后端新增API
```
用户认证:
- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/profile

统计分析:
- GET /api/statistics/{userId}
- GET /api/statistics/openings/{userId}
- GET /api/statistics/mistakes/{userId}


第二条时间线（AI分支分析）:
- POST /api/analysis/alternative (创建AI建议分支)
  - 请求参数: gameId, fromMoveNumber, alternativeMoves[]
  - 返回: 新分支的分析结果
- GET /api/analysis/compare/{gameId}/{moveNumber} (对比实际走法与AI建议)
  - 返回两条时间线的评分差异
- POST /api/analysis/continue-alternative (在AI分支上继续分析)
  - 支持用户在AI时间线上继续探索
开局相关:
- GET /api/openings (获取所有开局)
- GET /api/openings/detect?gameId={id} (识别棋局开局)

标签管理:
- POST /api/tags (创建标签)
- GET /api/tags/{userId}
- POST /api/games/{gameId}/tags (添加标签)
- DELETE /api/games/{gameId}/tags/{tagId}

局面收藏:
- POST /api/bookmarks (收藏局面)
- GET /api/bookmarks/{userId}
- DELETE /api/bookmarks/{id}

任务管理:
- GET /api/tasks (获取分析任务列表)
- POST /api/tasks/{taskId}/retry (重试失败任务)
```

### 数据库变更注意事项
- 所有新增表使用 `utf8mb4` 字符集
- 外键约束使用 `ON DELETE CASCADE`（按需）
- 为高频查询字段添加索引
- 使用 Flyway 或 Liquibase 管理数据库迁移（可选，推荐）

---

## 💡 毕设答辩准备建议

### 技术亮点展示
1. **核心创新**: 自研启发式走法分类算法
2. **架构设计**: 异步分析 + 结果缓存
3. **技术难点**: Java-C++ IPC 通信、UCI协议解析
4. **数据设计**: 8表设计体现多种关系（一对多、多对多、聚合统计）
5. **全栈能力**: 15页面涵盖前端各种技术（表单、图表、复杂交互）

### PPT素材准备
- 系统架构图（前后端分离、异步处理流程）
- 数据库ER图（8表关系）
- 关键代码截图（走法分类算法、Stockfish IPC）
- 页面截图（15个页面的展示）
- 性能对比（后端分析 vs 浏览器分析）
- 技术对比表（本项目 vs Lichess vs Chess.com）

### 演示场景
1. 用户注册 → 登录 → 上传PGN
2. 查看分析进度（任务管理页）
3. 复盘展示（棋盘 + 走法分类标注）
4. 数据分析（Dashboard、开局分析、失误统计）
5. 个性化功能（标签、收藏局面）

---

## 🚨 注意事项

### 开发时避免的坑
1. **前端路由**: 确保所有路由都在 Vue Router 中注册
2. **跨域问题**: 检查 WebConfig 的 CORS 配置
3. **认证鉴权**: API 需要检查用户登录状态（JWT Token）
4. **数据库迁移**: 新增表时确保与 application.properties 中的 `ddl-auto` 配置一致
5. **异步分析**: 新增统计计算时考虑使用 @Async 避免阻塞

### 时间管理
- 每个页面平均开发时间: 2-3小时（简单表单页） ~ 4-6小时（复杂交互页）
- 预留20%时间用于Bug修复和优化
- 优先完成核心功能，备选功能根据时间决定

### 代码质量
- 保持代码风格一致（使用Lombok简化代码）
- 添加必要注释（特别是复杂算法部分）
- 前端组件化开发（提高复用性）
- 后端使用 DTO 进行数据传输（避免直接返回 Entity）

---

## 📚 相关文档
- 详细需求: `项目详细任务书.md`
- 构建报告: `BUILD_REPORT.md`
- API测试报告: `API_TEST_REPORT.md`
- 前端接口说明: `README_FRONTEND.md`

---

**最后更新**: 2025-10-23  
**当前进度**: 核心功能完成，正在扩展至毕设要求（8表15页面）

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

<!-- 以下为自动追加的低优先级扩展内容，优先级已标注为最低 -->

### 低优先级扩展：第二条时间线（AI 分支）与 AI 对战（优先级：最低）

- 优先级：最低（仅在核心功能、8 表与 15 页面、答辩材料完成后再实现）
- 目的：支持从任一步创建 AI 建议的“第二条时间线”，并可进行 AI vs AI 的对弈模拟，作为后续的增强型功能。

主要功能概览：
- 从棋局任一步创建 AI 分支，返回分支 ID 与步序评估。
- 在复盘页面并排显示两条时间线（实际 vs AI），支持在 AI 时间线上继续分析。
- 支持 AI vs AI（指定两侧引擎参数，自主对弈若干回合并保存评估曲线与对局 PGN）。

建议的数据表（示例）：
- `tbl_analysis_branch`：分支元信息（id, game_id, parent_move_number, branch_name, created_by, status, created_at）
- `tbl_branch_move`：分支内走法（id, branch_id, move_number, san, uci, fen, engine_score_cp, bestmove_uci, info_json）

示例后端 API（附注：设计为异步/可排队执行）
- POST /api/analysis/alternative  - 创建 AI 分支（body: gameId, fromMoveNumber, options） -> 返回 branchId 与初始 moves
- POST /api/analysis/continue-alternative - 在分支上继续分析（body: branchId, movesToAdd） -> 异步 taskId
- GET  /api/analysis/branch/{branchId} - 获取分支详情
- POST /api/ai/duel - AI vs AI 对战（body: engineAOptions, engineBOptions, maxMoves/time） -> 返回对战分支与评估曲线

实现要点与风险控制（简要）：
- Stockfish 管理：建议采用引擎池或独立引擎进程避免并发冲突，每个任务分配独占引擎或使用会话锁。
- 评估一致性：分支间使用相同分析参数（depth/movetime）以便公平对比。
- 资源控制：限制并发引擎数，设置 movetime 上限，必要时在配置中提供并发阈值。
- 缓存策略：对相同 FEN + 参数的分析结果做缓存以减少重复计算。
- 异步任务：返回 taskId，支持任务查询/重试/失败标记。

粗略时间估算（仅供参考）：
- 最小可交付（创建分支、双棋盘基础、基本 API）：2~3 天
- 完整功能（AI vs AI、UI 美化、缓存、测试、部署优化）：5~8 天

备注：该段落已被标记为“最低优先级”，适合作为 Backlog 中的候选项，只有在核心需求与毕设答辩材料准备完成后再推进。
