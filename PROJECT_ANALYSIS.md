# 健身房会员管理系统 - 项目结构详细分析文档

## 1. 项目概述

### 1.1 项目简介
本项目是一个基于前后端分离架构的健身房会员管理系统（FitManage），旨在为健身房提供全面、高效和用户友好的会员管理解决方案。系统采用现代化的开发技术栈，支持会员管理、课程预订、费用结算、日志记录等核心功能。

### 1.2 技术栈

#### 后端技术栈
- **开发语言**: Java 17
- **框架**: Spring Boot 2.7.9
- **数据库**: MySQL
- **ORM框架**: MyBatis
- **数据库连接池**: Druid
- **分页插件**: PageHelper
- **安全认证**: JWT (Json Web Token)
- **对象存储**: 阿里云 OSS
- **文档工具**: Knife4j (Swagger增强版)
- **其他依赖**:
  - FastJSON (JSON处理)
  - Lombok (简化代码)
  - POI (Excel处理)
  - AspectJ (AOP切面编程)

#### 前端技术栈
- **开发框架**: Vue 3.2.13
- **开发语言**: TypeScript 4.5.5
- **UI组件库**: Element Plus 2.4.2
- **状态管理**: Pinia 2.1.7 (替代Vuex)
- **路由管理**: Vue Router 4.0.3
- **HTTP客户端**: Axios 1.6.2
- **构建工具**: Vue CLI 5.0.0
- **持久化插件**: pinia-plugin-persistedstate 3.2.0
- **其他**: js-cookie, sass-loader, stylus

### 1.3 项目特点
- 前后端完全分离，职责清晰
- 采用JWT实现无状态认证
- 使用AOP实现操作日志记录
- 集成阿里云OSS实现文件存储
- 支持多角色权限管理（管理员、会员）
- 使用MyBatis实现灵活的数据访问

---

## 2. 项目架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端层 (gym-web)                     │
│  Vue 3 + TypeScript + Element Plus + Pinia + Vue Router │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP/REST API
                      │ JWT Token
┌─────────────────────┴───────────────────────────────────┐
│                   控制器层 (gym-server)                  │
│         AdminController / MemberController              │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                   业务服务层 (gym-server)                │
│         UserService / CoachService / CourseService...    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                  数据访问层 (gym-server)                  │
│             Mapper接口 + MyBatis XML映射                 │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                   持久化层 (MySQL)                       │
│        gym_users / gym_courses / gym_coachs...          │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

项目采用Maven多模块架构，共包含4个主要模块：

```
gym-management (父模块)
├── gym-common      # 公共模块
├── gym-pojo        # 实体类模块
├── gym-server      # 后端服务模块
└── gym-web         # 前端项目模块
```

---

## 3. 模块详细分析

### 3.1 gym-common 模块（公共模块）

#### 目录结构
```
gym-common/
├── src/main/java/com/sjdddd/
│   ├── constant/          # 常量定义
│   │   ├── AutoFillConstant.java
│   │   ├── JwtClaimsConstant.java
│   │   └── MessageConstant.java
│   ├── context/           # 上下文管理
│   │   └── BaseContext.java
│   ├── enumeration/       # 枚举类
│   │   └── OperationType.java
│   ├── exception/         # 异常类
│   │   └── BaseException.java
│   ├── json/             # JSON配置
│   │   └── JacksonObjectMapper.java
│   ├── properties/       # 配置属性
│   │   ├── AliOssProperties.java
│   │   └── JwtProperties.java
│   ├── result/           # 统一返回结果
│   │   ├── PageResult.java
│   │   └── Result.java
│   ├── sql/              # SQL脚本
│   │   └── gym_management_init.sql
│   └── utils/            # 工具类
│       ├── AliOssUtil.java
│       └── JwtUtil.java
└── pom.xml
```

#### 核心组件说明

**1. 常量类**
- `MessageConstant.java`: 定义所有业务异常提示信息
- `JwtClaimsConstant.java`: JWT声明常量
- `AutoFillConstant.java`: 自动填充字段常量

**2. 工具类**
- `JwtUtil.java`: JWT令牌生成和解析工具
- `AliOssUtil.java`: 阿里云OSS文件上传工具

**3. 统一结果封装**
- `Result.java`: 统一的API响应格式
  ```java
  {
    "code": 1,           // 响应状态码
    "msg": "success",    // 响应消息
    "data": {...}        // 响应数据
  }
  ```
- `PageResult.java`: 分页查询结果封装

**4. 数据库初始化脚本**
- 包含所有表的建表语句和初始数据

---

### 3.2 gym-pojo 模块（实体类模块）

#### 目录结构
```
gym-pojo/
├── src/main/java/com/sjdddd/
│   ├── dto/               # 数据传输对象
│   │   ├── CoachAddDTO.java
│   │   ├── CoachEditDTO.java
│   │   ├── CourseAddDTO.java
│   │   ├── CourseEditDTO.java
│   │   ├── CourseWithEnrollmentStatusDTO.java
│   │   ├── MemberAddDTO.java
│   │   ├── MemberEditDTO.java
│   │   ├── OrderListDTO.java
│   │   ├── UserInfoDTO.java
│   │   ├── UserLoginDTO.java
│   │   └── UserRegisterDTO.java
│   ├── entity/            # 实体类
│   │   ├── Bill.java
│   │   ├── Booking.java
│   │   ├── Coach.java
│   │   ├── Course.java
│   │   ├── Log.java
│   │   ├── MemberCard.java
│   │   ├── Order.java
│   │   ├── Payment.java
│   │   └── User.java
│   └── vo/                # 视图对象
│       ├── CoachAddVO.java
│       ├── CoachEditVO.java
│       ├── CourseAddVO.java
│       ├── CourseEditVO.java
│       ├── MemberAddVO.java
│       ├── MemberEditVO.java
│       ├── OrderListVO.java
│       ├── UserLoginVO.java
│       └── UserRegisterVO.java
└── pom.xml
```

#### 核心实体说明

**1. User（用户实体）**
```java
- userId: Long          // 用户ID
- userName: String      // 用户名
- password: String      // 密码（MD5加密）
- userRealName: String  // 真实姓名
- userPhone: String     // 手机号
- dateBirth: Date       // 出生日期
- userType: String      // 用户类型（1-管理员，2-会员）
- avatar: String        // 头像URL
- sex: String          // 性别（0-男，1-女，2-未知）
- createTime: LocalDateTime
- updateTime: LocalDateTime
```

**2. Coach（教练实体）**
```java
- coachId: Long         // 教练ID
- coachSex: String      // 性别
- coachRealName: String // 教练姓名
- coachPhone: String    // 联系电话
- coachRemark: String   // 专长/备注
- createTime: LocalDateTime
- updateTime: LocalDateTime
```

**3. Course（课程实体）**
```java
- courseId: Long          // 课程ID
- courseName: String      // 课程名称
- coachId: Long           // 教练ID
- scheduleStart: DateTime // 开始时间
- scheduleEnd: DateTime   // 结束时间
- courseFee: BigDecimal   // 课程费用
- isEnrolled: String      // 是否已预订（0-未，1-已）
- createTime: LocalDateTime
- updateTime: LocalDateTime
```

**4. MemberCard（会员卡实体）**
```java
- memberCardId: Long      // 会员卡ID
- userId: Long            // 用户ID
- avatar: String          // 头像
- memberFee: BigDecimal   // 余额
- memberCardStatus: String // 状态（0-未激活，1-已激活，2-已过期）
- activateTime: Date       // 激活日期
- expireTime: Date         // 过期日期
- createTime: LocalDateTime
- updateTime: LocalDateTime
```

**5. Booking（预订实体）**
```java
- bookingId: Long              // 预订ID
- userId: Long                 // 用户ID
- courseId: Long               // 课程ID
- bookingDate: DateTime        // 预订时间
- isEnrolledByCurrentUser: String // 是否当前用户预订
```

**6. Payment（支付实体）**
```java
- paymentId: Long        // 支付ID
- bookingId: Long        // 预订ID
- userId: Long           // 用户ID
- amount: BigDecimal     // 金额
- paymentDate: DateTime  // 支付时间
- paymentType: String    // 支付方式
- paymentStatus: String  // 支付状态（0-未，1-已）
```

**7. Log（日志实体）**
```java
- logId: Long            // 日志ID
- userId: Long           // 用户ID
- actionType: String     // 操作类型
- actionDate: DateTime   // 操作时间
- requestMethod: String  // 请求方法
- requestData: String     // 请求参数
- responseData: String    // 返回结果
- exceptionName: String   // 异常名称
- exceptionMessage: String // 异常信息
- userName: String       // 用户名
- requestUri: String      // 请求URI
- requestIp: String       // 请求IP
```

---

### 3.3 gym-server 模块（后端服务模块）

#### 目录结构
```
gym-server/
├── src/main/java/com/sjdddd/
│   ├── GymManagementApplication.java  # 启动类
│   ├── annotation/                    # 自定义注解
│   │   ├── AutoFill.java             # 自动填充注解
│   │   └── OperationLog.java         # 操作日志注解
│   ├── aspect/                        # AOP切面
│   │   ├── AutoFillAspect.java       # 自动填充切面
│   │   └── OperationLogAspect.java   # 操作日志切面
│   ├── config/                        # 配置类
│   │   ├── OssConfiguration.java     # OSS配置
│   │   └── WebMvcConfiguration.java  # Web MVC配置
│   ├── handler/                       # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── interceptor/                   # 拦截器
│   │   └── JwtTokenAdminInterceptor.java
│   ├── controller/                    # 控制器
│   │   ├── admin/                    # 管理端控制器
│   │   │   ├── CoachController.java
│   │   │   ├── CommonController.java
│   │   │   ├── CourseController.java
│   │   │   ├── LogController.java
│   │   │   ├── MemberController.java
│   │   │   ├── OrderController.java
│   │   │   └── UserController.java
│   │   └── member/                   # 会员端控制器
│   │       └── MemberPaymentController.java
│   ├── mapper/                        # MyBatis Mapper接口
│   │   ├── BookingMapper.java
│   │   ├── CoachMapper.java
│   │   ├── CourseMapper.java
│   │   ├── LogMapper.java
│   │   ├── MemberCardMapper.java
│   │   ├── OrderMapper.java
│   │   ├── PaymentMapper.java
│   │   └── UserMapper.java
│   └── service/                       # 业务服务层
│       ├── impl/                     # 服务实现
│       │   ├── CoachServiceImpl.java
│       │   ├── CourseServiceImpl.java
│       │   ├── MemberServiceImpl.java
│       │   ├── OperateLogsServiceImpl.java
│       │   ├── OrderServiceImpl.java
│       │   ├── PaymentServiceImpl.java
│       │   └── UserServiceImpl.java
│       ├── CoachService.java
│       ├── CourseService.java
│       ├── MemberService.java
│       ├── OperateLogsService.java
│       ├── OrderService.java
│       ├── PaymentService.java
│       └── UserService.java
└── src/main/resources/
    ├── application.yml               # 主配置文件
    ├── application-dev.yml          # 开发环境配置
    └── mapper/                       # MyBatis XML映射文件
        ├── BookingMapper.xml
        ├── CoachMapper.xml
        ├── CourseMapper.xml
        ├── LogMapper.xml
        ├── MemberCardMapper.xml
        ├── OrderMapper.xml
        ├── PaymentMapper.xml
        └── UserMapper.xml
```

#### 核心功能实现

**1. 认证与授权**
- 使用JWT实现无状态认证
- 拦截器验证Token有效性
- 支持用户类型（管理员/会员）权限控制

**2. AOP切面编程**
- `OperationLogAspect`: 记录所有带@OperationLog注解的操作日志
- `AutoFillAspect`: 自动填充createTime、updateTime等公共字段

**3. 统一异常处理**
- `GlobalExceptionHandler`: 捕获并处理所有异常，统一返回格式

**4. 文件上传**
- 集成阿里云OSS实现头像等文件上传
- 支持文件类型和大小校验

**5. 事务管理**
- 使用Spring事务管理器
- 在支付、预订等涉及多表操作的方法上添加@Transactional注解

---

### 3.4 gym-web 模块（前端项目模块）

#### 目录结构
```
gym-web/
├── public/
├── src/
│   ├── apis/                    # API接口定义
│   │   ├── bill.ts
│   │   ├── coach.ts
│   │   ├── course.ts
│   │   ├── log.ts
│   │   ├── member.ts
│   │   ├── order.ts
│   │   └── user.ts
│   ├── assets/                  # 静态资源
│   │   └── logoc.png
│   ├── router/                  # 路由配置
│   │   └── index.ts
│   ├── stores/                  # Pinia状态管理
│   │   ├── index.ts
│   │   └── modules/
│   │       ├── member.ts
│   │       └── user.ts
│   ├── utils/                   # 工具函数
│   │   └── request.ts          # Axios封装
│   ├── views/                   # 页面组件
│   │   ├── coach/
│   │   │   └── CoachList.vue
│   │   ├── course/
│   │   │   ├── CourseList.vue      # 管理端课程列表
│   │   │   └── member/
│   │   │       └── CourseList.vue  # 会员端课程列表
│   │   ├── layout/
│   │   │   ├── AdminLayoutContainer.vue  # 管理端布局
│   │   │   └── MemberLayoutContainer.vue # 会员端布局
│   │   ├── log/
│   │   │   └── LogList.vue
│   │   ├── login/
│   │   │   └── LoginPage.vue
│   │   ├── member/
│   │   │   └── MemberList.vue
│   │   ├── order/
│   │   │   └── OrderList.vue
│   │   ├── payment/
│   │   │   └── PaymentManage.vue
│   │   └── user/
│   │       ├── UserProfile.vue
│   │       ├── UserAvatar.vue
│   │       ├── UserPassword.vue
│   │       └── member/
│   │           ├── UserProfile.vue
│   │           ├── UserAvatar.vue
│   │           └── UserPassword.vue
│   ├── App.vue                 # 根组件
│   ├── main.ts                 # 入口文件
│   └── shims-vue.d.ts          # TypeScript声明文件
├── .gitignore
├── package.json
├── tsconfig.json
└── vue.config.js
```

#### 前端架构特点

**1. 路由设计**
- 基于用户类型（userType）进行路由守卫
- 管理员路由：`/adminDashboard/*`
- 会员路由：`/memberDashboard/*`
- 使用路由懒加载优化性能

**2. 状态管理**
- 使用Pinia替代Vuex
- `user.ts`: 管理用户状态（token, userType, userInfo）
- `member.ts`: 管理会员相关状态
- 支持状态持久化到localStorage

**3. HTTP请求封装**
- Axios实例统一配置
- 请求/响应拦截器
- 自动添加JWT Token到请求头
- 统一错误处理

**4. 页面组织**
- 管理端和会员端独立布局组件
- 通用组件（用户信息、头像、密码）在user目录下复用
- 使用Element Plus组件库构建UI

---

## 4. 数据库设计

### 4.1 数据库表结构

#### 1. gym_users（用户表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| user_id | BIGINT(20) | 用户ID | 主键, 自增 |
| user_name | VARCHAR(12) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(64) | 密码 | NOT NULL |
| user_real_name | VARCHAR(10) | 真实姓名 | - |
| user_phone | VARCHAR(11) | 手机号 | - |
| date_birth | DATE | 出生日期 | - |
| user_type | VARCHAR(1) | 用户类型 | 默认'1'（1-管理员，2-会员） |
| avatar | VARCHAR(255) | 头像URL | - |
| sex | CHAR(1) | 性别 | 默认'0'（0-男，1-女，2-未知） |
| create_time | DATETIME | 创建时间 | - |
| update_time | DATETIME | 更新时间 | - |

#### 2. gym_member_cards（会员卡表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| member_card_id | BIGINT(20) | 会员卡ID | 主键, 自增 |
| user_id | BIGINT(20) | 用户ID | NOT NULL, 外键 |
| avatar | VARCHAR(100) | 头像 | - |
| member_fee | DECIMAL(10,2) | 余额 | NOT NULL, 默认0.00 |
| member_card_status | CHAR(1) | 状态 | 默认'0'（0-未激活，1-已激活，2-已过期） |
| activate_time | DATE | 激活日期 | - |
| expire_time | DATE | 过期日期 | - |
| create_time | DATETIME | 创建时间 | - |
| update_time | DATETIME | 更新时间 | - |

#### 3. gym_coachs（教练表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| coach_id | BIGINT(20) | 教练ID | 主键, 自增 |
| coach_sex | CHAR(1) | 性别 | 默认'0'（0-男，1-女，2-未知） |
| coach_real_name | VARCHAR(10) | 教练姓名 | NOT NULL |
| coach_phone | VARCHAR(11) | 手机号 | - |
| coach_remark | VARCHAR(500) | 专长/备注 | - |
| create_time | DATETIME | 创建时间 | - |
| update_time | DATETIME | 更新时间 | - |

#### 4. gym_courses（课程表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| course_id | BIGINT(20) | 课程ID | 主键, 自增 |
| course_name | VARCHAR(100) | 课程名称 | NOT NULL |
| coach_id | BIGINT(20) | 教练ID | NOT NULL, 外键 |
| schedule_start | DATETIME | 开始时间 | NOT NULL |
| schedule_end | DATETIME | 结束时间 | NOT NULL |
| course_fee | DECIMAL(10,2) | 课程费用 | NOT NULL, 默认0.00 |
| isEnrolled | CHAR(1) | 是否已预订 | 默认'0'（0-未，1-已） |
| create_time | DATETIME | 创建时间 | - |
| update_time | DATETIME | 更新时间 | - |

#### 5. gym_booking（预订表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| booking_id | BIGINT(20) | 预订ID | 主键, 自增 |
| user_id | BIGINT(20) | 用户ID | NOT NULL, 外键 |
| course_id | BIGINT(20) | 课程ID | NOT NULL, 外键 |
| booking_date | DATETIME | 预订时间 | - |
| isEnrolledByCurrentUser | CHAR(1) | 是否当前用户预订 | 默认'0' |

#### 6. gym_payments（支付表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| payment_id | BIGINT(20) | 支付ID | 主键, 自增 |
| booking_id | BIGINT(20) | 预订ID | NOT NULL, 外键 |
| user_id | BIGINT(20) | 用户ID | NOT NULL, 外键 |
| amount | DECIMAL(10,2) | 支付金额 | NOT NULL, 默认0.00 |
| payment_date | DATETIME | 支付时间 | - |
| payment_type | VARCHAR(20) | 支付方式 | - |
| payment_status | CHAR(1) | 支付状态 | 默认'0'（0-未支付，1-已支付） |

#### 7. gym_logs（日志表）
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| log_id | BIGINT(20) | 日志ID | 主键, 自增 |
| user_id | BIGINT(20) | 用户ID | 外键 |
| action_type | VARCHAR(255) | 操作类型 | - |
| action_date | DATETIME | 操作时间 | - |
| request_method | VARCHAR(255) | 请求方法 | - |
| request_data | TEXT | 请求参数 | - |
| response_data | TEXT | 返回结果 | - |
| exception_name | VARCHAR(255) | 异常名称 | - |
| exception_message | TEXT | 异常信息 | - |
| user_name | VARCHAR(255) | 用户名 | - |
| request_uri | VARCHAR(255) | 请求URI | - |
| request_ip | VARCHAR(255) | 请求IP | - |

### 4.2 数据库设计特点

1. **使用逻辑外键**: 不使用数据库物理外键约束，在应用层维护数据一致性
2. **统一命名规范**: 所有表以`gym_`前缀，字段使用下划线命名
3. **自动时间戳**: create_time和update_time记录数据的创建和更新时间
4. **状态字段**: 使用字符类型表示各种状态，便于扩展
5. **软删除**: 虽然当前实现使用物理删除，但表结构支持软删除扩展

---

## 5. 核心业务流程

### 5.1 用户登录流程

```
1. 用户输入用户名和密码
   ↓
2. 前端验证表单数据
   ↓
3. 发送POST请求到 /user/login
   ↓
4. 后端UserMapper查询用户
   ↓
5. 校验密码（MD5加密后比对）
   ↓
6. 生成JWT Token（包含userId）
   ↓
7. 返回Token和userType给前端
   ↓
8. 前端将Token存储到Pinia和localStorage
   ↓
9. 根据userType跳转到不同页面
```

### 5.2 课程预订流程

```
1. 会员浏览课程列表
   ↓
2. 选择课程点击"报名"
   ↓
3. 前端发送报名请求
   ↓
4. 后端校验：
   - 用户登录状态
   - 课程是否已被其他用户预订
   - 用户余额是否足够
   ↓
5. 开启事务
   ↓
6. 执行操作：
   - 扣除会员余额
   - 插入预订记录
   - 插入支付记录
   - 更新课程状态
   ↓
7. 提交事务
   ↓
8. 记录操作日志
   ↓
9. 返回成功结果
```

### 5.3 文件上传流程

```
1. 用户选择头像文件
   ↓
2. 前端校验文件类型和大小
   ↓
3. 发送multipart/form-data请求
   ↓
4. 后端接收文件
   ↓
5. 生成UUID文件名
   ↓
6. 调用AliOssUtil上传到OSS
   ↓
7. 获取文件URL
   ↓
8. 更新用户表的avatar字段
   ↓
9. 返回URL给前端
```

---

## 6. 关键技术实现

### 6.1 JWT认证机制

**Token生成**:
```java
Map<String, Object> claims = new HashMap<>();
claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
String token = JwtUtil.createJWT(
    jwtProperties.getUserSecretKey(),
    jwtProperties.getUserTtl(),
    claims);
```

**Token验证拦截器**:
```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String token = request.getHeader(jwtProperties.getUserTokenName());
    // 解析Token，验证有效性
    Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
    Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
    BaseContext.setCurrentId(userId);
    return true;
}
```

### 6.2 AOP操作日志

**自定义注解**:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String operDesc() default "";  // 操作描述
}
```

**切面实现**:
```java
@AfterReturning(value = "operLogPoinCut()", returning = "keys")
public void saveOperLog(JoinPoint joinPoint, Object keys) {
    // 获取请求信息
    HttpServletRequest request = ...;
    // 获取方法注解
    OperationLog opLog = method.getAnnotation(OperationLog.class);
    // 构建日志对象
    Log operlog = new Log();
    operlog.setActionType(opLog.operDesc());
    operlog.setRequestData(JSON.toJSONString(params));
    operlog.setResponseData(JSON.toJSONString(keys));
    // 保存日志
    operatelogsService.save(operlog);
}
```

### 6.3 前端Axios封装

**请求拦截器**:
```typescript
axios.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['token'] = userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)
```

**响应拦截器**:
```typescript
axios.interceptors.response.use(
  response => {
    if (response.data.code === 0) {
      ElMessage.error(response.data.msg || '请求失败')
    }
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## 7. 项目部署

### 7.1 后端部署

1. **环境要求**:
   - JDK 17
   - MySQL 8.0+
   - Maven 3.x

2. **配置修改**:
   - 修改`application-dev.yml`中的数据库连接信息
   - 配置阿里云OSS的AccessKey和SecretKey

3. **打包部署**:
   ```bash
   mvn clean package
   java -jar gym-server/target/gym-server-1.0.0-SNAPSHOT.jar
   ```

### 7.2 前端部署

1. **环境要求**:
   - Node.js 14+
   - npm 或 yarn

2. **打包**:
   ```bash
   npm install
   npm run build
   ```

3. **部署**:
   - 将`dist`目录部署到Nginx或其他Web服务器
   - 配置反向代理到后端API

---

## 8. 项目亮点与特色

### 8.1 技术亮点

1. **前后端分离架构**: 清晰的职责划分，便于开发和维护
2. **JWT无状态认证**: 无需服务端存储Session，支持水平扩展
3. **AOP切面编程**: 非侵入式实现操作日志和自动填充
4. **逻辑外键**: 提高数据库灵活性，降低复杂度
5. **模块化设计**: Maven多模块，代码组织清晰
6. **TypeScript**: 前端使用TS，提供类型安全
7. **Pinia状态管理**: 现代化的状态管理方案，支持持久化
8. **阿里云OSS**: 分布式文件存储，提高可靠性

### 8.2 业务特色

1. **多角色权限**: 支持管理员和会员两种角色，不同界面和权限
2. **课程预订**: 完整的课程预订流程，包含余额校验、状态管理
3. **智能搜索**: 教练姓名模糊搜索，支持拼音匹配
4. **操作审计**: 完整的日志记录，记录所有关键操作
5. **头像管理**: 支持头像上传和更换，使用OSS存储
6. **数据安全**: 密码MD5加密，Token认证，请求参数校验

---

## 9. 未来改进方向

1. **功能扩展**:
   - 增加课程评价功能
   - 支持会员卡类型和套餐管理
   - 增加数据统计和报表功能

2. **性能优化**:
   - 添加Redis缓存
   - 数据库查询优化
   - 前端懒加载和代码分割

3. **安全加固**:
   - 添加接口限流
   - 密码强度验证
   - SQL注入防护
   - XSS攻击防护

4. **用户体验**:
   - 添加移动端适配
   - 优化页面加载速度
   - 增加更多交互动画

5. **运维监控**:
   - 集成日志监控系统
   - 添加健康检查接口
   - 性能指标监控

---

## 10. 总结

本健身房会员管理系统采用前后端分离的现代化架构，技术栈成熟稳定，代码结构清晰规范。系统实现了用户管理、会员管理、课程管理、预订管理、支付管理和日志记录等核心功能，满足了健身房日常运营的基本需求。

通过使用Spring Boot + MyBatis + JWT + Vue 3 + Pinia等技术组合，系统具有良好的可扩展性和可维护性。模块化的设计使得各功能模块职责明确，便于团队协作开发和后期维护。

文档编写时间: 2026年2月10日
项目版本: 1.0.0-SNAPSHOT
