#!/bin/bash

echo "=========================================="
echo "Stockfish Analyzer 核心 API 测试"
echo "=========================================="
echo ""

BASE_URL="http://localhost:9090"

# 测试 1: 获取用户棋局列表
echo "测试 1: GET /api/pgn/games?userId=1"
echo "----------------------------------------"
curl -s "$BASE_URL/api/pgn/games?userId=1" > /tmp/games.json
if [ -s /tmp/games.json ]; then
    echo "✅ 成功获取棋局列表"
    echo "棋局数量: $(grep -o '"id":' /tmp/games.json | wc -l | tr -d ' ')"
    echo ""
else
    echo "❌ 失败"
    echo ""
fi

# 测试 2: 获取特定棋局详情 (ID=33)
echo "测试 2: GET /api/pgn/game/33"
echo "----------------------------------------"
curl -s "$BASE_URL/api/pgn/game/33" > /tmp/game.json
if [ -s /tmp/game.json ]; then
    echo "✅ 成功获取棋局详情"
    grep -o '"whitePlayer":"[^"]*"' /tmp/game.json | head -1
    grep -o '"blackPlayer":"[^"]*"' /tmp/game.json | head -1
    echo ""
else
    echo "❌ 失败"
    echo ""
fi

# 测试 3: 获取棋局分析结果 (ID=33)
echo "测试 3: GET /api/pgn/analysis/33"
echo "----------------------------------------"
curl -s "$BASE_URL/api/pgn/analysis/33" > /tmp/analysis.json
if [ -s /tmp/analysis.json ]; then
    echo "✅ 成功获取分析结果"
    echo "分析步数: $(grep -o '"moveNumber":' /tmp/analysis.json | wc -l | tr -d ' ')"
    echo ""
else
    echo "❌ 失败"
    echo ""
fi

# 测试 4: 获取特定步数分析 (ID=33, 步数=1)
echo "测试 4: GET /api/pgn/analysis/33/1"
echo "----------------------------------------"
curl -s "$BASE_URL/api/pgn/analysis/33/1" > /tmp/move.json
if [ -s /tmp/move.json ]; then
    echo "✅ 成功获取步数分析"
    grep -o '"moveSan":"[^"]*"' /tmp/move.json | head -1
    grep -o '"moveClassification":"[^"]*"' /tmp/move.json | head -1
    echo ""
else
    echo "❌ 失败"
    echo ""
fi

# 测试 5: 上传PGN
echo "测试 5: POST /api/pgn/upload"
echo "----------------------------------------"
cat > /tmp/test.pgn << 'PGNCONTENT'
[Event "API测试"]
[Site "测试"]
[Date "2025.10.23"]
[White "测试白方"]
[Black "测试黑方"]
[Result "1-0"]

1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O 1-0
PGNCONTENT

curl -s -X POST "$BASE_URL/api/pgn/upload?userId=1" \
  -H "Content-Type: text/plain" \
  --data-binary @/tmp/test.pgn > /tmp/upload.json

if [ -s /tmp/upload.json ]; then
    echo "✅ 成功上传PGN"
    grep -o '"gameId":[0-9]*' /tmp/upload.json
    grep -o '"totalMoves":[0-9]*' /tmp/upload.json
    echo ""
else
    echo "❌ 失败"
    echo ""
fi

echo "=========================================="
echo "测试完成"
echo "=========================================="

