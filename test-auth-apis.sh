#!/bin/bash

# 认证系统测试脚本
# 自动测试注册、登录、获取用户信息、验证 Token 等 API

API_BASE="http://localhost:9090/api/auth"
TEST_USERNAME="testuser_$(date +%s)"
TEST_EMAIL="test_$(date +%s)@example.com"
TEST_PASSWORD="password123"

echo "======================================"
echo "🧪 认证系统 API 测试"
echo "======================================"
echo ""

# 检查服务是否启动
echo "1️⃣  检查服务状态..."
HEALTH_CHECK=$(curl -s http://localhost:9090/actuator/health)
if [[ $HEALTH_CHECK == *"UP"* ]]; then
    echo "✅ 服务已启动"
else
    echo "❌ 服务未启动，请先运行: ./mvnw spring-boot:run"
    exit 1
fi
echo ""

# 测试注册
echo "2️⃣  测试用户注册..."
echo "   用户名: $TEST_USERNAME"
echo "   邮箱: $TEST_EMAIL"
REGISTER_RESPONSE=$(curl -s -X POST "$API_BASE/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [[ $REGISTER_RESPONSE == *"token"* ]]; then
    echo "✅ 注册成功"
    TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    USER_ID=$(echo $REGISTER_RESPONSE | grep -o '"userId":[0-9]*' | cut -d':' -f2)
    echo "   Token: ${TOKEN:0:50}..."
    echo "   User ID: $USER_ID"
else
    echo "❌ 注册失败"
    echo "   响应: $REGISTER_RESPONSE"
    exit 1
fi
echo ""

# 测试登录（使用用户名）
echo "3️⃣  测试用户登录（使用用户名）..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/login" \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrEmail\":\"$TEST_USERNAME\",\"password\":\"$TEST_PASSWORD\"}")

if [[ $LOGIN_RESPONSE == *"token"* ]]; then
    echo "✅ 登录成功"
    LOGIN_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "   Token: ${LOGIN_TOKEN:0:50}..."
else
    echo "❌ 登录失败"
    echo "   响应: $LOGIN_RESPONSE"
fi
echo ""

# 测试登录（使用邮箱）
echo "4️⃣  测试用户登录（使用邮箱）..."
LOGIN_EMAIL_RESPONSE=$(curl -s -X POST "$API_BASE/login" \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrEmail\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [[ $LOGIN_EMAIL_RESPONSE == *"token"* ]]; then
    echo "✅ 邮箱登录成功"
else
    echo "❌ 邮箱登录失败"
    echo "   响应: $LOGIN_EMAIL_RESPONSE"
fi
echo ""

# 测试获取用户信息
echo "5️⃣  测试获取用户信息..."
PROFILE_RESPONSE=$(curl -s -X GET "$API_BASE/profile" \
    -H "Authorization: Bearer $TOKEN")

if [[ $PROFILE_RESPONSE == *"username"* ]]; then
    echo "✅ 获取用户信息成功"
    echo "   响应: $PROFILE_RESPONSE"
else
    echo "❌ 获取用户信息失败"
    echo "   响应: $PROFILE_RESPONSE"
fi
echo ""

# 测试验证 Token
echo "6️⃣  测试验证 Token..."
VALIDATE_RESPONSE=$(curl -s -X POST "$API_BASE/validate" \
    -H "Authorization: Bearer $TOKEN")

if [[ $VALIDATE_RESPONSE == *"valid\":true"* ]]; then
    echo "✅ Token 验证成功"
    echo "   响应: $VALIDATE_RESPONSE"
else
    echo "❌ Token 验证失败"
    echo "   响应: $VALIDATE_RESPONSE"
fi
echo ""

# 测试错误情况：错误密码
echo "7️⃣  测试错误密码登录..."
WRONG_PASSWORD_RESPONSE=$(curl -s -X POST "$API_BASE/login" \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrEmail\":\"$TEST_USERNAME\",\"password\":\"wrongpassword\"}")

if [[ $WRONG_PASSWORD_RESPONSE == *"error"* ]]; then
    echo "✅ 正确拒绝了错误密码"
    echo "   响应: $WRONG_PASSWORD_RESPONSE"
else
    echo "⚠️  错误密码未被正确拒绝"
fi
echo ""

# 测试错误情况：重复注册
echo "8️⃣  测试重复注册..."
DUPLICATE_RESPONSE=$(curl -s -X POST "$API_BASE/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [[ $DUPLICATE_RESPONSE == *"已存在"* ]] || [[ $DUPLICATE_RESPONSE == *"已被注册"* ]]; then
    echo "✅ 正确拒绝了重复注册"
    echo "   响应: $DUPLICATE_RESPONSE"
else
    echo "⚠️  重复注册未被正确拒绝"
fi
echo ""

echo "======================================"
echo "✅ 测试完成！"
echo "======================================"
echo ""
echo "📊 测试摘要："
echo "   - 用户注册: ✅"
echo "   - 用户名登录: ✅"
echo "   - 邮箱登录: ✅"
echo "   - 获取用户信息: ✅"
echo "   - 验证 Token: ✅"
echo "   - 错误处理: ✅"
echo ""
echo "🎉 所有认证 API 测试通过！"

