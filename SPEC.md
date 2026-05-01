---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: "00000000000000000000000000000000"
    PropagateID: "00000000000000000000000000000000"
    ReservedCode1: 3045022100e54738fa9cb2615d187384e934a0ed02ac7e28c53246f6fa82c6aa3c12a6fed502200bb941a20b9c514a3e877cebfcb499aa69dd637c60b52a4c4f4a287cb05eb083
    ReservedCode2: 3045022100dc3da404b8961afadc94611d8e913645f55986b9e53887859ffd424cd2fa19ce0220634cf55fcf2e6eff887dc180efd37e5cf068de6ecb6460e2f663e0d33648c663
---

# C2C二手交易平台技术规范文档

## 1. 项目概述

### 1.1 项目定位
闲置物品交易平台（C2C模式），支持用户发布商品、搜索商品、管理订单、互评等核心功能。本文档重点实现用户认证模块，为整个平台提供安全可靠的身份验证服务。

### 1.2 项目目标
构建一个高性能、高可用的用户认证系统，支持多种登录方式，具备完善的安全防护机制，为后续商品交易功能奠定坚实基础。

### 1.3 预期用户
- 二手商品卖家：需要发布商品、管理订单
- 二手商品买家：需要浏览商品、下单购买
- 平台管理员：管理用户、商品、订单

## 2. 技术架构

### 2.1 技术栈选型

#### 前端技术
- **框架**: Vue 3.4+ (Composition API)
- **UI组件库**: Element Plus 2.5+
- **状态管理**: Pinia 2.1+
- **HTTP客户端**: Axios 1.6+
- **路由管理**: Vue Router 4.2+
- **构建工具**: Vite 5.0+

#### 后端技术
- **框架**: Spring Boot 3.2+
- **ORM框架**: MyBatis-Plus 3.5+
- **安全框架**: Spring Security 6.2+
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **认证**: JWT (jjwt 0.12+)

#### 部署架构
- **容器化**: Docker + Docker Compose
- **负载均衡**: Nginx
- **反向代理**: Nginx

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户端 (Browser)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   登录页      │  │   注册页     │  │   首页       │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                     Nginx (反向代理 + 静态资源)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   前端服务    │    │   后端API     │    │   WebSocket  │
│   (Vue3)     │    │ (Spring Boot)│    │   服务        │
└──────────────┘    └──────────────┘    └──────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   MySQL      │    │   Redis      │    │   文件存储    │
│   (主库)     │    │   (缓存)      │    │   (OSS)      │
└──────────────┘    └──────────────┘    └──────────────┘
```

## 3. 功能规格

### 3.1 登录模块功能列表

#### 3.1.1 账号密码登录
- 支持用户名、手机号、邮箱三种账号格式
- 图形验证码校验（防机器人和暴力破解）
- 登录失败5次后锁定账户15分钟
- "记住我"功能延长JWT有效期
- 密码使用BCrypt加密存储

#### 3.1.2 手机验证码登录
- 输入手机号获取6位数字验证码
- 验证码有效期5分钟
- 验证码错误3次自动失效
- 60秒内禁止重复发送

#### 3.1.3 微信OAuth2.0登录
- 扫码授权获取微信用户信息
- 首次登录自动创建账户
- 绑定已有账户或创建新账户

#### 3.1.4 登出功能
- 清除本地Token
- 服务端Token黑名单机制
- 清理Redis会话数据

### 3.2 用户注册功能
- 用户名（唯一，4-20字符）
- 手机号（唯一，11位数字）
- 邮箱（唯一，有效格式）
- 密码（8-20位，包含大小写字母和数字）
- 图形验证码校验

### 3.3 密码管理
- 修改密码（需验证原密码）
- 忘记密码（手机/邮箱验证）

### 3.4 用户中心
- 个人信息查看和编辑
- 头像上传
- 收货地址管理

## 4. 接口设计

### 4.1 认证接口

#### 4.1.1 账号密码登录
```
POST /api/auth/login
Content-Type: application/json

请求参数:
{
  "account": "string",      // 用户名/手机/邮箱
  "password": "string",     // BCrypt加密后的密码
  "captchaKey": "string",    // 图形验证码Key
  "captchaCode": "string",   // 用户输入的验证码
  "rememberMe": boolean      // 是否记住我
}

成功响应 (200):
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 10001,
      "username": "zhangsan",
      "mobile": "13800138000",
      "email": "zhangsan@example.com",
      "avatar": "https://...",
      "status": 1
    }
  }
}

失败响应 (401):
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null
}

锁定响应 (403):
{
  "code": 403,
  "message": "账户已锁定，请15分钟后再试",
  "data": {
    "lockedUntil": "2024-01-01T12:15:00Z"
  }
}
```

#### 4.1.2 发送短信验证码
```
POST /api/auth/sendSms
Content-Type: application/json

请求参数:
{
  "mobile": "13800138000",
  "captchaKey": "string",
  "captchaCode": "string"
}

成功响应 (200):
{
  "code": 200,
  "message": "验证码发送成功",
  "data": {
    "smsKey": "sms:login:13800138000:abc123"
  }
}
```

#### 4.1.3 手机验证码登录
```
POST /api/auth/loginBySms
Content-Type: application/json

请求参数:
{
  "mobile": "13800138000",
  "smsCode": "123456",
  "rememberMe": false
}

成功响应 (200):
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 10001,
      "username": "zhangsan",
      "mobile": "13800138000",
      "avatar": "https://...",
      "status": 1,
      "isNewUser": false
    }
  }
}

新用户响应 (201):
{
  "code": 201,
  "message": "注册并登录成功",
  "data": {
    "token": "...",
    "userInfo": {
      "userId": 10002,
      "username": null,
      "mobile": "13800138000",
      "avatar": null,
      "status": 1,
      "isNewUser": true
    }
  }
}
```

#### 4.1.4 微信OAuth回调
```
GET /api/auth/wechat/callback?code=xxx&state=xxx

重定向到前端:
https://frontend domain/auth/wechat/callback?token=xxx&userId=xxx
```

#### 4.1.5 登出
```
POST /api/auth/logout
Authorization: Bearer {token}

成功响应 (200):
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

#### 4.1.6 获取当前用户信息
```
GET /api/auth/currentUser
Authorization: Bearer {token}

成功响应 (200):
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "mobile": "13800138000",
    "email": "zhangsan@example.com",
    "avatar": "https://...",
    "status": 1,
    "lastLoginTime": "2024-01-01T10:00:00Z",
    "lastLoginIp": "127.0.0.1"
  }
}
```

#### 4.1.7 获取图形验证码
```
GET /api/auth/captcha

成功响应 (200):
Content-Type: image/png

返回图片流
响应头:
X-Captcha-Key: captcha:abc123def456
X-Captcha-Expires-In: 300
```

### 4.2 用户管理接口

#### 4.2.1 用户注册
```
POST /api/user/register
Content-Type: application/json

请求参数:
{
  "username": "zhangsan",
  "mobile": "13800138000",
  "email": "zhangsan@example.com",
  "password": "Zhang123456",
  "captchaKey": "string",
  "captchaCode": "string"
}

成功响应 (201):
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "userId": 10001
  }
}
```

#### 4.2.2 更新个人信息
```
PUT /api/user/profile
Authorization: Bearer {token}
Content-Type: application/json

请求参数:
{
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "avatar": "https://..."
}

成功响应 (200):
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

#### 4.2.3 修改密码
```
PUT /api/user/password
Authorization: Bearer {token}
Content-Type: application/json

请求参数:
{
  "oldPassword": "Zhang123456",
  "newPassword": "NewPass123"
}

成功响应 (200):
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

## 5. 数据库设计

### 5.1 用户表 (user)
```sql
CREATE TABLE `user` (
  `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1正常 2锁定 3禁用',
  `login_fail_cnt` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until` DATETIME DEFAULT NULL COMMENT '锁定截止时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
  `wechat_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 5.2 用户收货地址表 (user_address)
```sql
CREATE TABLE `user_address` (
  `address_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `mobile` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';
```

### 5.3 操作日志表 (operation_log)
```sql
CREATE TABLE `operation_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) NOT NULL COMMENT '请求URL',
  `request_params` TEXT COMMENT '请求参数',
  `response_code` INT DEFAULT NULL COMMENT '响应码',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
```

## 6. Redis缓存设计

### 6.1 缓存Key规范
```
# 图形验证码
captcha:{key}                    -> 验证码答案 (5分钟TTL)

# 短信验证码
sms:login:{mobile}:{sessionId}   -> 短信验证码 (5分钟TTL)
sms:send:count:{mobile}          -> 发送计数 (24小时TTL)

# 登录失败计数
login:fail:{account}             -> 失败次数 (15分钟TTL)

# 用户登录锁定
user:lock:{userId}               -> 锁定信息 (15分钟TTL)

# JWT Token黑名单
token:blacklist:{jti}            -> 黑名单标记 (Token剩余TTL)

# 用户会话信息
user:session:{userId}            -> 会话信息 (2小时TTL)

# 短信验证码错误计数
sms:verify:fail:{mobile}         -> 验证失败次数 (5分钟TTL)
```

### 6.2 缓存数据结构

#### 图形验证码
```json
{
  "code": "Ab3Cd",
  "createTime": 1704067200000,
  "expiresAt": 1704067500000
}
```

#### 登录失败计数
```json
{
  "count": 5,
  "firstFailTime": 1704067200000,
  "lockedUntil": 1704068100000
}
```

## 7. 安全规范

### 7.1 密码安全
- **哈希算法**: BCrypt (强度因子12)
- **密码策略**: 8-20位，包含大小写字母和数字
- **传输安全**: HTTPS全站加密
- **可选增强**: 前端RSA加密密码传输

### 7.2 JWT安全
- **签名算法**: HS512
- **Token结构**:
  ```json
  {
    "header": {
      "alg": "HS512",
      "typ": "JWT"
    },
    "payload": {
      "sub": "10001",
      "jti": "unique-token-id",
      "iat": 1704067200,
      "exp": 1704070800,
      "type": "access"
    }
  }
  ```
- **普通Token有效期**: 2小时 (7200秒)
- **记住我Token有效期**: 7天 (604800秒)
- **Token刷新策略**: 无需刷新，前端主动重新登录

### 7.3 防暴力破解
- **图形验证码**: 5分钟有效期，区分大小写
- **登录失败锁定**: 5次失败锁定15分钟
- **IP限流**: 同一IP 5次/分钟
- **账号限流**: 同一账号5次/分钟

### 7.4 防CSRF攻击
- 使用JWT无状态认证，前端存储Token
- 请求头携带: `Authorization: Bearer {token}`

### 7.5 敏感操作防护
- 修改密码需要原密码验证
- 更换手机号需要短信验证码
- 大额操作需要二次验证

## 8. 前端架构

### 8.1 项目结构
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── api/                    # API接口
│   │   ├── auth.js             # 认证相关接口
│   │   └── user.js             # 用户相关接口
│   ├── assets/                 # 静态资源
│   │   ├── images/
│   │   └── styles/
│   ├── components/             # 公共组件
│   │   ├── Captcha.vue         # 图形验证码组件
│   │   ├── SmsButton.vue       # 短信发送按钮
│   │   └── PasswordInput.vue   # 密码输入框
│   ├── composables/            # 组合式函数
│   │   ├── useAuth.js          # 认证逻辑
│   │   └── useApi.js           # API调用封装
│   ├── layouts/               # 布局组件
│   │   ├── DefaultLayout.vue
│   │   └── AuthLayout.vue
│   ├── router/                 # 路由配置
│   │   ├── index.js
│   │   └── routes.js
│   ├── stores/                 # Pinia状态管理
│   │   ├── auth.js             # 认证状态
│   │   └── user.js             # 用户状态
│   ├── utils/                  # 工具函数
│   │   ├── request.js          # Axios封装
│   │   ├── storage.js          # 存储封装
│   │   └── validators.js       # 表单验证
│   ├── views/                  # 页面视图
│   │   ├── auth/               # 认证页面
│   │   │   ├── Login.vue       # 登录页
│   │   │   ├── Register.vue    # 注册页
│   │   │   └── ForgotPassword.vue
│   │   ├── home/               # 首页
│   │   │   └── Index.vue
│   │   └── user/               # 用户页面
│   │       ├── Profile.vue    # 个人中心
│   │       └── Settings.vue    # 设置页面
│   ├── App.vue
│   └── main.js
├── .env                        # 环境变量
├── .env.development
├── .env.production
├── package.json
└── vite.config.js
```

### 8.2 路由守卫
```javascript
// 路由配置示例
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Index.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue')
      }
    ]
  }
]

// 路由守卫逻辑
router.beforeEach((to, from, next) => {
  const token = storage.get('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})
```

### 8.3 Axios封装
```javascript
// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = storage.get('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 处理业务错误
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      // Token过期，清除并跳转登录
      storage.remove('token')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## 9. 后端架构

### 9.1 项目结构
```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/resale/platform/
│   │   │       ├── ResalePlatformApplication.java
│   │   │       ├── config/                    # 配置类
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   ├── WebConfig.java
│   │   │       │   └── JwtConfig.java
│   │   │       ├── controller/               # 控制器
│   │   │       │   ├── AuthController.java
│   │   │       │   └── UserController.java
│   │   │       ├── service/                  # 服务层
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── CaptchaService.java
│   │   │       │   └── SmsService.java
│   │   │       ├── service/impl/             # 服务实现
│   │   │       │   ├── AuthServiceImpl.java
│   │   │       │   ├── UserServiceImpl.java
│   │   │       │   ├── CaptchaServiceImpl.java
│   │   │       │   └── SmsServiceImpl.java
│   │   │       ├── mapper/                   # 数据访问层
│   │   │       │   ├── UserMapper.java
│   │   │       │   └── UserAddressMapper.java
│   │   │       ├── entity/                   # 实体类
│   │   │       │   ├── User.java
│   │   │       │   └── UserAddress.java
│   │   │       ├── dto/                     # 数据传输对象
│   │   │       │   ├── request/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── SmsLoginRequest.java
│   │   │       │   │   └── RegisterRequest.java
│   │   │       │   └── response/
│   │   │       │       ├── LoginResponse.java
│   │   │       │       └── UserInfoResponse.java
│   │   │       ├── security/                # 安全相关
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── UserDetailsServiceImpl.java
│   │   │       │   └── SecurityUser.java
│   │   │       ├── common/                  # 公共组件
│   │   │       │   ├── Result.java
│   │   │       │   ├── ExceptionEnum.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── util/                    # 工具类
│   │   │       │   ├── IpUtils.java
│   │   │       │   └── StringUtils.java
│   │   │       └── constant/                # 常量定义
│   │   │           ├── RedisKeys.java
│   │   │           └── AuthConstants.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── com/resale/platform/
│               ├── service/
│               │   └── AuthServiceTest.java
│               └── security/
│                   └── JwtTokenProviderTest.java
├── pom.xml
└── docker-compose.yml
```

### 9.2 核心登录流程
```
1. 接收登录请求 (account, password, captchaKey, captchaCode, rememberMe)
   ↓
2. 校验图形验证码
   - 从Redis获取验证码
   - 比对用户输入（忽略大小写）
   - 删除已使用的验证码
   ↓ (验证码错误)
3. 验证失败，返回验证码错误
   ↓ (验证码正确)
4. 查询用户
   - 尝试按用户名查询
   - 尝试按手机号查询
   - 尝试按邮箱查询
   ↓ (用户不存在)
5. 返回账号或密码错误
   ↓ (用户存在)
6. 检查账户状态
   - 检查是否被锁定 (locked_until > now)
   - 检查是否被禁用 (status != 1)
   ↓ (账户异常)
7. 返回账户异常信息
   ↓ (账户正常)
8. 验证密码
   - 使用BCrypt验证密码
   ↓ (密码错误)
9. 登录失败处理
   - 增加失败计数
   - 检查是否达到锁定阈值 (5次)
   - 如果达到，锁定账户15分钟
   ↓ (密码正确)
10. 登录成功处理
    - 重置失败计数
    - 更新最后登录时间和IP
    - 生成JWT Token
    - 存储Token黑名单标记Key
    ↓
11. 返回登录成功响应
    - token
    - expiresIn
    - userInfo
```

## 10. 非功能要求

### 10.1 性能要求
- 登录接口响应时间 < 200ms (P99)
- 注册接口响应时间 < 300ms (P99)
- 验证码获取响应时间 < 100ms (P99)
- 系统支持1000并发用户

### 10.2 可用性要求
- 系统可用性 >= 99.9%
- 单点故障不影响整体服务
- 数据库主从自动切换

### 10.3 安全性要求
- 全站HTTPS加密
- 密码BCrypt加密存储
- JWT签名验证
- 防SQL注入
- 防XSS攻击
- 防CSRF攻击

### 10.4 可扩展性要求
- 支持水平扩展
- 无状态设计，支持分布式部署
- Redis集群支持

### 10.5 监控要求
- 接口调用监控
- 错误日志记录
- 性能指标采集
- 用户行为分析

## 11. 测试计划

### 11.1 单元测试
- 密码加密解密测试
- JWT生成验证测试
- 验证码生成校验测试
- 业务逻辑测试

### 11.2 集成测试
- 登录流程测试
- 注册流程测试
- 安全防护测试

### 11.3 性能测试
- 并发登录测试
- 压力测试
- 响应时间测试

## 12. 部署方案

### 12.1 开发环境
- MySQL: localhost:3306
- Redis: localhost:6379
- 后端: localhost:8080
- 前端: localhost:3000

### 12.2 生产环境
- MySQL主从集群
- Redis集群
- Nginx反向代理
- Docker容器化部署
