#!/bin/bash

echo "======================================"
echo "🧪 完整功能测试"
echo "======================================"
echo ""

# 1. 测试健康检查
echo "1️⃣ 测试健康检查..."
curl -s http://localhost:9090/actuator/health
echo -e "\n"

# 2. 测试主页
echo "2️⃣ 测试主页..."
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" http://localhost:9090/
echo ""

# 3. 测试Dashboard页面
echo "3️⃣ 测试Dashboard页面..."
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" http://localhost:9090/dashboard.html
echo ""

# 4. 检查前端依赖库
echo "4️⃣ 检查前端依赖库..."
cd /Users/david/codeFile/idea/firstIdea/stockfish-analyzer/src/main/resources/static/vendor
echo "Vue 3: $(ls -lh vue.global.js 2>/dev/null | awk '{print $5}')"
echo "ECharts: $(ls -lh echarts.min.js 2>/dev/null | awk '{print $5}')"
echo "jQuery: $(ls -lh jquery-3.6.0.min.js 2>/dev/null | awk '{print $5}')"
echo "Chess.js: $(ls -lh chess.js 2>/dev/null | awk '{print $5}')"
echo ""

# 5. 测试用户注册
echo "5️⃣ 测试用户注册API..."
RANDOM_ID=$(date +%s)
curl -s -X POST http://localhost:9090/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser${RANDOM_ID}\",\"password\":\"Test123456\",\"email\":\"test${RANDOM_ID}@example.com\"}"
echo -e "\n"

# 6. 验证前端库是否被正确引用
echo "6️⃣ 验证Dashboard页面引用的前端库..."
curl -s http://localhost:9090/dashboard.html | grep -o 'src="[^"]*\.js"' | head -5
echo ""

echo "======================================"
echo "✅ 测试完成！"
echo "======================================"
echo ""
echo "📱 请在浏览器中访问："
echo "   主页: http://localhost:9090/"
echo "   Dashboard: http://localhost:9090/dashboard.html"
echo ""

