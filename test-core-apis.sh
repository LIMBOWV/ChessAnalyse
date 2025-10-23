#!/bin/bash

# 测试核心API脚本
BASE_URL="http://localhost:9090"
USER_ID=1

echo "========================================"
echo "开始测试 Stockfish Analyzer 核心 API"
echo "========================================"
echo ""

# 测试1: 上传PGN文件
echo "测试1: 上传PGN文件"
echo "----------------------------------------"
PGN_CONTENT=$(cat lichess_pgn_2025.10.10_LIMBOWV_vs_Anonymous.VxZBqXAA.pgn)
UPLOAD_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/pgn/upload?userId=${USER_ID}" \
  -H "Content-Type: text/plain" \
  --data-raw "$PGN_CONTENT")

echo "响应: $UPLOAD_RESPONSE"
GAME_ID=$(echo $UPLOAD_RESPONSE | grep -o '"gameId":[0-9]*' | grep -o '[0-9]*')
echo "提取的 Game ID: $GAME_ID"
echo ""

# 等待分析完成
echo "等待5秒，让分析任务启动..."
sleep 5
echo ""

# 测试2: 获取用户的所有棋局
echo "测试2: 获取用户的所有棋局"
echo "----------------------------------------"
curl -s -X GET "${BASE_URL}/api/pgn/games?userId=${USER_ID}" | python3 -m json.tool
echo ""
echo ""

# 测试3: 获取棋局详情
if [ -n "$GAME_ID" ]; then
  echo "测试3: 获取棋局详情 (Game ID: $GAME_ID)"
  echo "----------------------------------------"
  curl -s -X GET "${BASE_URL}/api/pgn/game/${GAME_ID}" | python3 -m json.tool
  echo ""
  echo ""

  # 测试4: 获取棋局的所有分析结果
  echo "测试4: 获取棋局的所有分析结果 (Game ID: $GAME_ID)"
  echo "----------------------------------------"
  ANALYSIS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/pgn/analysis/${GAME_ID}")
  echo "$ANALYSIS_RESPONSE" | python3 -m json.tool
  echo ""
  echo ""

  # 测试5: 获取特定步数的分析结果 (第1步)
  echo "测试5: 获取第1步的分析结果"
  echo "----------------------------------------"
  curl -s -X GET "${BASE_URL}/api/pgn/analysis/${GAME_ID}/1" | python3 -m json.tool
  echo ""
  echo ""

  # 测试6: 获取第10步的分析结果
  echo "测试6: 获取第10步的分析结果"
  echo "----------------------------------------"
  curl -s -X GET "${BASE_URL}/api/pgn/analysis/${GAME_ID}/10" | python3 -m json.tool
  echo ""
else
  echo "⚠️  未能获取 Game ID，跳过后续测试"
fi

echo ""
echo "========================================"
echo "API 测试完成"
echo "========================================"

