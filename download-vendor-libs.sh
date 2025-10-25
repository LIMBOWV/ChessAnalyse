#!/bin/bash

# Vue 3 前端依赖库下载脚本
# 用于修复损坏的vendor库文件

echo "🚀 开始下载前端依赖库..."

cd "$(dirname "$0")/src/main/resources/static/vendor" || exit 1

# 下载 Vue 3
echo "📦 下载 Vue 3..."
curl -L -o vue.global.js https://unpkg.com/vue@3.3.4/dist/vue.global.prod.js
if [ $? -eq 0 ]; then
    echo "✅ Vue 3 下载成功 ($(ls -lh vue.global.js | awk '{print $5}'))"
else
    echo "❌ Vue 3 下载失败"
fi

# 下载 ECharts
echo "📦 下载 ECharts..."
curl -L -o echarts.min.js https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js
if [ $? -eq 0 ]; then
    echo "✅ ECharts 下载成功 ($(ls -lh echarts.min.js | awk '{print $5}'))"
else
    echo "❌ ECharts 下载失败"
fi

# 下载 jQuery（如果不存在）
if [ ! -f "jquery-3.6.0.min.js" ] || [ ! -s "jquery-3.6.0.min.js" ]; then
    echo "📦 下载 jQuery..."
    curl -L -o jquery-3.6.0.min.js https://code.jquery.com/jquery-3.6.0.min.js
    if [ $? -eq 0 ]; then
        echo "✅ jQuery 下载成功 ($(ls -lh jquery-3.6.0.min.js | awk '{print $5}'))"
    else
        echo "❌ jQuery 下载失败"
    fi
fi

# 下载 Chess.js（如果不存在）
if [ ! -f "chess.js" ] || [ ! -s "chess.js" ]; then
    echo "📦 下载 Chess.js..."
    curl -L -o chess.js https://cdnjs.cloudflare.com/ajax/libs/chess.js/0.12.1/chess.min.js
    if [ $? -eq 0 ]; then
        echo "✅ Chess.js 下载成功 ($(ls -lh chess.js | awk '{print $5}'))"
    else
        echo "❌ Chess.js 下载失败"
    fi
fi

# 下载 Chessboard.js（如果不存在）
if [ ! -f "chessboard-1.0.0.min.js" ] || [ ! -s "chessboard-1.0.0.min.js" ]; then
    echo "📦 下载 Chessboard.js..."
    curl -L -o chessboard-1.0.0.min.js https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.js
    if [ $? -eq 0 ]; then
        echo "✅ Chessboard.js 下载成功 ($(ls -lh chessboard-1.0.0.min.js | awk '{print $5}'))"
    else
        echo "❌ Chessboard.js 下载失败"
    fi
fi

if [ ! -f "chessboard-1.0.0.min.css" ] || [ ! -s "chessboard-1.0.0.min.css" ]; then
    echo "📦 下载 Chessboard CSS..."
    curl -L -o chessboard-1.0.0.min.css https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.css
    if [ $? -eq 0 ]; then
        echo "✅ Chessboard CSS 下载成功 ($(ls -lh chessboard-1.0.0.min.css | awk '{print $5}'))"
    else
        echo "❌ Chessboard CSS 下载失败"
    fi
fi

echo ""
echo "🎉 下载完成！文件列表："
ls -lh

echo ""
echo "✅ 现在刷新浏览器页面即可看到正常显示的Dashboard！"


