# 部署指南

> 项目: Stockfish Chess Analyzer  
> 版本: 1.0.0  
> 更新时间: 2025-10-29

---

## 目录
1. [系统要求](#系统要求)
2. [本地开发部署](#本地开发部署)
3. [生产环境部署](#生产环境部署)
4. [Docker部署](#docker部署)
5. [数据库配置](#数据库配置)
6. [性能优化建议](#性能优化建议)
7. [故障排查](#故障排查)

---

## 系统要求

### 最低配置
- **CPU**: 2核心
- **内存**: 4GB RAM
- **磁盘**: 10GB 可用空间
- **操作系统**: Linux / macOS / Windows

### 推荐配置
- **CPU**: 4核心+ (Stockfish分析需要CPU)
- **内存**: 8GB+ RAM
- **磁盘**: 20GB+ SSD
- **操作系统**: Ubuntu 20.04+ / macOS 12+

### 软件依赖
- **JDK**: 17+ (推荐 OpenJDK 17 或 21)
- **Maven**: 3.8+ 或使用项目自带的 `mvnw`
- **MySQL**: 8.0+ (推荐 9.1+)
- **Stockfish**: 15+ (国际象棋引擎)
- **Git**: 2.30+ (可选)

---

## 本地开发部署

### 1. 克隆项目

```bash
git clone https://github.com/LIMBOWV/ChessAnalyse.git
cd stockfish-analyzer
```

### 2. 安装MySQL数据库

#### macOS (使用Homebrew)
```bash
brew install mysql
brew services start mysql
mysql -u root -p
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql_secure_installation
```

#### Windows
下载并安装 [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

### 3. 创建数据库

```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE Chess CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户 (可选,建议生产环境使用)
CREATE USER 'chess_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON Chess.* TO 'chess_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 4. 安装Stockfish引擎

#### macOS (使用Homebrew)
```bash
brew install stockfish
which stockfish  # 查看安装路径,通常是 /opt/homebrew/bin/stockfish
```

#### Ubuntu/Debian
```bash
sudo apt install stockfish
which stockfish  # 通常是 /usr/games/stockfish
```

#### Windows
1. 下载 [Stockfish for Windows](https://stockfishchess.org/download/)
2. 解压到 `C:\Program Files\Stockfish\`
3. 记录 `stockfish.exe` 的完整路径

#### 手动安装 (所有平台)
```bash
# 下载最新版本
wget https://github.com/official-stockfish/Stockfish/releases/download/sf_16/stockfish-ubuntu-x86-64-avx2.tar
tar -xvf stockfish-ubuntu-x86-64-avx2.tar
sudo cp stockfish/stockfish-ubuntu-x86-64-avx2 /usr/local/bin/stockfish
sudo chmod +x /usr/local/bin/stockfish
```

### 5. 配置应用

编辑 `src/main/resources/application.properties`:

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/Chess?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# Stockfish引擎路径 (根据实际安装路径修改)
# macOS Homebrew
stockfish.engine.path=/opt/homebrew/bin/stockfish

# Ubuntu/Debian
# stockfish.engine.path=/usr/games/stockfish

# Windows
# stockfish.engine.path=C:\\Program Files\\Stockfish\\stockfish.exe

# 服务器端口 (可选修改)
server.port=9090
```

### 6. 构建项目

```bash
# 使用项目自带的Maven Wrapper (推荐)
./mvnw clean package -DskipTests

# 或使用系统Maven
mvn clean package -DskipTests
```

**构建成功标志**:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  4.145 s
```

### 7. 运行应用

```bash
# 方式1: 直接运行JAR包
java -jar target/stockfish-analyzer-0.0.1-SNAPSHOT.jar

# 方式2: 使用Maven
./mvnw spring-boot:run

# 方式3: 后台运行
nohup java -jar target/stockfish-analyzer-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

**启动成功标志**:
```
Started StockfishAnalyzerApplication in 4.874 seconds
Tomcat started on port 9090 (http)
```

### 8. 验证部署

```bash
# 健康检查
curl http://localhost:9090/actuator/health
# 期望输出: {"status":"UP"}

# 访问主页
open http://localhost:9090
# 或在浏览器中打开 http://localhost:9090
```

---

## 生产环境部署

### 1. 环境准备

#### 创建专用用户
```bash
sudo useradd -m -s /bin/bash chess
sudo passwd chess
```

#### 安装JDK
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证安装
java -version
```

### 2. 生产配置

创建生产环境配置文件 `application-prod.properties`:

```properties
# 服务器配置
server.port=8080
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/json

# 数据库配置 (使用环境变量)
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/Chess}
spring.datasource.username=${DB_USERNAME:chess_user}
spring.datasource.password=${DB_PASSWORD}

# JPA配置 (生产环境)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# 连接池优化
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# 日志配置
logging.level.root=INFO
logging.level.org.example.stockfishanalyzer=INFO
logging.file.name=/var/log/chess-analyzer/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Stockfish配置
stockfish.engine.path=/usr/local/bin/stockfish
stockfish.analysis.movetime=1000
stockfish.analysis.depth=18

# 异步任务配置
spring.task.execution.pool.core-size=10
spring.task.execution.pool.max-size=20
spring.task.execution.pool.queue-capacity=200
```

### 3. 使用Systemd管理服务

创建服务文件 `/etc/systemd/system/chess-analyzer.service`:

```ini
[Unit]
Description=Stockfish Chess Analyzer
After=network.target mysql.service

[Service]
Type=simple
User=chess
Group=chess
WorkingDirectory=/home/chess/stockfish-analyzer
ExecStart=/usr/bin/java -jar \
  -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  /home/chess/stockfish-analyzer/target/stockfish-analyzer-0.0.1-SNAPSHOT.jar

# 环境变量
Environment="DB_PASSWORD=your_secure_password"
Environment="DB_URL=jdbc:mysql://localhost:3306/Chess"
Environment="DB_USERNAME=chess_user"

# 重启策略
Restart=on-failure
RestartSec=10

# 日志
StandardOutput=journal
StandardError=journal
SyslogIdentifier=chess-analyzer

[Install]
WantedBy=multi-user.target
```

**启用并启动服务**:
```bash
sudo systemctl daemon-reload
sudo systemctl enable chess-analyzer
sudo systemctl start chess-analyzer

# 查看状态
sudo systemctl status chess-analyzer

# 查看日志
sudo journalctl -u chess-analyzer -f
```

### 4. Nginx反向代理

安装Nginx:
```bash
sudo apt install nginx
```

创建配置文件 `/etc/nginx/sites-available/chess-analyzer`:

```nginx
upstream chess_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name chess.yourdomain.com;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # 日志
    access_log /var/log/nginx/chess-analyzer-access.log;
    error_log /var/log/nginx/chess-analyzer-error.log;

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        proxy_pass http://chess_backend;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API代理
    location / {
        proxy_pass http://chess_backend;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 文件上传大小限制
    client_max_body_size 10M;
}
```

**启用配置**:
```bash
sudo ln -s /etc/nginx/sites-available/chess-analyzer /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 5. HTTPS配置 (Let's Encrypt)

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d chess.yourdomain.com

# 自动续期测试
sudo certbot renew --dry-run
```

---

## Docker部署

### 1. 创建Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine

# 安装Stockfish
RUN apk add --no-cache stockfish

WORKDIR /app

# 复制JAR包
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/chess-analyzer

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
  "-Xms512m", \
  "-Xmx2g", \
  "-Dspring.profiles.active=prod", \
  "-jar", \
  "app.jar"]
```

### 2. 创建docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:9.1
    container_name: chess-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: Chess
      MYSQL_USER: chess_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  chess-analyzer:
    build: .
    container_name: chess-analyzer
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      DB_URL: jdbc:mysql://mysql:3306/Chess?useSSL=false&serverTimezone=UTC
      DB_USERNAME: chess_user
      DB_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/var/log/chess-analyzer
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: chess-nginx
    depends_on:
      - chess-analyzer
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    restart: unless-stopped

volumes:
  mysql-data:
```

### 3. 创建.env文件

```bash
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_PASSWORD=your_secure_password
```

### 4. 启动容器

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f chess-analyzer

# 查看状态
docker-compose ps

# 停止服务
docker-compose down

# 完全清理 (包括数据卷)
docker-compose down -v
```

---

## 数据库配置

### 1. 性能优化

编辑MySQL配置文件 `/etc/mysql/my.cnf`:

```ini
[mysqld]
# 基础配置
max_connections = 200
max_allowed_packet = 16M

# InnoDB配置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 查询缓存 (MySQL 8.0已移除)
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow-query.log
long_query_time = 2
```

### 2. 索引验证

登录MySQL并检查索引:

```sql
USE Chess;

-- 查看GamePgn表索引
SHOW INDEX FROM tbl_game_pgn;

-- 查看AnalysisResult表索引
SHOW INDEX FROM tbl_analysis_result;

-- 分析表性能
ANALYZE TABLE tbl_game_pgn;
ANALYZE TABLE tbl_analysis_result;
```

### 3. 定期备份

```bash
# 创建备份脚本 /home/chess/backup.sh
#!/bin/bash
BACKUP_DIR="/home/chess/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/chess_backup_$DATE.sql"

mkdir -p $BACKUP_DIR

mysqldump -u chess_user -p'your_password' Chess > $BACKUP_FILE

# 压缩
gzip $BACKUP_FILE

# 删除7天前的备份
find $BACKUP_DIR -name "chess_backup_*.sql.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_FILE.gz"
```

**设置定时任务**:
```bash
crontab -e

# 每天凌晨2点备份
0 2 * * * /home/chess/backup.sh >> /var/log/chess-backup.log 2>&1
```

---

## 性能优化建议

### 1. JVM调优

```bash
java -jar \
  -Xms2g \
  -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/chess-analyzer/heapdump.hprof \
  app.jar
```

### 2. 应用层优化

- ✅ 已启用数据库索引 (6个战略性索引)
- ✅ 已配置Hibernate批处理 (batch_size=20)
- ✅ 已优化HikariCP连接池
- 🔄 建议添加Redis缓存层
- 🔄 建议使用CDN加速静态资源

### 3. 监控指标

使用Spring Boot Actuator监控:

```properties
# application-prod.properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

---

## 故障排查

### 1. 应用无法启动

**症状**: 启动失败或卡住

**检查步骤**:
```bash
# 检查端口占用
lsof -i :8080
netstat -tuln | grep 8080

# 检查数据库连接
mysql -h localhost -u chess_user -p -e "SHOW DATABASES;"

# 检查Stockfish
stockfish quit

# 查看详细日志
tail -f /var/log/chess-analyzer/application.log
```

### 2. 数据库连接失败

**错误**: `Access denied for user`

**解决**:
```sql
-- 重置密码
ALTER USER 'chess_user'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;

-- 检查权限
SHOW GRANTS FOR 'chess_user'@'localhost';
```

### 3. Stockfish引擎错误

**错误**: `Stockfish not found` 或 `Permission denied`

**解决**:
```bash
# 检查路径
which stockfish

# 检查权限
ls -l /opt/homebrew/bin/stockfish
chmod +x /opt/homebrew/bin/stockfish

# 测试引擎
echo "quit" | stockfish
```

### 4. 性能问题

**症状**: API响应慢 (> 1秒)

**诊断**:
```bash
# 检查数据库慢查询
tail -f /var/log/mysql/slow-query.log

# 检查JVM内存
jmap -heap <PID>

# 检查连接池
# 查看应用日志中的HikariCP警告
```

**优化**:
- 增加数据库连接池大小
- 添加缺失的索引
- 优化查询SQL
- 增加JVM堆内存

### 5. 内存溢出

**错误**: `java.lang.OutOfMemoryError`

**解决**:
```bash
# 分析堆转储文件
jhat /var/log/chess-analyzer/heapdump.hprof

# 或使用VisualVM, MAT等工具分析
```

---

## 安全建议

1. **数据库安全**
   - 使用强密码
   - 禁用root远程访问
   - 定期备份

2. **应用安全**
   - 启用HTTPS
   - 配置CORS白名单
   - 添加JWT认证 (未来版本)

3. **服务器安全**
   - 配置防火墙 (UFW/iptables)
   - 定期更新系统补丁
   - 限制SSH访问

4. **监控告警**
   - 配置Prometheus + Grafana
   - 设置磁盘/内存/CPU告警
   - 监控应用错误率

---

## 附录

### 快速命令参考

```bash
# 启动应用
sudo systemctl start chess-analyzer

# 停止应用
sudo systemctl stop chess-analyzer

# 重启应用
sudo systemctl restart chess-analyzer

# 查看日志
sudo journalctl -u chess-analyzer -f

# 数据库备份
mysqldump -u chess_user -p Chess > backup.sql

# 数据库恢复
mysql -u chess_user -p Chess < backup.sql
```

---

**文档版本**: v1.0.0  
**最后更新**: 2025-10-29  
**维护者**: David / GitHub Copilot  
**支持**: GitHub Issues
