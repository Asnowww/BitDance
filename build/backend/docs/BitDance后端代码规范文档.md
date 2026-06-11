# BitDance 后端代码规范文档

版本：V1.0  
技术栈：Spring Boot + Java 21 + PostgreSQL  
适用范围：BitDance 舞蹈学习与约练平台后端开发、代码评审、团队协作与后续维护

## 1. 规范目标

本规范用于统一 BitDance 后端项目的代码风格、目录结构、接口设计、数据库设计和工程协作方式，降低多人开发时的理解成本，保证系统在 MVP 阶段可快速交付，并为后续商家端、活动交易、推荐搜索等能力扩展留下清晰边界。

后端开发应遵循以下原则：

1. 业务边界清晰：按用户、舞室课程、评价、约练、成长档案、商家管理等领域组织代码。
2. 接口语义稳定：REST API 命名清晰，返回结构统一，错误码可追踪。
3. 数据一致可靠：涉及多表写入的业务必须显式使用事务。
4. 安全默认开启：鉴权、权限、参数校验、敏感数据保护必须内建到开发流程。
5. 代码可读优先：避免过度抽象，优先写简单、可测试、可维护的代码。

## 2. 项目结构规范

推荐按“领域模块 + 分层职责”组织代码。MVP 阶段采用模块化单体结构，后续可按模块拆分服务。

```text
src/main/java/com/bitdance
  ├── BitDanceApplication.java
  ├── common
  │   ├── config          # 全局配置
  │   ├── exception       # 统一异常与错误码
  │   ├── response        # 统一响应结构
  │   ├── security        # 鉴权、权限、Token
  │   ├── validation      # 自定义校验注解
  │   └── util            # 通用工具类
  ├── user
  │   ├── controller
  │   ├── service
  │   ├── repository
  │   ├── domain
  │   └── dto
  ├── studio
  ├── review
  ├── practice
  ├── growth
  ├── merchant
  └── admin

src/main/resources
  ├── application.yml
  ├── application-dev.yml
  ├── application-prod.yml
  └── db/migration        # Flyway 数据库迁移脚本
```

各层职责如下：

| 层级 | 目录 | 职责 |
| --- | --- | --- |
| Controller | `controller` | 接收 HTTP 请求，完成参数校验、身份获取、调用 Service，不写业务细节。 |
| Service | `service` | 编排业务流程，处理事务、权限判断、状态流转和跨表操作。 |
| Repository | `repository` | 数据访问层，封装 PostgreSQL 查询与持久化。 |
| Domain | `domain` | 领域实体、枚举、领域常量。 |
| DTO | `dto` | 请求、响应、内部传输对象。 |
| Common | `common` | 全局通用能力，不放具体业务逻辑。 |

## 3. Java 21 代码风格

### 3.1 基本风格

1. 使用 UTF-8 编码。
2. 类名使用 `UpperCamelCase`，方法名、变量名使用 `lowerCamelCase`。
3. 常量使用 `UPPER_SNAKE_CASE`。
4. 单个方法建议不超过 80 行；复杂业务应拆成私有方法或领域服务。
5. 禁止在业务代码中使用魔法值，统一定义枚举或常量。
6. 优先使用不可变对象，能用 `final` 的局部变量可以加 `final`。
7. 不允许吞异常，捕获异常后必须记录日志或转换为业务异常。

### 3.2 Java 21 特性使用建议

可以适度使用 Java 21 新特性，但不得为了炫技牺牲可读性。

推荐使用：

```java
public record StudioSearchRequest(
        String keyword,
        String danceType,
        BigDecimal longitude,
        BigDecimal latitude,
        Integer distanceKm
) {}
```

适用场景：

| 特性 | 使用场景 | 注意事项 |
| --- | --- | --- |
| `record` | DTO、只读值对象 | 不用于 JPA 实体或需要可变状态的对象。 |
| `switch expression` | 枚举分支、状态转换 | 分支必须覆盖完整，避免默认吞掉未知状态。 |
| `var` | 局部变量类型非常明确时 | 禁止在复杂泛型、返回值不清晰处滥用。 |
| `Optional` | Repository 查询结果 | 不用于字段属性，不作为 Controller 入参。 |

## 4. Spring Boot 开发规范

### 4.1 Controller 规范

Controller 只负责 HTTP 层逻辑，不直接访问 Repository，不写复杂业务判断。

```java
@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @GetMapping
    public ApiResponse<PageResult<StudioSummaryResponse>> searchStudios(
            @Valid StudioSearchRequest request
    ) {
        return ApiResponse.ok(studioService.searchStudios(request));
    }
}
```

要求：

1. 所有接口统一以 `/api` 开头。
2. 入参必须使用 DTO，不直接暴露实体类。
3. 写接口必须使用 `@RequestBody @Valid`。
4. 路径变量使用名词，不使用动词堆叠。
5. Controller 不返回 `Map<String, Object>`。

### 4.2 Service 规范

Service 承担业务编排和事务边界。

```java
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StudioRepository studioRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        var studio = studioRepository.findById(request.studioId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDIO_NOT_FOUND));

        var review = Review.create(userId, studio.getId(), request);
        reviewRepository.save(review);

        return ReviewResponse.from(review);
    }
}
```

要求：

1. 涉及新增、修改、删除、多表写入时必须标注 `@Transactional`。
2. 查询方法可使用 `@Transactional(readOnly = true)`。
3. Service 方法入参应尽量明确，不传递过大的上下文对象。
4. 不允许在 Service 中直接拼接 SQL 字符串。
5. 不允许跨模块直接操作对方数据表，必须通过对方 Service 或清晰的 Repository 查询边界完成。

### 4.3 Repository 规范

Repository 只负责数据访问，不写业务状态判断。

命名建议：

```text
UserRepository
StudioRepository
CourseRepository
ReviewRepository
PracticePostRepository
CheckinRecordRepository
MerchantAccountRepository
```

要求：

1. 查询方法命名必须表达筛选条件和排序意图。
2. 复杂查询应独立成专门方法，不在 Service 中拼接。
3. 分页查询必须有稳定排序字段。
4. 列表查询必须限制分页大小，禁止无条件全表查询。

## 5. DTO 与对象转换规范

### 5.1 DTO 分类

| 类型 | 命名 | 示例 |
| --- | --- | --- |
| 请求对象 | `XxxRequest` | `CreateReviewRequest` |
| 响应对象 | `XxxResponse` | `StudioDetailResponse` |
| 列表摘要 | `XxxSummaryResponse` | `CourseSummaryResponse` |
| 查询条件 | `XxxQuery` 或 `XxxSearchRequest` | `PracticeSearchRequest` |
| 内部命令 | `XxxCommand` | `CreatePracticeCommand` |

### 5.2 转换规则

1. Entity 不直接返回给前端。
2. Response 由静态工厂方法或 Mapper 生成。
3. 简单转换可使用 `from` 方法，复杂转换使用专门 Mapper。
4. DTO 中不包含数据库实体对象。

示例：

```java
public record StudioSummaryResponse(
        Long id,
        String name,
        String address,
        BigDecimal rating,
        Integer reviewCount
) {
    public static StudioSummaryResponse from(Studio studio) {
        return new StudioSummaryResponse(
                studio.getId(),
                studio.getName(),
                studio.getAddress(),
                studio.getRating(),
                studio.getReviewCount()
        );
    }
}
```

## 6. 统一响应与错误码规范

### 6.1 响应结构

所有接口统一返回：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {},
  "traceId": "202604261530001234"
}
```

Java 定义建议：

```java
public record ApiResponse<T>(
        String code,
        String message,
        T data,
        String traceId
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("SUCCESS", "success", data, TraceContext.getTraceId());
    }
}
```

### 6.2 错误码规范

错误码使用模块前缀：

| 模块 | 前缀 | 示例 |
| --- | --- | --- |
| 通用 | `COMMON` | `COMMON_PARAM_INVALID` |
| 用户 | `USER` | `USER_NOT_FOUND` |
| 舞室课程 | `STUDIO` | `STUDIO_NOT_FOUND` |
| 评价 | `REVIEW` | `REVIEW_DUPLICATED` |
| 约练 | `PRACTICE` | `PRACTICE_FULL` |
| 成长档案 | `GROWTH` | `GROWTH_CHECKIN_INVALID` |
| 商家 | `MERCHANT` | `MERCHANT_CLAIM_PENDING` |

业务异常统一使用：

```java
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

## 7. 参数校验规范

所有外部入参必须显式校验。

```java
public record CreatePracticeRequest(
        @NotBlank String danceType,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Min(1) @Max(20) Integer expectedMembers,
        @Size(max = 500) String description
) {}
```

要求：

1. 字符串长度必须设置上限。
2. 金额、人数、时长等数值必须设置范围。
3. 时间类参数必须校验开始时间早于结束时间。
4. 枚举值不使用裸字符串长期流转，应尽早转换为枚举类型。
5. 参数校验失败由全局异常处理器统一返回。

## 8. PostgreSQL 数据库规范

### 8.1 命名规范

数据库对象统一使用小写蛇形命名。

| 对象 | 规范 | 示例 |
| --- | --- | --- |
| 表名 | 单数或业务名词，统一小写蛇形 | `studio`、`course_schedule` |
| 字段名 | 小写蛇形 | `created_at`、`user_id` |
| 主键 | `id` | `id bigint` |
| 外键字段 | `xxx_id` | `studio_id`、`teacher_id` |
| 索引 | `idx_表名_字段名` | `idx_studio_city_id` |
| 唯一索引 | `uk_表名_字段名` | `uk_user_phone` |

### 8.2 字段规范

通用字段：

```sql
id          BIGSERIAL PRIMARY KEY,
created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
deleted     BOOLEAN NOT NULL DEFAULT FALSE,
status      VARCHAR(32) NOT NULL
```

字段类型建议：

| 数据 | PostgreSQL 类型 | 说明 |
| --- | --- | --- |
| 主键 | `BIGSERIAL` 或 `BIGINT GENERATED BY DEFAULT AS IDENTITY` | MVP 阶段简单可靠。 |
| 金额 | `NUMERIC(12,2)` | 禁止使用浮点类型。 |
| 经纬度 | `NUMERIC(10,7)` | 后续可升级 PostGIS。 |
| 时间 | `TIMESTAMPTZ` | 统一存储带时区时间。 |
| 状态 | `VARCHAR(32)` | 与 Java 枚举保持一致。 |
| 长文本 | `TEXT` | 评价正文、简介等内容。 |
| JSON 扩展 | `JSONB` | 用于非核心、可扩展配置字段。 |

### 8.3 SQL 与索引规范

1. 所有分页查询必须包含排序字段。
2. 高频筛选条件必须建立组合索引。
3. 禁止在高频查询条件上对字段使用函数导致索引失效。
4. 模糊搜索 MVP 可使用 `ILIKE`，后续迁移全文搜索或 Elasticsearch。
5. 逻辑删除字段统一使用 `deleted`。
6. 重要状态流转必须记录审计日志。

示例索引：

```sql
CREATE INDEX idx_studio_city_dance_type
    ON studio (city_id, main_dance_type)
    WHERE deleted = false;

CREATE INDEX idx_review_target
    ON review (target_type, target_id, created_at DESC)
    WHERE deleted = false;
```

## 9. 数据库迁移规范

统一使用 Flyway 管理数据库版本。

脚本目录：

```text
src/main/resources/db/migration
```

命名规范：

```text
V1__init_schema.sql
V2__create_studio_tables.sql
V3__create_review_tables.sql
V4__add_practice_module.sql
```

要求：

1. 已合并到主分支的迁移脚本不得修改，只能新增脚本。
2. 每个脚本只做一个明确主题的变更。
3. DDL 必须可重复理解，字段注释要清晰。
4. 删除字段、改字段类型等高风险操作必须提前评审。

## 10. 事务与并发规范

必须使用事务的场景：

1. 创建评价并更新评分聚合。
2. 发布约练并创建成员记录。
3. 用户报名活动并生成订单。
4. 支付回调更新订单和支付记录。
5. 商家认领审核通过并绑定管理员权限。

并发控制建议：

| 场景 | 方案 |
| --- | --- |
| 活动名额扣减 | 数据库行锁或乐观锁版本号。 |
| 重复报名 | 唯一索引 + 幂等 token。 |
| 重复评价 | 用户、目标对象、来源记录建立唯一约束。 |
| 短信验证码 | Redis 计数限流 + 过期时间。 |

## 11. 日志规范

日志框架使用 SLF4J + Logback。

```java
private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
```

要求：

1. 禁止使用 `System.out.println`。
2. 日志必须包含关键业务 id，例如 `userId`、`studioId`、`orderId`。
3. 不记录手机号完整值、Token、验证码、身份证号等敏感信息。
4. 异常日志必须带异常对象。
5. 高频循环中禁止打印大量 info 日志。

示例：

```java
log.info("create review success, userId={}, targetType={}, targetId={}",
        userId, request.targetType(), request.targetId());

log.warn("practice join rejected, userId={}, practiceId={}, reason={}",
        userId, practiceId, reason);
```

## 12. 安全规范

1. 所有非公开接口必须鉴权。
2. 管理端接口必须校验角色权限。
3. 用户只能访问自己的收藏、打卡、约练申请等私有数据。
4. 商家只能维护自己认领或绑定的舞室数据。
5. 文件上传必须校验类型、大小和扩展名。
6. 前端传入的用户 id 不可信，应以后端 Token 解析出的用户身份为准。
7. SQL 查询必须使用参数绑定，禁止字符串拼接。

权限角色建议：

| 角色 | 说明 |
| --- | --- |
| `USER` | 普通舞蹈学习者。 |
| `COACH` | 教练。 |
| `MERCHANT_ADMIN` | 舞室管理员。 |
| `PLATFORM_ADMIN` | 平台管理员。 |

## 13. 接口设计规范

### 13.1 URL 命名

使用名词复数或业务资源名，不在 URL 中堆叠动词。

推荐：

```text
GET    /api/studios
GET    /api/studios/{studioId}
POST   /api/reviews
POST   /api/practices
POST   /api/practices/{practiceId}/members
DELETE /api/favorites/{favoriteId}
```

不推荐：

```text
POST /api/createReview
GET  /api/getStudioList
POST /api/user/doJoinPractice
```

### 13.2 HTTP 方法

| 方法 | 用途 |
| --- | --- |
| `GET` | 查询资源。 |
| `POST` | 创建资源或执行复杂动作。 |
| `PUT` | 全量更新资源。 |
| `PATCH` | 局部更新资源。 |
| `DELETE` | 删除或取消资源。 |

### 13.3 分页规范

请求参数：

```text
page=1&pageSize=20&sort=createdAt,desc
```

响应结构：

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 100
}
```

要求：

1. `page` 从 1 开始。
2. `pageSize` 默认 20，最大不超过 100。
3. 排序字段必须在白名单内。

## 14. 测试规范

测试分层：

| 类型 | 工具 | 覆盖内容 |
| --- | --- | --- |
| 单元测试 | JUnit 5 + Mockito | Service 业务规则、状态流转、异常分支。 |
| Web 测试 | Spring MockMvc | Controller 参数校验、响应结构、权限控制。 |
| 数据库测试 | Testcontainers PostgreSQL | Repository 查询、迁移脚本、索引相关查询。 |
| 集成测试 | Spring Boot Test | 关键业务闭环，如发布约练、提交评价、创建收藏。 |

要求：

1. 核心 Service 必须有单元测试。
2. Bug 修复必须补充回归测试。
3. 数据库相关逻辑优先使用真实 PostgreSQL 测试环境，不用 H2 替代 PostgreSQL 特性。
4. 测试方法命名应表达场景和预期。

示例：

```java
@Test
void createReview_shouldReject_whenUserAlreadyReviewedTarget() {
    // given
    // when
    // then
}
```

## 15. Git 与代码评审规范

### 15.1 分支命名

```text
feature/studio-search
feature/review-module
fix/practice-join-duplicated
refactor/user-auth
```

### 15.2 Commit 规范

```text
feat: add studio search api
fix: prevent duplicate practice join
refactor: split review score calculator
docs: add backend code style guide
test: add review service tests
```

### 15.3 Review 检查项

1. 是否符合分层职责。
2. 是否存在未校验的外部入参。
3. 是否直接返回 Entity。
4. 是否存在 N+1 查询或无分页查询。
5. 是否缺少事务。
6. 是否记录敏感信息。
7. 是否缺少核心测试。
8. 数据库变更是否有 Flyway 脚本。

## 16. 模块开发补充规范

### 16.1 舞室与课程模块

1. 舞室搜索默认按距离、评分、评价数综合排序。
2. 经纬度字段必须使用统一精度。
3. 课程难度使用枚举：`BEGINNER_FRIENDLY`、`BASIC`、`INTERMEDIATE`、`ADVANCED`。
4. 舞种类型使用字典表或枚举，不允许自由字符串长期沉淀。

### 16.2 评价模块

1. 评价对象必须明确 `targetType` 和 `targetId`。
2. 评分维度使用单独表保存，便于不同对象扩展维度。
3. 商家只能回复和申诉，不能直接删除评价。
4. 异常评价必须保留原始记录和处理日志。

### 16.3 约练模块

1. 约练状态必须使用状态机思路设计。
2. 满员后禁止继续加入。
3. 发起者取消约练时必须通知已加入成员。
4. 私密位置和联系方式只能在双方确认后展示。

### 16.4 成长档案模块

1. 打卡记录属于用户隐私数据，默认仅自己可见。
2. 统计数据可异步计算，但原始打卡记录不得丢失。
3. 收藏对象必须记录对象类型和对象 id。

## 17. 推荐枚举命名

```java
public enum CourseLevel {
    BEGINNER_FRIENDLY,
    BASIC,
    INTERMEDIATE,
    ADVANCED
}

public enum ReviewTargetType {
    STUDIO,
    COURSE,
    TEACHER,
    WORKSHOP
}

public enum PracticeStatus {
    DRAFT,
    PUBLISHED,
    FULL,
    CANCELLED,
    FINISHED,
    EXPIRED
}
```

## 18. 结论

BitDance 后端应以“模块化单体 + 清晰领域边界 + PostgreSQL 稳定数据模型”为基础开展开发。Spring Boot 负责提供成熟的 Web、事务、安全和工程组织能力；Java 21 提供更简洁的类型表达；PostgreSQL 负责承载可靠的结构化业务数据。

在 MVP 阶段，团队应优先保证用户账号、舞室课程、结构化评价、约练和成长打卡模块的代码质量。后续商家端、活动交易、搜索推荐等能力可以在现有模块边界上逐步扩展，而不需要推倒重来。
