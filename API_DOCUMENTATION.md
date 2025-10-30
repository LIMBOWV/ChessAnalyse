# API 接口文档

> 项目: Stockfish Chess Analyzer  
> 版本: 1.0.0  
> 更新时间: 2025-10-29  
> 基础URL: `http://localhost:9090`

---

## 目录
- [认证说明](#认证说明)
- [PGN管理](#1-pgn管理)
- [游戏分析](#2-游戏分析)
- [趋势分析](#3-趋势分析)
- [开局分析](#4-开局分析)
- [失误分析](#5-失误分析)
- [任务管理](#6-任务管理)
- [健康检查](#7-健康检查)
- [错误码说明](#错误码说明)

---

## 认证说明

**当前版本**: 无需认证  
**未来版本**: 将支持 JWT Token 认证

所有API请求都需要提供用户ID参数 (`userId`)。

---

## 1. PGN管理

### 1.1 上传PGN棋谱

**接口**: `POST /api/pgn/upload`

**描述**: 上传PGN格式的国际象棋棋谱并进行自动分析

**请求参数**:
- **Query参数**:
  - `userId` (可选, 默认=1): 用户ID

**请求体**:
```
Content-Type: text/plain

[Event "Rated Blitz game"]
[Site "https://lichess.org/abc123"]
[Date "2025.10.29"]
[White "Player1"]
[Black "Player2"]
[Result "1-0"]

1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 ...
```

**响应示例**:
```json
{
  "success": true,
  "message": "PGN 上传成功",
  "gameId": 123,
  "taskId": 456,
  "gameInfo": {
    "white": "Player1",
    "black": "Player2",
    "result": "1-0",
    "event": "Rated Blitz game"
  }
}
```

**cURL示例**:
```bash
curl -X POST "http://localhost:9090/api/pgn/upload?userId=1" \
  -H "Content-Type: text/plain" \
  --data-binary "@game.pgn"
```

---

### 1.2 获取用户游戏列表

**接口**: `GET /api/pgn/games`

**描述**: 获取指定用户的所有棋局

**请求参数**:
- `userId` (可选, 默认=1): 用户ID

**响应示例**:
```json
[
  {
    "id": 123,
    "userId": 1,
    "pgnContent": "...",
    "uploadedAt": "2025-10-29T10:30:00",
    "analysisStatus": "COMPLETED",
    "white": "Player1",
    "black": "Player2",
    "result": "1-0",
    "event": "Rated Blitz game",
    "site": "https://lichess.org/abc123",
    "gameDate": "2025.10.29"
  }
]
```

**cURL示例**:
```bash
curl "http://localhost:9090/api/pgn/games?userId=1"
```

---

### 1.3 获取单个游戏详情

**接口**: `GET /api/pgn/game/{gameId}`

**描述**: 获取指定棋局的详细信息

**路径参数**:
- `gameId`: 棋局ID

**响应示例**:
```json
{
  "id": 123,
  "userId": 1,
  "pgnContent": "...",
  "uploadedAt": "2025-10-29T10:30:00",
  "analysisStatus": "COMPLETED",
  "white": "Player1",
  "black": "Player2",
  "result": "1-0"
}
```

**cURL示例**:
```bash
curl "http://localhost:9090/api/pgn/game/123"
```

---

## 2. 游戏分析

### 2.1 获取游戏分析结果

**接口**: `GET /api/pgn/analysis/{gameId}`

**描述**: 获取指定棋局的完整Stockfish分析结果

**路径参数**:
- `gameId`: 棋局ID

**响应示例**:
```json
[
  {
    "id": 1,
    "gameId": 123,
    "moveNumber": 1,
    "move": "e4",
    "evaluation": 0.3,
    "bestMove": "e4",
    "classification": "EXCELLENT",
    "analysisDepth": 18,
    "pvLine": "e4 e5 Nf3 Nc6"
  },
  {
    "id": 2,
    "gameId": 123,
    "moveNumber": 2,
    "move": "e5",
    "evaluation": -0.3,
    "bestMove": "e5",
    "classification": "GOOD",
    "analysisDepth": 18,
    "pvLine": "e5 Nf3 Nc6"
  }
]
```

**分类说明**:
- `BRILLIANT`: 妙手 (评估提升 > 1.0)
- `EXCELLENT`: 最佳着法 (与引擎推荐一致)
- `GOOD`: 优秀着法 (评估损失 < 0.3)
- `INACCURACY`: 不精确 (评估损失 0.3-1.0)
- `MISTAKE`: 失误 (评估损失 1.0-3.0)
- `BLUNDER`: 漏着 (评估损失 > 3.0)

**cURL示例**:
```bash
curl "http://localhost:9090/api/pgn/analysis/123"
```

---

## 3. 趋势分析

### 3.1 获取用户表现趋势

**接口**: `GET /api/trends`

**描述**: 分析用户在指定时间段内的表现趋势

**请求参数**:
- `userId` (必需): 用户ID
- `startDate` (必需): 开始日期 (格式: YYYY-MM-DD)
- `endDate` (必需): 结束日期 (格式: YYYY-MM-DD)

**响应示例**:
```json
{
  "timeline": [
    {
      "date": "2025-10-10",
      "avgAccuracy": 98.41,
      "totalGames": 13,
      "winRate": 100.0,
      "blunders": 5,
      "mistakes": 5,
      "inaccuracies": 0,
      "brilliantMoves": 4
    },
    {
      "date": "2025-10-23",
      "avgAccuracy": 100.0,
      "totalGames": 3,
      "winRate": 100.0,
      "blunders": 0,
      "mistakes": 0,
      "inaccuracies": 0,
      "brilliantMoves": 2
    }
  ]
}
```

**字段说明**:
- `avgAccuracy`: 平均准确率 (%)
- `totalGames`: 总对局数
- `winRate`: 胜率 (%)
- `blunders`: 漏着次数
- `mistakes`: 失误次数
- `inaccuracies`: 不精确次数
- `brilliantMoves`: 妙手次数

**cURL示例**:
```bash
curl "http://localhost:9090/api/trends?userId=1&startDate=2025-10-01&endDate=2025-10-29"
```

---

## 4. 开局分析

### 4.1 获取用户开局统计

**接口**: `GET /api/openings/stats/{userId}`

**描述**: 分析用户各种开局的胜率和使用频率

**路径参数**:
- `userId`: 用户ID

**响应示例**:
```json
{
  "openings": [
    {
      "openingName": "Italian Game",
      "eco": "C50",
      "totalGames": 25,
      "wins": 18,
      "draws": 5,
      "losses": 2,
      "winRate": 72.0,
      "avgAccuracy": 95.3,
      "firstMoves": "1. e4 e5 2. Nf3 Nc6 3. Bc4"
    },
    {
      "openingName": "Sicilian Defense",
      "eco": "B20",
      "totalGames": 30,
      "wins": 20,
      "draws": 6,
      "losses": 4,
      "winRate": 66.7,
      "avgAccuracy": 93.8,
      "firstMoves": "1. e4 c5"
    }
  ]
}
```

**字段说明**:
- `eco`: Encyclopedia of Chess Openings 编码
- `totalGames`: 使用该开局的总局数
- `wins/draws/losses`: 胜/和/负场次
- `winRate`: 胜率 (%)
- `avgAccuracy`: 该开局的平均准确率

**cURL示例**:
```bash
curl "http://localhost:9090/api/openings/stats/1"
```

---

## 5. 失误分析

### 5.1 获取用户失误统计

**接口**: `GET /api/analysis/mistakes/{userId}`

**描述**: 获取用户的失误、漏着和不精确统计

**路径参数**:
- `userId`: 用户ID

**请求参数**:
- `type` (可选): 筛选类型
  - `BLUNDER`: 仅漏着
  - `MISTAKE`: 仅失误
  - `INACCURACY`: 仅不精确

**响应示例**:
```json
{
  "mistakes": [
    {
      "id": 45,
      "gameId": 123,
      "moveNumber": 15,
      "move": "Qh5",
      "bestMove": "Nf3",
      "evaluation": -2.5,
      "bestEvaluation": 0.3,
      "evalLoss": 2.8,
      "classification": "BLUNDER",
      "position": "rnbqkb1r/pppp1ppp/5n2/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R",
      "explanation": "失去了子力优势"
    }
  ],
  "summary": {
    "totalBlunders": 12,
    "totalMistakes": 25,
    "totalInaccuracies": 40,
    "averageEvalLoss": 1.2
  }
}
```

**cURL示例**:
```bash
# 获取所有失误
curl "http://localhost:9090/api/analysis/mistakes/1"

# 仅获取漏着
curl "http://localhost:9090/api/analysis/mistakes/1?type=BLUNDER"
```

---

## 6. 任务管理

### 6.1 获取用户任务列表

**接口**: `GET /api/tasks/user/{userId}`

**描述**: 获取用户的所有分析任务及其状态

**路径参数**:
- `userId`: 用户ID

**响应示例**:
```json
[
  {
    "id": 456,
    "userId": 1,
    "gameId": 123,
    "status": "COMPLETED",
    "createdAt": "2025-10-29T10:30:00",
    "completedAt": "2025-10-29T10:35:00",
    "errorMessage": null,
    "progress": 100
  },
  {
    "id": 457,
    "userId": 1,
    "gameId": 124,
    "status": "RUNNING",
    "createdAt": "2025-10-29T11:00:00",
    "completedAt": null,
    "errorMessage": null,
    "progress": 45
  }
]
```

**状态说明**:
- `PENDING`: 等待中
- `RUNNING`: 分析中
- `COMPLETED`: 已完成
- `FAILED`: 失败

**cURL示例**:
```bash
curl "http://localhost:9090/api/tasks/user/1"
```

---

### 6.2 获取任务详情

**接口**: `GET /api/tasks/{taskId}`

**描述**: 获取指定任务的详细信息

**路径参数**:
- `taskId`: 任务ID

**响应示例**:
```json
{
  "id": 456,
  "userId": 1,
  "gameId": 123,
  "status": "COMPLETED",
  "createdAt": "2025-10-29T10:30:00",
  "completedAt": "2025-10-29T10:35:00",
  "errorMessage": null,
  "progress": 100,
  "totalMoves": 42,
  "analyzedMoves": 42
}
```

**cURL示例**:
```bash
curl "http://localhost:9090/api/tasks/456"
```

---

## 7. 健康检查

### 7.1 应用健康状态

**接口**: `GET /actuator/health`

**描述**: 检查应用程序健康状态

**响应示例**:
```json
{
  "status": "UP"
}
```

**cURL示例**:
```bash
curl "http://localhost:9090/actuator/health"
```

---

## 错误码说明

### HTTP状态码

| 状态码 | 说明 | 示例场景 |
|-------|------|---------|
| 200 | 成功 | 请求成功处理 |
| 400 | 请求参数错误 | PGN格式无效 |
| 404 | 资源不存在 | 游戏ID不存在 |
| 500 | 服务器内部错误 | Stockfish引擎异常 |

### 错误响应格式

```json
{
  "error": "Invalid PGN format",
  "message": "PGN parsing failed at line 5",
  "timestamp": "2025-10-29T10:30:00"
}
```

---

## 性能指标

### 响应时间 (测试环境)

| API 端点 | 平均响应时间 | 备注 |
|---------|------------|------|
| `/api/pgn/upload` | ~500ms | 不含分析时间 |
| `/api/trends` | 223ms | 已优化 |
| `/api/openings/stats/{userId}` | 90ms | 含缓存 |
| `/api/analysis/mistakes/{userId}` | 158ms | 已优化 |
| `/api/pgn/games` | 13ms | 列表查询 |

### 并发性能

- **最大并发**: 10个连接 (HikariCP配置)
- **5并发测试**: 平均146ms响应
- **推荐QPS**: < 50 (单实例)

---

## 使用示例

### 完整工作流程

```bash
# 1. 上传PGN
GAME_ID=$(curl -s -X POST "http://localhost:9090/api/pgn/upload?userId=1" \
  -H "Content-Type: text/plain" \
  --data-binary "@game.pgn" | jq -r '.gameId')

echo "Game ID: $GAME_ID"

# 2. 等待分析完成 (异步任务,约30秒)
sleep 30

# 3. 获取分析结果
curl "http://localhost:9090/api/pgn/analysis/$GAME_ID" | jq

# 4. 查看趋势分析
curl "http://localhost:9090/api/trends?userId=1&startDate=2025-10-01&endDate=2025-10-29" | jq

# 5. 查看开局统计
curl "http://localhost:9090/api/openings/stats/1" | jq

# 6. 查看失误分析
curl "http://localhost:9090/api/analysis/mistakes/1?type=BLUNDER" | jq
```

---

## 注意事项

1. **异步分析**: PGN上传后,分析是异步进行的,需要轮询任务状态
2. **数据库索引**: 已优化查询性能,建议使用日期范围查询趋势
3. **Stockfish配置**: 
   - 分析深度: 18层
   - 每步时间: 1000ms
4. **批量上传**: 建议单次上传,避免并发上传导致队列阻塞

---

**文档版本**: v1.0.0  
**最后更新**: 2025-10-29  
**维护者**: David / GitHub Copilot
