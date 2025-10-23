#!/bin/bash

echo "🚀 启动 Stockfish 国际象棋分析器"
echo "=================================="
echo ""

# 检查 MySQL 是否运行
echo "📊 检查 MySQL 数据库..."
if ! pgrep -x "mysqld" > /dev/null; then
    echo "⚠️  MySQL 未运行，请先启动 MySQL 服务"
    echo "   提示：brew services start mysql"
    exit 1
fi
echo "✅ MySQL 正在运行"
echo ""

# 检查 Stockfish 是否安装
echo "🏰 检查 Stockfish 引擎..."
if [ ! -f "/usr/local/bin/stockfish" ]; then
    echo "⚠️  Stockfish 未安装在 /usr/local/bin/stockfish"
    echo "   提示：brew install stockfish"
    echo "   或修改 application.properties 中的 stockfish.engine.path"
else
    echo "✅ Stockfish 已安装"
fi
echo ""

# 启动 Spring Boot 应用
echo "🌱 启动 Spring Boot 应用..."
echo "=================================="
./mvnw spring-boot:run

