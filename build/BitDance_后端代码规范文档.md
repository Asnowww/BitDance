# BitDance 后端代码规范文档

版本：V1.1  
技术栈：Spring Boot + Java 21 + PostgreSQL  
适用范围：BitDance 后端开发、代码评审、接口联调、数据库迁移与后续维护。

## 1. 规范目标

本文档用于统一 BitDance 后端项目的代码风格、目录结构、接口设计、数据库使用和团队协作方式。

后端开发必须遵循：

1. 业务边界清晰：按账号权限、舞室课程、评价、约练、成长档案、商家运营等领域组织代码。
2. 接口语义稳定：REST API 命名清晰，返回结构统一，错误码可追踪。
3. 数据一致可靠：多表写入、状态流转、支付回调、名额扣减必须显式处理事务和幂等。
4. 安全默认开启：鉴权、权限、参数校验、敏感数据保护必须内建到开发流程。
5. 与 schema 对齐：命名、枚举、状态、字段类型必须以 `bitdance_postgresql_schema.sql` 为准。

## 2. 项目结构规范

MVP 阶段采用模块化单体，代码包名可简化，但必须能映射到架构文档中的业务模块。

```text
src/main/java/com/bitdance
  ├── BitDanceApplication.java
  ├── common
  │   ├── config
  │   ├── exception
  │   ├── response
  │   ├── security
  │   ├── validation
  │   ├── audit
  │   └── util
  ├── iam
  ├── profile
  ├── catalog
  ├── review
  ├── practice
  ├── growth
  ├── content
  ├── workshop
  ├── merchant
  ├── settlement
  ├── governance
  └── message

src/main/resources
  ├── application.yml
  ├── application-dev.yml
  ├── application-prod.yml
  └── db/migration
```

模块映射：

| Java 包 | 架构模块 | 说明 |
| --- | --- | --- |
| `iam` | `iam-auth` | 登录、Token、角色、权限、数据权限 |
| `profile` | `user-profile` | 用户资料、偏好、隐私、设备、安全 |
| `catalog` | `studio-catalog` | 舞室、课程、教练、课表、收藏、试听 |
| `review` | `review-center`、`review-risk` | 评价、维度、权重、风控、申诉 |
| `practice` | `practice-social` | 约练、响应、搭子、拼课意向 |
| `growth` | `growth-archive` | 打卡、作品、目标、徽章、时间线 |
| `content` | `content-community` | 动态、评论、点赞、话题、关注 |
| `workshop` | `workshop-consumer` | Workshop 浏览、订单、签到、日历 |
| `merchant` | `merchant-ops` | 舞室入驻、课程创建侧、教练关系 |
| `settlement` | `billing-settlement` | 分账规则、账单、提现 |
| `governance` | `governance-center` | 审核、举报、处罚、风控处置 |
| `message` | `message-center` | 站内信、订阅消息、提醒 |

各业务包内建议分为：

| 层级 | 目录 | 职责 |
| --- | --- | --- |
| Controller | `controller` | 接收 HTTP 请求，完成参数校验、身份获取和响应包装，不写业务细节。 |
| Service | `service` | 编排业务流程，处理事务、权限判断、状态流转和跨表操作。 |
| Repository | `repository` | 数据访问层，封装 PostgreSQL 查询与持久化。 |
| Domain | `domain` | 领域实体、枚举、领域常量和简单领域规则。 |
| DTO | `dto` | 请求、响应、查询条件、内部命令对象。 |

## 3. Java 21 代码风格

1. 使用 UTF-8 编码。
2. 类名使用 `UpperCamelCase`，方法名和变量名使用 `lowerCamelCase`。
3. 常量使用 `UPPER_SNAKE_CASE`。
4. 单个方法建议不超过 80 行，复杂业务拆分到私有方法或领域服务。
5. 禁止在业务代码中使用魔法值，状态值必须定义枚举或常量。
6. 不允许吞异常，捕获异常后必须记录日志或转换为业务异常。
7. 可以适度使用 Java 21 的 `record`、`switch expression`、`var`，但不得牺牲可读性。

DTO 可使用 `record`：

```java
public record StudioSearchRequest(
        String keyword,
        Long cityId,
        Long danceStyleId,
        BigDecimal longitude,
        BigDecimal latitude,
        Integer distanceKm
) {}
```

`record` 不用于 JPA 实体或需要可变状态的领域对象。

## 4. Spring Boot 分层规范

### 4.1 Controller

Controller 只负责 HTTP 层逻辑，不直接访问 Repository。

```java
@RestController
@RequestMapping("/api/mp/studios")
@RequiredArgsConstructor
public class MpStudioController {

    private final StudioQueryService studioQueryService;

    @GetMapping
    public ApiResponse<PageResult<StudioSummaryResponse>> searchStudios(
            @Valid StudioSearchRequest request
    ) {
        return ApiResponse.ok(studioQueryService.searchStudios(request));
    }
}
```

要求：

1. 入参必须使用 DTO，不直接暴露 Entity。
2. 写接口必须使用 `@RequestBody @Valid`。
3. 路径变量使用资源名，不使用 `createReview`、`getStudioList` 这类动词式路径。
4. Controller 不返回 `Map<String, Object>`。
5. 当前用户身份必须从安全上下文读取，不信任前端传入的 `userId`。

### 4.2 Service

Service 承担业务编排和事务边界。

```java
@Service
@RequiredArgsConstructor
public class ReviewWriteService {

    private final ReviewRepository reviewRepository;
    private final ReviewDimensionScoreRepository dimensionScoreRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        Review review = Review.create(userId, request);
        reviewRepository.save(review);
        dimensionScoreRepository.saveAll(review.buildDimensionScores(request.dimensionScores()));
        return ReviewResponse.from(review);
    }
}
```

要求：

1. 新增、修改、删除、多表写入必须标注 `@Transactional`。
2. 查询方法可使用 `@Transactional(readOnly = true)`。
3. Service 不直接拼接 SQL 字符串。
4. 不跨模块直接操作对方数据表；跨域协作通过应用服务、领域事件或只读快照完成。

### 4.3 Repository

Repository 只负责数据访问，不写业务状态判断。

要求：

1. 查询方法命名必须表达筛选条件和排序意图。
2. 复杂查询独立封装为专门方法。
3. 分页查询必须有稳定排序字段。
4. 列表查询必须限制分页大小，禁止无条件全表查询。

## 5. API 分区与统一响应

接口按端侧和权限域分区：

| 路径前缀 | 使用方 | 说明 |
| --- | --- | --- |
| `/api/public/**` | 游客、小程序公开页 | 公开城市、舞室、课程等查询。 |
| `/api/mp/**` | 微信小程序 | 普通用户和教练端接口。 |
| `/api/merchant/**` | 舞室管理员 Web | 舞室管理、课程排期、试听处理等。 |
| `/api/admin/**` | 平台管理员 Web | 审核、举报、字典、平台治理。 |
| `/api/callback/**` | 第三方回调 | 微信、支付、消息等回调。 |

统一响应：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {},
  "traceId": "202605061530001234"
}
```

分页响应：

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 100
}
```

分页要求：

1. `page` 从 1 开始。
2. `pageSize` 默认 20，最大不超过 100。
3. `sort` 字段必须在白名单内。

## 6. 错误码规范

错误码使用模块前缀：

| 模块 | 前缀 | 示例 |
| --- | --- | --- |
| 通用 | `COMMON` | `COMMON_PARAM_INVALID` |
| 账号权限 | `IAM` | `IAM_TOKEN_EXPIRED` |
| 用户资料 | `PROFILE` | `PROFILE_NOT_FOUND` |
| 舞室课程 | `CATALOG` | `CATALOG_STUDIO_NOT_FOUND` |
| 评价 | `REVIEW` | `REVIEW_DUPLICATED` |
| 约练 | `PRACTICE` | `PRACTICE_FULL` |
| 成长档案 | `GROWTH` | `GROWTH_CHECKIN_INVALID` |
| 商家 | `MERCHANT` | `MERCHANT_CLAIM_PENDING` |
| 平台治理 | `GOVERNANCE` | `GOVERNANCE_REPORT_NOT_FOUND` |
| 消息 | `MESSAGE` | `MESSAGE_TEMPLATE_NOT_FOUND` |

业务异常统一转换为 `ApiResponse`，不得将堆栈直接暴露给前端。

## 7. 数据库规范

数据库对象统一使用小写蛇形命名。

| 对象 | 规范 | 示例 |
| --- | --- | --- |
| 表名 | 单数或业务名词 | `studio`、`course_schedule` |
| 字段名 | 小写蛇形 | `created_at`、`coach_id` |
| 主键 | `id` | `id bigint` |
| 外键字段 | `xxx_id` | `studio_id`、`coach_id` |
| 索引 | `idx_表名_字段名` | `idx_studio_city_id_status` |
| 唯一约束 | `uk_表名_字段名` | `uk_app_user_phone` |
| 检查约束 | `chk_表名_字段名` | `chk_review_status` |

字段类型要求：

| 数据 | PostgreSQL 类型 | 说明 |
| --- | --- | --- |
| 主键 | `BIGINT GENERATED BY DEFAULT AS IDENTITY` | 与当前 schema 保持一致。 |
| 金额 | `NUMERIC(10,2)` | 禁止使用浮点类型。 |
| 经纬度 | `NUMERIC(10,6)` | 与当前 schema 保持一致。 |
| 时间 | `TIMESTAMPTZ` | 统一存储带时区时间。 |
| 状态 | `VARCHAR(16)` 或 `VARCHAR(32)` | 与 Java 枚举保持一致。 |
| 长文本 | `TEXT` | 评价正文、简介等内容。 |
| JSON 扩展 | `JSONB` | 用于低频配置或可扩展字段。 |

不全局新增 `deleted` 字段。删除、隐藏、下架、取消等语义通过业务状态字段表达，例如 `status`、`review_status`、`post_status`、`publish_status`。

## 8. 命名与枚举对齐

### 8.1 领域命名

- 教练统一使用 `coach`，不使用 `teacher`。
- 账号主表统一为 `app_user`，Java 领域对象可命名为 `AppUser`。
- 多态对象统一为 `targetType + targetId`，数据库字段为 `target_type + target_id`。
- 舞种统一引用 `dance_style`，不长期沉淀自由字符串。

### 8.2 评价对象

数据库 `review.target_type` 支持：

```java
public enum ReviewTargetType {
    STUDIO,
    COURSE,
    COACH
}
```

### 8.3 收藏对象

数据库 `favorite.target_type` 支持：

```java
public enum FavoriteTargetType {
    STUDIO,
    COURSE,
    COACH,
    WORKSHOP,
    CONTENT_POST
}
```

### 8.4 约练状态

数据库 `practice_post.post_status` 对应：

```java
public enum PracticePostStatus {
    DRAFT,
    PUBLISHED,
    MATCHED,
    CONFIRMED,
    COMPLETED,
    CANCELED,
    EXPIRED
}
```

## 9. 参数校验规范

所有外部入参必须显式校验。

```java
public record CreatePracticePostRequest(
        @NotNull Long danceStyleId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @Min(1) @Max(20) Integer expectedPeopleMax,
        @Size(max = 500) String description
) {}
```

要求：

1. 字符串长度必须设置上限。
2. 金额、人数、时长必须设置范围。
3. 时间类参数必须校验开始时间早于结束时间。
4. 枚举值不使用裸字符串长期流转，应尽早转换为枚举类型。
5. 参数校验失败由全局异常处理器统一返回。

## 10. 事务、幂等与并发

必须使用事务的场景：

1. 商家认领审核通过并绑定管理员权限。
2. 创建课程排期并刷新相关缓存。
3. 创建评价并写入维度分、附件和风控记录。
4. 发布约练并初始化参与人数。
5. 确认约练申请并更新人数、状态和通知。
6. Workshop 下单、支付回调、退款、签到和结算。

幂等要求：

| 场景 | 建议方案 |
| --- | --- |
| 支付/退款回调 | 回调流水唯一约束 + `idempotency_token` |
| Workshop 重复报名 | 唯一约束 + Redis 幂等键 |
| 重复评价 | 用户、目标对象、来源记录唯一约束 |
| 重复加入约练 | `practice_post_id + applicant_user_id` 唯一约束 |
| 重复试听预约 | 业务规则校验 + 幂等键 |

## 11. 安全与数据权限

1. 所有非公开接口必须鉴权。
2. 管理端接口必须校验角色权限。
3. 前端传入的用户 id 不可信，应以后端 Token 解析出的用户身份为准。
4. 普通用户只能访问自己的收藏、预约、约练申请、打卡、成长目标和消息。
5. 教练只能访问自己的课程关系、评价、活动和授权舞室数据。
6. 舞室管理员只能访问已认领或被授权的舞室数据。
7. 平台管理员按 RBAC 访问审核、举报、字典、风控和平台治理数据。
8. SQL 查询必须使用参数绑定，禁止字符串拼接。
9. 日志不得记录 Token、验证码、完整手机号、身份证号等敏感信息。

## 12. 媒体与文件上传

文件统一先写入 `media_asset`，再通过 `media_attachment` 与业务对象关联。

规则：

1. 上传必须校验文件类型、大小、扩展名和业务用途。
2. 业务表只保留头像、封面等少量强关联字段，例如 `avatar_asset_id`、`cover_asset_id`。
3. 评价图片、成长作品、动态附件、资质材料统一走 `media_attachment`。
4. 文件审核状态以 `media_asset.audit_status` 为准。
5. 不在业务表中散落保存多个文件 URL。

## 13. Redis 使用规范

Redis 只存放短期状态、热点缓存、幂等结果和限流计数，PostgreSQL 始终是权威数据源。

| 场景 | Key 示例 |
| --- | --- |
| 验证码 | `auth:sms:{phone}` |
| 刷新令牌 | `auth:refresh:{userId}:{deviceId}` |
| 舞室热点详情 | `catalog:studio:detail:{studioId}` |
| 评价摘要 | `review:summary:{targetType}:{targetId}` |
| 约练广场 | `practice:square:{cityId}:{danceStyleId}` |
| 幂等结果 | `idem:{bizType}:{bizKey}` |

要求：

1. Key 必须包含业务域前缀。
2. 所有 Key 必须设置过期时间，永久配置类数据不放 Redis。
3. 缓存失效策略必须写清楚触发时机。
4. 缓存不可作为关键业务状态的唯一来源。

## 14. 日志与审计

日志框架使用 SLF4J + Logback。

要求：

1. 禁止使用 `System.out.println`。
2. 日志必须包含关键业务 id，例如 `userId`、`studioId`、`orderNo`。
3. 异常日志必须带异常对象。
4. 高频循环中禁止打印大量 `info` 日志。
5. 关键状态流转必须写入 `audit_log` 或对应业务记录。

## 15. 数据库迁移

统一使用 Flyway 管理数据库版本。

脚本目录：

```text
src/main/resources/db/migration
```

命名规范：

```text
V1__init_schema.sql
V2__seed_dictionary_data.sql
V3__add_practice_geo_hash.sql
```

要求：

1. 已合并的迁移脚本不得修改，只能新增脚本。
2. 每个脚本只做一个明确主题的变更。
3. 删除字段、改字段类型、修改约束等高风险操作必须提前评审。
4. 字典初始化和演示数据应拆分，避免生产环境误导入演示数据。

## 16. 测试规范

| 类型 | 工具 | 覆盖内容 |
| --- | --- | --- |
| 单元测试 | JUnit 5 + Mockito | Service 业务规则、状态流转、异常分支 |
| Web 测试 | Spring MockMvc | Controller 参数校验、响应结构、权限控制 |
| 数据库测试 | Testcontainers PostgreSQL | Repository 查询、迁移脚本、PostgreSQL 特性 |
| 集成测试 | Spring Boot Test | 登录、收藏、预约、评价、约练、打卡等闭环 |

要求：

1. 核心 Service 必须有单元测试。
2. Bug 修复必须补充回归测试。
3. 数据库相关逻辑优先使用真实 PostgreSQL 测试环境，不用 H2 替代 PostgreSQL 特性。
4. 测试方法命名应表达场景和预期。

## 17. Git 与代码评审

分支命名：

```text
feature/studio-search
feature/review-module
fix/practice-join-duplicated
refactor/iam-auth
```

Commit 规范：

```text
feat: add studio search api
fix: prevent duplicate practice join
refactor: split review score calculator
docs: add backend code style guide
test: add review service tests
```

Review 检查项：

1. 是否符合分层职责。
2. 是否存在未校验的外部入参。
3. 是否直接返回 Entity。
4. 是否存在 N+1 查询或无分页查询。
5. 是否缺少事务或幂等保护。
6. 是否记录敏感信息。
7. 是否缺少核心测试。
8. 数据库变更是否有 Flyway 脚本。
9. 枚举、状态、字段是否与 schema 保持一致。

## 18. 模块补充规范

### 18.1 舞室与课程

1. 舞室搜索 MVP 先用 PostgreSQL 城市/商圈/边界框粗筛，再用距离函数精排。
2. 课程、教练、舞室展示使用公开快照或聚合读模型。
3. 舞种使用 `dance_style` 字典，不允许自由字符串长期沉淀。
4. 收藏统一使用 `favorite.target_type + favorite.target_id`。

### 18.2 评价

1. 评价对象必须明确 `targetType` 和 `targetId`。
2. 评分维度使用 `review_dimension_score` 单独保存。
3. 商家只能回复和申诉，不能直接删除评价。
4. 异常评价必须保留原始记录和处理日志。

### 18.3 约练

1. 约练状态必须与 `practice_post.post_status` 保持一致。
2. 满员后禁止继续加入。
3. 发起者取消约练时必须通知已确认成员。
4. 私密联系方式只能在双方确认后展示。
5. 过期关闭任务必须可重复执行。

### 18.4 成长档案

1. 打卡记录属于用户隐私数据，默认可见性以业务设计为准。
2. 统计数据可异步计算，但原始打卡记录不得丢失。
3. 成长时间线来自试听、课程、约练、打卡、作品、Workshop 等事件聚合。

### 18.5 商家与教练

1. 教练关系以 `studio_coach_relation` 为准，明确全职、签约、自由三类关系。
2. 教练发布 Workshop 的权限必须由后端根据关系和审核状态判断。
3. 舞室管理员数据权限必须绑定到已认领或授权的舞室。

## 19. 结论

后端实现以“模块化单体 + 清晰领域边界 + PostgreSQL 稳定数据模型”为基础推进。MVP 阶段优先保证账号权限、舞室课程、结构化评价、约练和成长打卡模块质量；后续社区、Workshop、推荐、结算等能力在现有边界上逐步扩展。

