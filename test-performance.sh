#!/bin/bash

echo "========================================="
echo "性能测试报告 - $(date)"
echo "========================================="

# 测试配置
BASE_URL="http://localhost:9090"
USER_ID=1
START_DATE="2025-10-01"
END_DATE="2025-10-29"

# 测试1: 健康检查
echo -e "\n[测试1] 健康检查"
NO_PROXY=localhost time curl -s -o /dev/null -w "响应时间: %{time_total}s | HTTP状态: %{http_code}\n" \
  "$BASE_URL/actuator/health"

# 测试2: 趋势分析API
echo -e "\n[测试2] 趋势分析API (关键优化目标)"
NO_PROXY=localhost time curl -s -o /dev/null -w "响应时间: %{time_total}s | HTTP状态: %{http_code} | 响应大小: %{size_download} bytes\n" \
  "$BASE_URL/api/trends?userId=$USER_ID&startDate=$START_DATE&endDate=$END_DATE"

# 测试3: 并发性能测试 (5个并发请求)
echo -e "\n[测试3] 并发性能测试 (5个并发请求)"
for i in {1..5}; do
  NO_PROXY=localhost curl -s -o /dev/null -w "请求$i 响应时间: %{time_total}s\n" \
    "$BASE_URL/api/trends?userId=$USER_ID&startDate=$START_DATE&endDate=$END_DATE" &
done
wait
echo "并发测试完成"

# 测试4: 游戏列表API
echo -e "\n[测试4] 游戏列表API"
NO_PROXY=localhost time curl -s -o /dev/null -w "响应时间: %{time_total}s | HTTP状态: %{http_code}\n" \
  "$BASE_URL/api/pgn/games?userId=$USER_ID"

# 测试5: 开局分析API
echo -e "\n[测试5] 开局分析API"
NO_PROXY=localhost time curl -s -o /dev/null -w "响应时间: %{time_total}s | HTTP状态: %{http_code}\n" \
  "$BASE_URL/api/openings/stats/$USER_ID"

# 测试6: 失误分析API
echo -e "\n[测试6] 失误分析API"
NO_PROXY=localhost time curl -s -o /dev/null -w "响应时间: %{time_total}s | HTTP状态: %{http_code}\n" \
  "$BASE_URL/api/analysis/mistakes/$USER_ID"

echo -e "\n========================================="
echo "性能测试完成"
echo "========================================="
