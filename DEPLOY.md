# Render 部署指南

## 概述
本项目支持一键部署到 Render 平台，包含：
- 后端：Spring Boot (Java 17)
- 前端：Vue 3 + Vite
- 数据库：H2 文件模式（免费持久化）

## 部署前准备

### 1. 注册 Render 账号
- 访问 https://render.com/
- 使用 GitHub 账号登录（推荐）

### 2. 推送代码到 GitHub
```bash
# 初始化 Git 仓库（如果还没有）
git init
git add .
git commit -m "Initial commit for Render deployment"

# 创建 GitHub 仓库并推送
git remote add origin https://github.com/你的用户名/resale-platform.git
git push -u origin main
```

## 部署步骤

### 方式一：使用 Blueprint 一键部署（推荐）

1. 登录 Render Dashboard: https://dashboard.render.com/
2. 点击 "New +" → "Blueprint"
3. 选择你的 GitHub 仓库
4. Render 会自动读取 `render.yaml` 配置并创建服务
5. 等待部署完成（约 3-5 分钟）

### 方式二：手动创建服务

#### 1. 部署后端服务
1. 点击 "New +" → "Web Service"
2. 选择 GitHub 仓库
3. 配置：
   - **Name**: `resale-platform-backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/platform-1.0.0.jar`
   - **Plan**: Free
4. 添加环境变量：
   - `JAVA_VERSION`: `17`
   - `SPRING_PROFILES_ACTIVE`: `prod`
5. 添加磁盘（用于数据持久化）：
   - **Name**: `data`
   - **Mount Path**: `/data`
   - **Size**: 1 GB
6. 点击 "Create Web Service"

#### 2. 部署前端静态站点
1. 点击 "New +" → "Static Site"
2. 选择同一个 GitHub 仓库
3. 配置：
   - **Name**: `resale-platform-frontend`
   - **Build Command**: `cd frontend && npm install && npm run build`
   - **Publish Directory**: `./frontend/dist`
   - **Plan**: Free
4. 添加环境变量：
   - `NODE_VERSION`: `18`
5. 添加重写规则：
   - Source: `/api/*` → Destination: `https://resale-platform-backend.onrender.com/api/$1`
   - Source: `/ws/*` → Destination: `wss://resale-platform-backend.onrender.com/ws/$1`
   - Source: `/*` → Destination: `/index.html`
6. 点击 "Create Static Site"

## 部署后配置

### 1. 获取服务地址
- 后端: `https://resale-platform-backend.onrender.com`
- 前端: `https://resale-platform-frontend.onrender.com`

### 2. 修改前端 API 地址（如果使用手动部署）
编辑 `frontend/.env.production`：
```env
VITE_API_BASE_URL=https://你的后端服务名.onrender.com/api
VITE_WS_URL=你的后端服务名.onrender.com
```

### 3. 重新部署前端
修改环境变量后，前端会自动重新部署。

## 初始化数据

首次部署后，需要手动创建管理员账号：

1. 访问后端 Swagger API 文档：
   ```
   https://resale-platform-backend.onrender.com/api/swagger-ui.html
   ```

2. 或使用 curl 注册管理员：
   ```bash
   curl -X POST https://resale-platform-backend.onrender.com/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "password": "Test123456",
       "mobile": "13800138000",
       "role": "ADMIN"
     }'
   ```

## 注意事项

### 免费版限制
- **Web Service**: 15分钟无活动会休眠，首次访问需等待唤醒（约 30 秒）
- **Static Site**: 无限制，但每月 100 GB 带宽
- **磁盘**: 1 GB 免费存储

### 数据持久化
- H2 数据库文件存储在 `/data/resale_platform.mv.db`
- 磁盘数据会持久化，服务重启不会丢失
- 建议定期备份（可通过 Render 控制台下载）

### 自定义域名（可选）
1. 在 Render 控制台点击 "Settings" → "Custom Domains"
2. 添加你的域名
3. 按提示配置 DNS 记录

## 故障排查

### 后端启动失败
查看日志检查：
1. Java 版本是否正确（应为 17）
2. 数据库目录是否有写入权限
3. 端口是否被占用（使用 10000）

### 前端无法连接后端
1. 检查 CORS 配置是否允许前端域名
2. 检查 `.env.production` 中的 API 地址是否正确
3. 检查 Render 的 Rewrite 规则是否配置

### 数据丢失
1. 检查磁盘是否正确挂载到 `/data`
2. 检查 `application-prod.yml` 中的数据库路径是否为 `/data/resale_platform`

## 更新部署

推送代码到 GitHub 后，Render 会自动重新部署：
```bash
git add .
git commit -m "Update features"
git push
```

## 技术支持

- Render 文档: https://render.com/docs
- 项目 Issues: （你的 GitHub 仓库地址）
