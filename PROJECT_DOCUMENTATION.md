# C2C二手交易平台 - 技术文档与项目介绍

## 一、项目概述

### 1.1 项目背景
随着互联网技术的发展和共享经济的兴起，二手交易市场迎来了快速增长。C2C（Consumer to Consumer）模式让个人用户能够直接进行闲置物品交易，既环保又经济。本项目旨在构建一个功能完善、安全可靠的C2C二手交易平台，为用户提供便捷的商品发布、搜索、交易和沟通服务。

### 1.2 项目目标
- 构建高性能、高可用的二手交易平台
- 实现安全可靠的用户认证和授权机制
- 提供流畅的实时通信体验
- 建立完善的商品管理和订单系统
- 支持多角色权限管理（普通用户、管理员）

### 1.3 核心功能
| 功能模块 | 主要特性 |
|---------|---------|
| 用户认证 | 账号密码登录、短信验证码登录、JWT无状态认证 |
| 商品管理 | 发布商品、编辑商品、商品搜索、分类浏览 |
| 订单系统 | 创建订单、订单状态管理、订单历史 |
| 实时通信 | WebSocket聊天、消息持久化、离线消息 |
| 互动功能 | 商品评论、点赞、收藏 |
| 管理后台 | 用户管理、权限控制、系统消息推送 |

---

## 二、技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层 (Client Layer)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Web浏览器   │  │   移动端浏览器 │  │   桌面浏览器  │          │
│  │   (Vue3 SPA) │  │   (响应式)     │  │   (完整功能)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTPS/WSS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      网关层 (Gateway Layer)                       │
│                    Nginx / Render Static Site                     │
│         静态资源托管 / 反向代理 / 负载均衡 / SSL终端                │
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   前端应用       │  │   后端API服务    │  │   WebSocket服务  │
│   (Vue 3)       │  │  (Spring Boot)  │  │  (Spring Boot)   │
│  - 用户界面      │  │  - RESTful API  │  │  - 实时消息推送   │
│  - 状态管理      │  │  - 业务逻辑      │  │  - 在线状态管理   │
│  - 路由控制      │  │  - 数据校验      │  │  - 消息广播      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   数据存储层     │  │   缓存层         │  │   文件存储       │
│   (H2/MySQL)    │  │   (Redis/H2)    │  │   (本地/OSS)     │
│  - 用户数据      │  │  - 会话缓存      │  │  - 商品图片      │
│  - 商品数据      │  │  - 验证码缓存    │  │  - 用户头像      │
│  - 订单数据      │  │  - Token黑名单   │  │                  │
│  - 消息数据      │  │                  │  │                  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 2.2 技术栈选型

#### 前端技术栈
| 技术 | 版本 | 用途 |
|-----|------|------|
| Vue.js | 3.4+ | 渐进式JavaScript框架 |
| Vite | 5.0+ | 前端构建工具 |
| Element Plus | 2.5+ | UI组件库 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.2+ | 路由管理 |
| Axios | 1.6+ | HTTP客户端 |
| SCSS | - | CSS预处理器 |

#### 后端技术栈
| 技术 | 版本 | 用途 |
|-----|------|------|
| Spring Boot | 3.2+ | 应用框架 |
| Spring Security | 6.2+ | 安全框架 |
| Spring WebSocket | 3.2+ | 实时通信 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| JWT (jjwt) | 0.12+ | 无状态认证 |
| H2 Database | 2.x | 内存数据库（演示） |
| Redis | 7.0+ | 缓存（可选） |
| Maven | 3.8+ | 构建工具 |

#### 部署与运维
| 技术 | 用途 |
|-----|------|
| Docker | 容器化部署 |
| Render | 云托管平台 |
| GitHub | 代码版本控制 |

---

## 三、功能模块详解

### 3.1 用户认证模块

#### 3.1.1 登录方式
1. **账号密码登录**
   - 支持用户名/手机号/邮箱三种格式
   - 图形验证码防暴力破解
   - 登录失败5次锁定15分钟
   - "记住我"延长Token有效期

2. **短信验证码登录**
   - 6位数字验证码
   - 有效期5分钟
   - 60秒防重发
   - 演示模式显示在控制台

3. **JWT认证机制**
   - HS512签名算法
   - Access Token: 2小时有效期
   - Refresh Token: 7天有效期（记住我）
   - Token黑名单机制

#### 3.1.2 安全特性
- 密码BCrypt加密存储
- 防SQL注入（MyBatis参数化查询）
- 防XSS攻击（前端转义输出）
- 登录限流保护
- CORS跨域配置

### 3.2 商品管理模块

#### 3.2.1 商品操作
- **发布商品**: 标题、描述、价格、分类、图片
- **编辑商品**: 修改商品信息
- **上下架**: 控制商品展示状态
- **删除商品**: 软删除（标记删除）

#### 3.2.2 商品浏览
- **分类浏览**: 多级分类树
- **搜索功能**: 关键词搜索
- **排序**: 价格、时间、热度
- **分页**: 支持大数据量分页

### 3.3 订单系统模块

#### 3.3.1 订单流程
```
创建订单 → 待付款 → 待发货 → 待收货 → 已完成
            ↓
         已取消
```

#### 3.3.2 订单状态
| 状态 | 值 | 说明 |
|-----|---|------|
| 待付款 | 0 | 订单创建，等待支付 |
| 待发货 | 1 | 已付款，等待卖家发货 |
| 待收货 | 2 | 已发货，等待买家确认 |
| 已完成 | 3 | 交易完成 |
| 已取消 | 4 | 订单取消 |

### 3.4 实时聊天模块

#### 3.4.1 架构设计
```
┌─────────────┐     WebSocket      ┌─────────────┐
│   用户A     │ ◄────────────────► │   服务器     │
│  (浏览器)    │                    │  (Spring Boot)│
└─────────────┘                    └──────┬──────┘
                                          │
                     ┌────────────────────┼────────────────────┐
                     ▼                    ▼                    ▼
              ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
              │  在线用户B   │      │  离线用户C   │      │   数据库     │
              │  实时推送    │      │  消息存储    │      │  持久化      │
              └─────────────┘      └─────────────┘      └─────────────┘
```

#### 3.4.2 功能特性
- **WebSocket实时通信**: 低延迟消息传递
- **轮询降级**: WebSocket失败时自动切换HTTP轮询
- **消息持久化**: 所有消息存储到数据库
- **未读计数**: 实时显示未读消息数量
- **在线状态**: 显示用户在线/离线状态
- **消息状态**: 发送中、已发送、已读

#### 3.4.3 消息类型
| 类型 | 说明 |
|-----|------|
| CHAT | 普通聊天消息 |
| READ | 已读回执 |
| PING/PONG | 心跳检测 |
| ERROR | 错误消息 |

### 3.5 管理员模块

#### 3.5.1 RBAC权限模型
```
用户(User) ──► 角色(Role) ──► 权限(Permission)
```

#### 3.5.2 角色定义
| 角色 | 权限 |
|-----|------|
| USER | 普通用户权限（发布商品、下单、聊天） |
| ADMIN | 管理员权限（用户管理、消息推送、系统配置） |

#### 3.5.3 管理功能
- 用户列表查看
- 用户状态管理（启用/禁用）
- 系统消息推送（站内信）
- 商品审核（预留）

---

## 四、数据库设计

### 4.1 核心实体关系

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    users    │       │   goods     │       │   orders    │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄──────┤ seller_id   │       │ buyer_id    │
│ username    │       │ id (PK)     │◄──────┤ seller_id   │
│ password    │       │ title       │       │ goods_id    │
│ mobile      │       │ price       │       │ status      │
│ email       │       │ category_id │──────►│ ...         │
│ role        │       │ ...         │       │             │
└─────────────┘       └─────────────┘       └─────────────┘
         │
         │            ┌─────────────┐       ┌─────────────┐
         │            │ chat_conver │       │ chat_message│
         │            ├─────────────┤       ├─────────────┤
         └───────────►│ user1_id    │◄──────┤ conv_id     │
                      │ user2_id    │       │ sender_id   │
                      │ ...         │       │ content     │
                      └─────────────┘       └─────────────┘
```

### 4.2 主要数据表

| 表名 | 用途 | 核心字段 |
|-----|------|---------|
| users | 用户信息 | id, username, password, mobile, email, role, status |
| goods | 商品信息 | id, seller_id, title, description, price, category_id, status |
| category | 商品分类 | id, name, parent_id, sort_order |
| orders | 订单信息 | id, order_no, buyer_id, seller_id, goods_id, price, status |
| chat_conversation | 聊天会话 | id, user1_id, user2_id, last_message_time |
| chat_message | 聊天消息 | id, conversation_id, sender_id, receiver_id, content, status |
| comments | 商品评论 | id, goods_id, user_id, content, parent_id, like_count |
| favorite | 收藏记录 | id, user_id, goods_id |
| message | 系统消息 | id, user_id, title, content, type, is_read |

---

## 五、API接口规范

### 5.1 接口设计原则
- RESTful设计风格
- 统一响应格式
- 版本控制（v1）
- 幂等性设计

### 5.2 响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 5.3 主要接口列表

#### 认证接口
| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/auth/login | POST | 账号密码登录 |
| /api/auth/sendSms | POST | 发送短信验证码 |
| /api/auth/loginBySms | POST | 短信验证码登录 |
| /api/auth/register | POST | 用户注册 |
| /api/auth/logout | POST | 用户登出 |
| /api/auth/currentUser | GET | 获取当前用户 |

#### 商品接口
| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/products | GET | 商品列表 |
| /api/products | POST | 发布商品 |
| /api/products/{id} | GET | 商品详情 |
| /api/products/{id} | PUT | 更新商品 |
| /api/products/categories | GET | 商品分类 |

#### 订单接口
| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/orders | POST | 创建订单 |
| /api/orders | GET | 我的订单 |
| /api/orders/{id} | GET | 订单详情 |
| /api/orders/{id}/status | PUT | 更新订单状态 |

#### 聊天接口
| 接口 | 方法 | 说明 |
|-----|------|------|
| /ws/chat | WebSocket | 实时聊天连接 |
| /api/chat/conversations | GET | 会话列表 |
| /api/chat/messages | GET | 消息列表 |
| /api/chat/messages | POST | 发送消息 |
| /api/chat/unread | GET | 未读消息数 |

---

## 六、部署架构

### 6.1 部署环境
- **平台**: Render (https://render.com)
- **前端**: Static Site
- **后端**: Web Service (Docker)
- **数据库**: H2内存数据库（演示环境）

### 6.2 部署流程
```
Git Push → GitHub → Render Auto Deploy → Live
```

### 6.3 环境配置
| 环境 | 前端URL | 后端URL | 数据库 |
|-----|---------|---------|--------|
| 开发 | http://localhost:5173 | http://localhost:8080/api | H2内存 |
| 生产 | https://resale-platform-frontend.onrender.com | https://resale-platform-backend.onrender.com/api | H2内存 |

---

## 七、安全设计

### 7.1 认证安全
- JWT Token + Bearer认证
- Token有效期控制
- 黑名单机制（登出失效）
- 登录失败锁定

### 7.2 传输安全
- HTTPS全站加密
- WebSocket over TLS (WSS)
- CORS跨域限制

### 7.3 数据安全
- 密码BCrypt加密
- SQL注入防护（参数化查询）
- XSS攻击防护（输入过滤、输出转义）

---

## 八、性能优化

### 8.1 前端优化
- 代码分割（Code Splitting）
- 懒加载（Lazy Loading）
- 组件缓存
- 图片懒加载

### 8.2 后端优化
- 数据库连接池（HikariCP）
- 缓存机制（Redis）
- 分页查询
- 索引优化

### 8.3 网络优化
- Gzip压缩
- CDN静态资源
- HTTP/2

---

## 九、测试账号

### 9.1 管理员账号
- 用户名: `admin`
- 手机号: `13800138000`
- 密码: `Test123456`
- 角色: ADMIN

### 9.2 普通用户账号
- 用户名: `user1`
- 手机号: `13800138001`
- 密码: `Test123456`
- 角色: USER

- 用户名: `user2`
- 手机号: `13800138002`
- 密码: `Test123456`
- 角色: USER

---

## 十、项目结构

```
c2c-secondhand-platform/
├── backend/                          # 后端项目
│   ├── src/main/java/com/resale/platform/
│   │   ├── config/                   # 配置类
│   │   ├── controller/               # 控制器层
│   │   ├── service/                  # 业务逻辑层
│   │   ├── mapper/                   # 数据访问层
│   │   ├── entity/                   # 实体类
│   │   ├── dto/                      # 数据传输对象
│   │   ├── security/                 # 安全相关
│   │   ├── websocket/                # WebSocket处理器
│   │   └── common/                   # 公共组件
│   ├── src/main/resources/
│   │   ├── application.yml           # 配置文件
│   │   ├── application-prod.yml      # 生产配置
│   │   ├── schema.sql                # 数据库结构
│   │   └── data.sql                  # 初始数据
│   ├── Dockerfile                    # Docker镜像
│   └── pom.xml                       # Maven配置
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── api/                      # API接口
│   │   ├── assets/                   # 静态资源
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # 状态管理
│   │   ├── utils/                    # 工具函数
│   │   ├── views/                    # 页面组件
│   │   └── App.vue                   # 根组件
│   ├── package.json                  # npm配置
│   └── vite.config.js                # Vite配置
├── render.yaml                       # Render部署配置
├── docker-compose.yml                # Docker编排
└── README.md                         # 项目说明
```

---

## 十一、开发团队与致谢

### 11.1 开发信息
- **项目类型**: 毕业设计项目
- **开发周期**: 2026年
- **开源协议**: MIT License

### 11.2 技术致谢
- Vue.js Team
- Spring Team
- Element Plus Team
- Render Platform

---

## 十二、联系方式

- **GitHub**: https://github.com/wxuhao32/c2c-secondhand-platform
- **演示地址**: https://resale-platform-frontend.onrender.com

---

*本文档最后更新日期: 2026-05-01*
