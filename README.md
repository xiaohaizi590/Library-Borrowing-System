# 图书管理系统

//下一阶段的目标：对密钥进行软编码，完成。处理可能存在的高并发请求，还没有进行压测.对页面进行优化设计
采取一些防爬机制，如IP限制，验证码。增加前端报错的提示信息。
//对删除操作进行改正，要改成软删除，可以恢复信息，但是只能管理员进行使用
## 1. 项目概述

### 1.1 项目定位

本项目是一个基于 **Spring Boot** + **Vue 3** 构建的图书管理系统，提供完整的用户注册、登录、认证、授权、图书管理和借阅管理功能。系统采用前后端分离架构，后端提供 REST API，前端使用 Vue 3 + Vite + TailwindCSS 构建现代化 Web 界面。

### 1.2 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 后端编程语言 |
| Spring Boot | 3.2.0 | 后端应用框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 集成 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | 3.2.x | 数据访问 |
| JWT (JJWT) | 0.12.3 | 令牌认证 |这里准备改成软编码，不用硬编码，更加符合1应用场景
| MySQL | 8.x | 数据库 |
| Vue | 3.4.21 | 前端框架 |
| Vue Router | 4.2.5 | 前端路由 |
| Vite | 5.2.8 | 前端构建工具 |
| TailwindCSS | 3.4.1 | CSS 框架 |
| Axios | 1.6.5 | HTTP 客户端 |
| Lucide | 0.314.0 | 图标库 |
| Lombok | 1.18.30 | POJO 简化 |

### 1.3 项目结构

```
nacon7/                                    # 根目录
├── pom.xml                                 # 后端 Maven 父 POM（版本管理）
├── index.html                              # 前端入口 HTML
├── package.json                            # 前端依赖配置
├── vite.config.js                          # Vite 配置
├── tailwind.config.js                      # TailwindCSS 配置
├── postcss.config.js                       # PostCSS 配置
├── common/                                 # 后端公共模块
│   └── src/main/java/net/togogo/
│       ├── common/                         # 通用类（Result、ResultCode、BusinessException）
│       ├── dto/                            # 数据传输对象
│       └── entity/                         # 数据库实体（User、Book、BorrowRecord）
├── server/                                 # 后端服务模块
│   └── src/main/java/net/togogo/
│       ├── config/                         # 配置类（Security、Cors、DataInitializer）
│       ├── controller/                     # REST API控制器（UserController、BookController）
│       ├── repository/                     # 数据访问层（UserRepository、BookRepository、BorrowRecordRepository）
│       ├── security/                       # 安全组件（JWT、过滤器）
│       ├── server/                         # 启动类
│       ├── service/                        # 业务逻辑层（UserService、BookService）
│       └── util/                           # 工具类
├── src/                                    # 前端源码
│   ├── views/                              # 页面组件
│   │   ├── LoginView.vue                   # 登录页面
│   │   ├── RegisterView.vue                # 注册页面
│   │   ├── MainLayout.vue                  # 主布局（侧边栏）
│   │   ├── BookListView.vue                # 图书浏览页面
│   │   ├── MyBorrowView.vue                # 我的借阅页面
│   │   ├── ProfileView.vue                 # 个人信息页面
│   │   ├── BookManageView.vue              # 图书管理页面（管理员）
│   │   ├── UserManageView.vue              # 用户管理页面（管理员）
│   │   └── BorrowManageView.vue            # 借阅管理页面（管理员）
│   ├── router/                             # 路由配置
│   ├── services/                           # API 服务层
│   ├── utils/                              # 工具函数（API拦截器、认证）
│   ├── App.vue                             # 根组件
│   ├── main.js                             # 入口文件
│   └── style.css                           # 全局样式
└── docs/                                   # 文档目录
    └── 技术指南.md                          # 详细技术文档
```

---

## 2. 模块架构与职责

### 2.1 模块关系图

```
┌─────────────────────────────────────────────────────────────┐
│                         Nacos Server                         │
│                    (服务注册与配置中心 - 仅服务端)               │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
              ┌───────────────┐
              │   nacon7-server│
              │   (端口: 8081) │
              └───────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │   MySQL 8.x   │
              └───────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Vue 3 前端 (Vite)                         │
│              (端口: 5173 - 代理 /api → 8081)                │
│                                                              │
│   LoginView / RegisterView (登录/注册)                      │
│        ↓                                                     │
│   MainLayout (侧边栏导航 + 主内容区)                          │
│        ↓                                                     │
│   ├── BookListView    ── 图书浏览+搜索+借阅                  │
│   ├── MyBorrowView    ── 我的借阅+归还+续借                  │
│   ├── ProfileView     ── 个人信息                           │
│   ├── BookManageView  ── 图书管理CRUD (管理员)               │
│   ├── UserManageView  ── 用户管理 (管理员)                   │
│   └── BorrowManageView ── 借阅管理全部/逾期 (管理员)          │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 各模块职责

| 模块 | 职责 | 依赖 |
|------|------|------|
| **common** | 公共实体、DTO、通用返回类 | Spring Boot Starter、JPA API、Lombok |
| **server** | 用户服务端，提供 REST API（用户管理、图书管理、借阅管理） | common、Spring Web、Security、JPA、JWT、Nacos |
| **src** | Vue 3 前端，提供用户交互界面 | Vue 3、Vue Router、Vite、TailwindCSS、Axios |

---

## 3. 核心流程详解

### 3.1 用户注册流程

```
POST /api/users/register
├─ 接收 RegisterRequest (username, password, phone)
├─ 参数校验 (@NotBlank, @Size)
├─ 检查用户名是否已存在
├─ 检查手机号是否已存在
├─ BCrypt 加密密码
├─ 保存用户到数据库（默认角色 USER）
└─ 返回 UserDTO
```

### 3.2 用户登录流程

```
POST /api/users/login
├─ 接收 LoginRequest (account, password)
├─ 参数校验
├─ 根据 account 查询用户（仅支持 username）
├─ BCrypt 密码匹配校验
├─ JWT 生成令牌（含 userId、username）
└─ 返回 LoginResponse (id, username, email, phone, token, role)
```

### 3.3 JWT 认证流程

```
请求到达 → JwtAuthenticationFilter
              ├─ 提取 Authorization Header 中的 Bearer Token
              ├─ 验证 Token 有效性
              ├─ 解析 Token 获取 username
              ├─ 加载 UserDetails（含角色权限）
              ├─ 设置 SecurityContext 认证信息
              └─ 继续过滤器链 → Controller
```

### 3.4 前端认证流程

```
登录成功 → 存储 Token 和用户信息到 localStorage
              ↓
后续请求 → Axios 请求拦截器自动注入 Authorization 头
              ↓
Token 过期 → Axios 响应拦截器清除本地存储，跳转登录页
```

### 3.5 权限控制流程

```
Controller 方法标注 @PreAuthorize
    ├─ hasRole('ADMIN')         → 仅管理员可访问
    ├─ isAuthenticated()        → 登录用户可访问
    └─ @userServiceImpl.isOwnUser(#id)  → 仅本人可访问

前端路由守卫
    ├─ requiresAuth: true       → 未登录跳转登录页
    ├─ requiresAdmin: true      → 非管理员跳转首页
```

### 3.6 图书借阅流程

```
POST /api/books/borrow
├─ 接收 BorrowRequest (bookId, borrowDays)
├─ 从 SecurityContextHolder 获取当前登录用户名
├─ 根据用户名查询用户信息（获取 userId）
├─ 检查图书是否存在
├─ 检查图书库存是否充足
├─ 检查用户是否已借阅该图书
├─ 扣减图书库存
├─ 创建借阅记录（设置借阅时间、应还时间）
└─ 返回 BorrowRecordDTO
```

> **安全设计**：借阅接口不再接受客户端传入的 `userId`，改为从当前 JWT Token 中解析用户身份，彻底消除越权风险。

### 3.7 图书归还流程

```
POST /api/books/return/{recordId}
├─ 根据 recordId 查询借阅记录
├─ 检查记录是否处于借阅状态
├─ 查询图书信息
├─ 增加图书库存
├─ 更新借阅记录（设置归还时间、状态改为已归还）
└─ 返回 BorrowRecordDTO
```

---

## 4. API 接口清单

### 4.1 用户认证接口

| 接口路径 | HTTP方法 | 权限要求 | 说明 |
|----------|----------|----------|------|
| `/api/users/register` | POST | 无 | 用户注册 |
| `/api/users/login` | POST | 无 | 用户登录 |
| `/api/users/profile` | GET | 已认证 | 获取当前用户信息 |

### 4.2 用户管理接口

| 接口路径 | HTTP方法 | 权限要求 | 说明 |
|----------|----------|----------|------|
| `/api/users/getAllUsers` | GET | ADMIN | 获取所有用户（分页） |
| `/api/users/getUserById/{id}` | GET | ADMIN 或本人 | 根据ID获取用户 |
| `/api/users/getUserByUsername/{username}` | GET | ADMIN 或本人 | 根据用户名获取用户 |
| `/api/users/getUserByPhone/{phone}` | GET | ADMIN | 根据手机号获取用户 |
| `/api/users/updateUser/{id}` | PUT | ADMIN 或本人 | 更新用户信息 |
| `/api/users/deleteUser/{id}` | DELETE | ADMIN | 删除用户 |

### 4.3 图书管理接口

| 接口路径 | HTTP方法 | 权限要求 | 说明 |
|----------|----------|----------|------|
| `/api/books/create` | POST | ADMIN | 创建图书 |
| `/api/books/getById/{id}` | GET | 无 | 根据ID获取图书 |
| `/api/books/getAll` | GET | 无 | 获取所有图书（分页） |
| `/api/books/searchByTitle` | GET | 无 | 按标题搜索（分页） |
| `/api/books/searchByAuthor` | GET | 无 | 按作者搜索（分页） |
| `/api/books/searchByCategory` | GET | 无 | 按分类搜索（分页） |
| `/api/books/update/{id}` | PUT | ADMIN | 更新图书信息 |
| `/api/books/delete/{id}` | DELETE | ADMIN | 删除图书 |

### 4.4 借阅管理接口

| 接口路径 | HTTP方法 | 权限要求 | 说明 |
|----------|----------|----------|------|
| `/api/books/borrow` | POST | 已认证 | 借阅图书 |
| `/api/books/return/{recordId}` | POST | 已认证 | 归还图书 |
| `/api/books/renew/{recordId}` | POST | 已认证 | 续借图书 |
| `/api/books/borrowRecords/user/{userId}` | GET | 已认证 | 获取用户借阅记录（分页） |
| `/api/books/borrowRecords/book/{bookId}` | GET | ADMIN | 获取图书借阅记录 |
| `/api/books/borrowRecords/all` | GET | ADMIN | 获取全部借阅记录（分页） |
| `/api/books/overdue` | GET | ADMIN | 获取逾期记录（分页） |

### 4.5 通用响应格式

```json
{
  "code": "integer (状态码)",
  "message": "string (提示信息)",
  "data": "T (泛型数据，成功时返回)"
}
```

### 4.6 业务状态码

| 状态码 | 常量 | 说明 |
|--------|------|------|
| 200 | SUCCESS | 操作成功 |
| 400 | BAD_REQUEST | 参数错误 |
| 401 | UNAUTHORIZED | 未认证 |
| 403 | FORBIDDEN | 权限不足 |
| 404 | NOT_FOUND | 资源不存在 |
| 500 | INTERNAL_ERROR | 系统错误 |
| 1001 | USERNAME_EXIST | 用户名已存在 |
| 1002 | USER_NOT_FOUND | 用户不存在 |
| 1003 | PASSWORD_ERROR | 密码错误 |
| 1004 | TOKEN_INVALID | Token 无效 |
| 1005 | TOKEN_EXPIRED | Token 已过期 |
| 1006 | PHONE_EXIST | 手机号已存在 |
| 2001 | BOOK_ISBN_EXIST | ISBN已存在 |
| 2002 | BOOK_NOT_AVAILABLE | 图书库存不足 |
| 2003 | BOOK_ALREADY_BORROWED | 图书已被借阅 |
| 2004 | BOOK_BORROWED_CANNOT_DELETE | 图书已被借阅，无法删除 |
| 3001 | RECORD_NOT_BORROWED | 记录未处于借阅状态 |
| 3002 | MAX_RENEW_COUNT_EXCEEDED | 已达到最大续借次数 |

---

## 5. 数据模型

### 5.1 User 实体

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | Long | @Id, @GeneratedValue | 主键，自增 |
| username | String | @Column(nullable=false, unique=true, length=50) | 用户名 |
| password | String | @Column(nullable=false, length=100) | 加密后的密码 |
| email | String | 可选 | 邮箱 |
| phone | String | @Column(nullable=false, unique=true, length=11) | 手机号 |
| role | Role | @Enumerated(EnumType.STRING) | 角色（USER/ADMIN） |
| createTime | LocalDateTime | @PrePersist 自动填充 | 创建时间 |

### 5.2 Book 实体

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | Long | @Id, @GeneratedValue | 主键，自增 |
| title | String | @Column(nullable=false, length=200) | 书名 |
| author | String | @Column(nullable=false, length=100) | 作者 |
| isbn | String | @Column(unique=true, length=20) | ISBN |
| publisher | String | 可选 | 出版社 |
| publishDate | LocalDateTime | 可选 | 出版日期 |
| category | String | 可选 | 分类 |
| description | String | 可选 | 描述 |
| stock | Integer | @Column(nullable=false) | 库存数量 |
| available | Integer | @Column(nullable=false) | 可借数量 |
| createTime | LocalDateTime | @PrePersist 自动填充 | 创建时间 |

### 5.3 BorrowRecord 实体

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | Long | @Id, @GeneratedValue | 主键，自增 |
| bookId | Long | @Column(nullable=false) | 图书ID |
| userId | Long | @Column(nullable=false) | 用户ID |
| borrowTime | LocalDateTime | @Column(nullable=false) | 借阅时间 |
| dueTime | LocalDateTime | @Column(nullable=false) | 应还时间 |
| returnTime | LocalDateTime | 可选 | 归还时间 |
| renewCount | Integer | @Column(nullable=false) | 续借次数 |
| status | Borrowstatus | @Enumerated(EnumType.STRING) | 状态（BORROWED/RETURNED/OVERDUE） |

---

## 6. 部署与运行

### 6.1 前置依赖

| 依赖 | 版本 | 端口 | 说明 |
|------|------|------|------|
| Nacos Server | 2.x | 8848 | 服务注册与配置中心（仅服务端） |
| MySQL | 8.x | 3306 | 数据库 |
| Node.js | >= 18.x | - | 前端运行环境 |

### 6.2 数据库配置

创建数据库：
```sql
CREATE DATABASE nacon7_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

连接信息：
- 数据库名：`nacon7_db`
- 用户名：`root`
- 密码：`123456`
- 连接URL：`jdbc:mysql://localhost:3306/nacon7_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true`

### 6.3 启动顺序

1. **启动 Nacos Server**（仅服务端需要）
   ```bash
   cd nacos/bin
   # Linux/Mac
   sh startup.sh -m standalone
   # Windows
   startup.cmd -m standalone
   ```

2. **启动 MySQL**
   - 确保 MySQL 服务已启动，数据库 `nacon7_db` 已创建

3. **启动后端服务**
   ```bash
   cd nacon7
   mvn spring-boot:run -pl server
   ```
   服务地址：`http://localhost:8081`

4. **启动前端服务**
   ```bash
   cd nacon7
   npm install
   npm run dev
   ```
   服务地址：`http://localhost:5173`

### 6.4 初始化账号

项目启动时自动创建测试账号：

| 用户名 | 密码 | 角色 | 手机号 |
|--------|------|------|--------|
| admin | admin123 | ADMIN | 13800138000 |
| user | user123 | USER | 13800138001 |

### 6.5 前端页面说明

| 页面 | 路径 | 功能 | 说明 |
|------|------|------|------|
| **登录页面** | /login | 账号/密码输入、登录、注册跳转 | Enter键提交、异步请求、错误提示 |
| **注册页面** | /register | 用户名/密码/手机号输入、注册、返回登录 | 表单校验、密码确认 |
| **图书浏览** | / | 分页浏览图书、按书名/作者/分类搜索、借阅 | 卡片式布局、借阅弹窗 |
| **我的借阅** | /my-borrow | 查看个人借阅记录、归还、续借 | 表格展示、状态标签、续借限制 |
| **个人信息** | /profile | 显示用户详细信息、刷新 | 用户头像、角色标签 |
| **图书管理** | /book-manage | 图书CRUD、搜索、分页管理 | 仅管理员可见，弹窗编辑 |
| **用户管理** | /user-manage | 查看用户列表、删除用户 | 仅管理员可见 |
| **借阅管理** | /borrow-manage | 查看全部/逾期借阅记录（分页） | 仅管理员可见，模式切换 |

---

## 7. 常用命令

```bash
# 后端编译
mvn clean compile

# 启动后端
mvn spring-boot:run -pl server

# 前端安装依赖
npm install

# 启动前端开发服务器
npm run dev

# 前端构建生产版本
npm run build

# 前端预览生产版本
npm run preview
```

---

## 8. 开发规范

### 8.1 模块依赖规则

- **controller** 类必须放在 `server` 模块，不能放在 `common` 模块
- **common** 模块只能包含 entities、DTOs 和 utility classes
- **server** 模块必须使用 `${project.version}` 依赖 `common` 模块

### 8.2 前端开发规范

- 组件文件命名使用 PascalCase（如 `BookListView.vue`）
- 工具函数放在 `src/utils/` 目录
- API 服务放在 `src/services/` 目录
- 路由配置在 `src/router/index.js`

### 8.3 新增功能开发规范

**后端：**
1. Entity（实体类）→ common 模块
2. DTO（数据传输对象）→ common 模块
3. Repository（数据访问层）→ server 模块
4. Service（业务逻辑接口）→ server 模块
5. ServiceImpl（业务逻辑实现）→ server 模块
6. Controller（REST API）→ server 模块

**前端：**
1. Service（API 调用）→ src/services/
2. View（页面组件）→ src/views/
3. Route（路由配置）→ src/router/index.js
