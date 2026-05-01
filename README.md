---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: "00000000000000000000000000000000"
    PropagateID: "00000000000000000000000000000000"
    ReservedCode1: 30450220238c92fac57b0d29c3f802f5d87eba94ccfe41af72ec4244d2e6f23d17d7693e022100c6aad4c7533e8d8efab13fc165dd53a5370a080cd38a7d225fa232ba1b6aefd4
    ReservedCode2: 3046022100c02fd68d50a2c37ebb01e30e8923870117fc0358622282f810c60da2bc71392b022100c6a1d660ed0a4fa1e9059211fbc6d48842dfffd746d30b336b6bc151406f48d1
---

# C2C二手交易平台 - 部署说明

## 项目概述

这是一个基于 Vue3 + Spring Boot 的 C2C 二手交易平台，重点实现了用户认证模块。

## 技术栈

- **前端**: Vue 3 + Vite + Element Plus + Pinia + Axios
- **后端**: Spring Boot 3.2 + MyBatis-Plus + Spring Security
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **认证**: JWT (HS512)

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 1. 数据库初始化

```bash
# 登录MySQL并执行初始化脚本
mysql -u root -p < database/init.sql
```

### 2. 后端启动

```bash
cd backend

# 修改 application.yml 中的数据库和Redis配置
# 然后启动应用

# 使用Maven启动
mvn spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/platform-1.0.0.jar
```

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产环境构建
npm run build
```

### 4. 访问应用

- 前端地址: http://localhost:3000
- 后端API: http://localhost:8080/api
- 测试账号:
  - 用户名: `admin` / 手机: `13800138000` / 邮箱: `admin@example.com`
  - 密码: `Test123456`

## Docker部署

### 使用Docker Compose一键启动

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 手动Docker构建

```bash
# 构建后端镜像
cd backend
docker build -t resale-platform-backend .

# 构建前端镜像
cd frontend
docker build -t resale-platform-frontend .

# 运行容器
docker run -d -p 8080:8080 --name backend resale-platform-backend
docker run -d -p 3000:80 --name frontend resale-platform-frontend
```

## API接口说明

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/captcha` | GET | 获取图形验证码 |
| `/api/auth/login` | POST | 账号密码登录 |
| `/api/auth/sendSms` | POST | 发送短信验证码 |
| `/api/auth/loginBySms` | POST | 短信验证码登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/logout` | POST | 登出 |
| `/api/auth/currentUser` | GET | 获取当前用户信息 |

### 请求示例

**账号密码登录**
```json
POST /api/auth/login
{
  "account": "13800138000",
  "password": "Test123456",
  "captchaKey": "abc123",
  "captchaCode": "Ab3Cd",
  "rememberMe": false
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "mobile": "13800138000",
      "email": "admin@example.com",
      "status": 1
    }
  }
}
```

## 核心功能

### 登录模块

- [x] 账号密码登录（支持用户名/手机/邮箱）
- [x] 图形验证码校验
- [x] 登录失败5次锁定15分钟
- [x] "记住我"延长Token有效期
- [x] 手机验证码登录
- [x] 微信OAuth登录（预留接口）
- [x] JWT无状态认证
- [x] Token黑名单机制

### 安全特性

- [x] 密码BCrypt加密存储
- [x] HTTPS传输（生产环境）
- [x] 防SQL注入
- [x] 防XSS攻击
- [x] 登录限流保护
- [x] IP限流保护

## 项目结构

```
c2c-secondhand-platform/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/resale/platform/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 服务层
│   │       ├── mapper/      # 数据访问层
│   │       ├── entity/      # 实体类
│   │       ├── dto/         # 数据传输对象
│   │       ├── security/    # 安全相关
│   │       └── common/      # 公共组件
│   └── src/main/resources/
│       ├── application.yml   # 配置文件
│       └── mapper/          # MyBatis映射
├── frontend/                # Vue3前端
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 组件
│   │   ├── router/          # 路由
│   │   ├── stores/          # 状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面
│   └── public/              # 公共资源
├── database/                # 数据库脚本
├── docker-compose.yml       # Docker编排
├── SPEC.md                  # 技术规范
└── README.md                # 说明文档
```

## 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key-here  # 生产环境请使用复杂的随机密钥
  access-token-validity-in-seconds: 7200      # 2小时
  refresh-token-validity-in-seconds: 604800   # 7天
```

### 安全配置

```yaml
security:
  login:
    max-fail-count: 5           # 最大失败次数
    lock-duration-minutes: 15   # 锁定时长
    captcha-validity-minutes: 5 # 验证码有效期
    sms-validity-minutes: 5     # 短信验证码有效期
```

## 测试

### 后端单元测试

```bash
cd backend
mvn test
```

### 前端E2E测试

```bash
cd frontend
npm run test:e2e
```

## 性能优化建议

1. **数据库优化**
   - 合理使用索引
   - 使用连接池
   - 定期清理过期数据

2. **Redis缓存**
   - 验证码缓存
   - Session管理
   - Token黑名单

3. **前端优化**
   - 路由懒加载
   - 组件按需加载
   - 图片压缩

## 常见问题

### 1. 数据库连接失败
检查MySQL服务是否启动，确认 `application.yml` 中的数据库配置正确。

### 2. Redis连接失败
确认Redis服务已启动，检查防火墙设置。

### 3. 跨域问题
检查Nginx配置是否正确设置跨域头，或在Spring Security中配置CORS。

### 4. Token过期
前端会自动处理401响应，提示用户重新登录。

## 开发团队

- **架构设计**: MiniMax Agent
- **技术栈**: Vue3 + Spring Boot
- **许可证**: MIT

## 更新日志

### v1.0.0 (2024-01)
- 完成登录模块核心功能
- 实现账号密码登录、手机验证码登录
- 集成JWT无状态认证
- 完成前端页面开发

## 联系方式

如有问题，请提交Issue或联系开发团队。
