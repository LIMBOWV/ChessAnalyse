# 前端接口使用说明

## 🎨 前端界面

项目已经配置了完整的前端界面，位于：
- **主界面**: `src/main/resources/static/index.html`
- **测试页面**: `test-api.html`（项目根目录）

## 🚀 启动项目

1. **启动后端服务**
```bash
./mvnw spring-boot:run
```

2. **访问前端界面**
打开浏览器访问：
- 主界面：http://localhost:9090/
- API测试：http://localhost:9090/test-api.html

## 📡 API 接口列表

### 1. 上传 PGN 棋谱
```
POST /api/pgn/upload?userId=1
Content-Type: text/plain
Body: PGN文本内容
```

**响应示例**:
```json
{
  "gameId": 1,
  "message": "PGN 上传成功，异步分析已启动",
  "status": "PENDING"
}
```

### 2. 获取用户棋局列表
```
GET /api/pgn/games?userId=1
```

**响应示例**:
```json
[
  {
    "id": 1,
    "event": "Test Game",
    "whiteName": "Player1",
    "blackName": "Player2",
    "gameDate": "2024.01.01",
    "result": "1-0",
    "analysisStatus": "COMPLETED",
    "moves": "e4 e5 Nf3 Nc6..."
  }
]
```

### 3. 获取棋局详情
```
GET /api/pgn/game/{gameId}
```

### 4. 获取棋局分析结果
```
GET /api/pgn/analysis/{gameId}
```

**响应示例**:
```json
[
  {
    "moveNumber": 1,
    "moveSan": "e4",
    "score": 0.3,
    "bestMove": "e4",
    "moveClassification": "BOOK"
  },
  {
    "moveNumber": 2,
    "moveSan": "e5",
    "score": -0.2,
    "bestMove": "e5",
    "moveClassification": "GOOD"
  }
]
```

### 5. 获取特定步数分析
```
GET /api/pgn/analysis/{gameId}/{moveNumber}
```

## 🎯 前端功能特性

### ✨ 已实现的功能

1. **PGN上传**
   - 支持文件拖放上传
   - 支持文件选择上传
   - 支持直接粘贴 PGN 文本

2. **棋局管理**
   - 显示所有棋局列表
   - 显示分析状态（等待中/分析中/已完成/失败）
   - 点击选择棋局进行复盘

3. **交互式棋盘**
   - 使用 chessboard.js 显示棋盘
   - 支持前进/后退/跳转到开始/结束
   - 点击走法列表直接跳转到对应局面

4. **走法分析**
   - 显示每步棋的评分
   - 显示走法分类图标：
     - ✨ 妙手 (Brilliant)
     - 👍 很好 (Great)
     - 😊 好棋 (Good)
     - 📖 开局理论 (Book)
     - 🤔 不准确 (Inaccuracy)
     - 😞 失误 (Mistake)
     - 😱 大漏着 (Blunder)
     - 💔 错失胜利 (Missed Win)
   - 显示 AI 推荐的最佳走法

5. **评估曲线**
   - 使用 ECharts 绘制局势评估曲线
   - 实时显示整局的评分变化

## 🎨 界面设计

- **响应式设计**: 适配桌面端和移动端
- **现代化 UI**: 渐变色背景、圆角卡片、流畅动画
- **直观交互**: 拖放上传、点击选择、悬停高亮

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端框架（CDN版本） |
| chessboard.js | 棋盘可视化 |
| chess.js | 国际象棋逻辑引擎 |
| ECharts | 数据可视化（评估曲线） |
| jQuery | chessboard.js 依赖 |

## 📝 示例 PGN

```pgn
[Event "Test Game"]
[Site "Online"]
[Date "2024.01.01"]
[White "Player1"]
[Black "Player2"]
[Result "1-0"]

1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. b4 Bxb4 5. c3 Ba5 6. d4 exd4 7. O-O d3 1-0
```

## 🔧 配置说明

如果需要修改 API 地址，在 `index.html` 中找到：
```javascript
API_BASE: '/api/pgn'
```

修改为你的 API 地址，例如：
```javascript
API_BASE: 'http://your-server:9090/api/pgn'
```

## 🐛 故障排查

### 1. 无法访问前端页面
- 确认后端服务已启动
- 检查是否能访问 http://localhost:9090
- 查看浏览器控制台是否有错误

### 2. API 请求失败
- 检查后端日志
- 确认数据库已启动（MySQL）
- 确认 application.properties 配置正确

### 3. 棋盘不显示
- 检查浏览器控制台是否有 CDN 加载错误
- 确认网络连接正常

### 4. 分析状态一直是 PENDING
- 检查 Stockfish 引擎是否正确安装
- 查看后端日志的异步任务执行情况
- 确认 `stockfish.engine.path` 配置正确

## 📚 相关文档

- [Chessboard.js 文档](https://chessboardjs.com/)
- [Chess.js 文档](https://github.com/jhlywa/chess.js)
- [ECharts 文档](https://echarts.apache.org/zh/index.html)
- [Vue 3 文档](https://cn.vuejs.org/)

