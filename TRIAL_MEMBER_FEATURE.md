# 体验会员功能说明文档

## 功能概述

新增"体验会员"类型，允许会员免费享受三次指定课程。

## 实现细节

### 1. 数据库变更

数据库表结构已更新：`gym-common/src/main/java/com/sjdddd/sql/gym_management_init.sql`

`gym_member_cards` 表新增以下字段：
- `member_type` VARCHAR(2) DEFAULT '1' - 会员类型：1-普通会员，2-体验会员
- `free_quota_remaining` INT DEFAULT 0 - 体验会员剩余免费次数
- `free_course_ids` VARCHAR(255) DEFAULT NULL - 可免费享受的课程ID列表（逗号分隔）

测试数据说明：
- 用户ID 102 已设置为体验会员，有3次免费额度，可免费课程ID为 101,102,103
- 用户ID 101 和 103 为普通会员，无免费额度

### 2. 核心文件变更

#### 2.1 新增文件
- `gym-common/src/main/java/com/sjdddd/enumeration/MemberType.java` - 会员类型枚举

#### 2.2 修改的实体类
- `gym-pojo/src/main/java/com/sjdddd/entity/MemberCard.java`
  - 添加 `memberType` 字段
  - 添加 `freeQuotaRemaining` 字段
  - 添加 `freeCourseIds` 字段

#### 2.3 修改的 DTO
- `gym-pojo/src/main/java/com/sjdddd/dto/MemberAddDTO.java`
  - 添加 `memberType` 字段
  - 添加 `freeCourseIds` 字段

- `gym-pojo/src/main/java/com/sjdddd/dto/MemberEditDTO.java`
  - 添加 `memberType` 字段
  - 添加 `freeCourseIds` 字段

#### 2.4 修改的 Service
- `gym-server/src/main/java/com/sjdddd/service/impl/MemberServiceImpl.java`
  - `add()` 方法：添加体验会员时自动设置3次免费次数
  - `edit()` 方法：支持会员类型切换和免费课程设置

- `gym-server/src/main/java/com/sjdddd/service/impl/OrderServiceImpl.java`
  - `createOrder()` 方法：实现免费预订逻辑
    - 检查是否为体验会员
    - 检查课程是否在可免费列表中
    - 检查剩余免费次数
    - 免费预订不扣余额，只扣减免费次数

#### 2.5 修改的 Mapper
- `gym-server/src/main/java/com/sjdddd/mapper/MemberCardMapper.java`
  - 添加 `selectByMemberCardId()` 方法
  - 添加 `updateFreeQuotaByUserId()` 方法

- `gym-server/src/main/resources/mapper/MemberCardMapper.xml`
  - 更新 `BaseResultMap` 映射
  - 更新 `Base_Column_List`
  - 更新 `insert` 和 `insertSelective` 语句
  - 更新 `updateByPrimaryKeySelective` 语句
  - 添加 `selectByMemberCardId` 查询
  - 添加 `updateFreeQuotaByUserId` 更新

## 使用说明

### 1. 创建体验会员

**调用接口**：`POST /member/add`

**请求示例**：
```json
{
  "userName": "trialuser",
  "userRealName": "体验会员张三",
  "userPhone": "13800138000",
  "sex": "0",
  "memberFee": 100.00,
  "memberCardStatus": "1",
  "memberType": "2",
  "freeCourseIds": "101,102,103"
}
```

**参数说明**：
- `memberType`: "2" 表示体验会员
- `freeCourseIds`: 可免费享受的课程ID列表，用逗号分隔

### 2. 编辑体验会员

**调用接口**：`PUT /member/edit`

**请求示例**：
```json
{
  "userId": 123,
  "userName": "trialuser",
  "userRealName": "张三",
  "userPhone": "13800138000",
  "sex": "0",
  "memberFee": 100.00,
  "memberCardStatus": "1",
  "memberType": "2",
  "freeCourseIds": "101,102,103,104"
}
```

### 3. 创建订单（免费预订）

**调用接口**：`POST /order/create`

**请求示例**：
```json
{
  "userId": 123,
  "courseId": 101,
  "coachId": 456
}
```

**处理逻辑**：
1. 系统检查该会员是否为体验会员（`memberType = "2"`）
2. 检查该课程是否在可免费课程列表中（`freeCourseIds`）
3. 检查剩余免费次数是否大于 0（`freeQuotaRemaining > 0`）
4. 如果满足上述条件，则免费预订：
   - 不扣除会员余额
   - 扣减剩余免费次数
   - 支付金额为 0
   - 支付类型标记为"体验会员免费预订"
5. 否则按正常流程扣除会员余额

## 业务规则

### 免费预订条件
1. 会员类型必须为体验会员（`memberType = "2"`）
2. 课程ID必须在可免费课程列表中（`freeCourseIds`）
3. 剩余免费次数必须大于 0（`freeQuotaRemaining > 0`）
4. 该会员未预订过此课程

### 免费次数管理
- 创建体验会员时，默认免费次数为 3
- 每次免费预订成功后，免费次数减 1
- 免费次数用完后，体验会员仍可正常预订课程，但需要支付费用
- 支持后续编辑增加免费次数（如续费）

### 会员类型切换
- 支持从普通会员切换为体验会员（自动初始化3次免费次数）
- 支持从体验会员切换为普通会员（免费次数保留，但不再生效）

## 预期效果

### 1. 体验会员预订指定课程
- 订单创建成功
- 会员余额不变
- 剩余免费次数 -1
- 支付金额显示为 0
- 支付类型为"体验会员免费预订"

### 2. 体验会员预订非指定课程
- 订单创建成功
- 正常扣除会员余额
- 免费次数不变
- 支付金额显示课程原价

### 3. 免费次数用完
- 订单创建成功
- 正常扣除会员余额
- 免费次数保持为 0

## 注意事项

1. **免费课程ID格式**：必须使用逗号分隔的字符串，如 `"101,102,103"`
2. **免费次数初始化**：创建体验会员时自动设置为 3，可后续修改
3. **重复预订检查**：系统会检查会员是否已预订该课程，防止重复预订
4. **余额不足检查**：只有非免费预订才检查余额是否充足

## 扩展建议

1. **免费次数续费**：可以添加充值接口，为体验会员增加免费次数
2. **免费课程动态管理**：可以开发单独的接口管理体验会员的可免费课程列表
3. **会员等级体系**：可以扩展更多会员类型（VIP、钻石等），每类有不同权益
4. **到期时间**：可以为体验会员添加有效期限制
5. **体验券**：可以开发体验券功能，体验会员可以通过体验券获得额外免费次数
