# BitDance 后端交接文档 v2（BE-014.3 + BE-015）

> 写作时间：2026-05-12
> 写作动因：上一棒（Claude）额度即将耗尽，把 BE-014 第三段与 BE-015 收官工作的开发目标、契约、约束、测试方式、流程纪律一次性交底。
> 适用对象：能读 Java/Spring Boot 3 + JPA + Spring Security + JJWT + Spring AOP 的 AI 或人类开发者。
> 前置阅读：先看 `build/docs/backend-handoff-BE011-BE015.md` 第 0 章（项目结构、流程纪律、测试基础设施、Git 工作流）。本文档假定那一章你已经看过，不再重复。

---

## 0. 当前进度快照

| BE | 模块 | commit | tests |
| --- | --- | --- | --- |
| BE-001/002 | 工程骨架 + M3 验证码登录 + JWT | `cf77033` + `6ec75e6` | 4 |
| BE-003/004 | M3 Profile + Message | `ae542ef` | 12 |
| BE-005 | M1 Studio + Favorite | `b67b34b` | 22 |
| BE-006 | M1 Coach/Course/Schedule | `99d193f` | 31 |
| BE-007 | M1 Trial Booking 用户侧 | `0e3896d` | 40 |
| BE-008 | M2 Review + 风控权重 | `c497807` | 49 |
| BE-009 | M4 Practice 主流程 + 过期定时 | `9e9384c` | 66 |
| BE-010 | M5 Growth | `183ff71` | 83 |
| BE-011 | M6 Community | `689e1bf` | 109 |
| BE-012 | M6 Workshop 用户侧 | `e5567a6` | 131 |
| BE-013 | M4 Buddy 互评 + 搭子 | `56c6c65` | 145 |
| BE-014.1 | Trial 商家侧 + Review Reply/Appeal | `1decf5d` | 163 |
| BE-014.2 | Studio Claim + Coach Relation + Merchant Workshop 创建侧 + MerchantAccessGuard | `7c2d207` | 186 |

**待办**：BE-014.3、BE-015。

**当前 `mvn test`**：186/186 PASS。

---

## 1. 已建立的基础设施（BE-014.3 / 015 必须复用）

### 1.1 包结构

```
com.bitdance/
  BitDanceApplication
  common/{web,exception,audit,config}     ApiResponse、BizException、GlobalExceptionHandler、TraceIdFilter、BaseEntity、CorsConfig、JpaConfig
  iam/{domain,repository,service,...}     AppUser、UserRoleBinding、JwtService、JwtAuthFilter、SecurityConfig、CurrentUser
                                          - SecurityConfig：/h5 鉴权、/merchant 要 STUDIO_ADMIN 或 PLATFORM_ADMIN、/admin 要 PLATFORM_ADMIN
                                          - CurrentUser.getId() 强制登录、getIdOrNull() 公开接口选填
  profile/                                M3 Profile / Privacy / Preference
  message/                                M3 Notification
  catalog/                                M1 Studio/Coach/Course/Schedule/Studio搜索
  booking/                                M1 TrialBooking 用户侧 + MerchantTrialBookingService（BE-014.1）
  review/                                 M2 Review + Reply（BE-014.1）+ Appeal（BE-014.1）
  practice/                               M4 Practice 主流程 + 过期定时
  buddy/                                  M4 BuddyService（约练互评 + 搭子）
  growth/                                 M5 Growth
  community/                              M6 社区主流程
  workshop/                               M6 Workshop 用户侧 + MerchantWorkshopService（BE-014.2）
  favorite/                               多态收藏
  merchant/                               商家侧基础设施（BE-014.2）：
                                          - domain：StudioClaim、StudioCoachRelation
                                          - service：MerchantAccessGuard、StudioClaimService、CoachRelationService
                                          - controller：StudioClaimController、CoachRelationController
```

### 1.2 MerchantAccessGuard（**BE-014.3 必须复用**）

位置：`com.bitdance.merchant.service.MerchantAccessGuard`。

```java
@Component
public class MerchantAccessGuard {
    public void requireStudioOwnership(Long userId, Long studioId);  // 已 approved studio_claim 或 PLATFORM_ADMIN
    public Set<Long> approvedStudioIds(Long userId);
    public boolean isPlatformAdmin(Long userId);
}
```

BE-014.3 所有 `/merchant/**` 写操作必须先调 `guard.requireStudioOwnership(userId, studioId)`，违反抛 `BizException("FORBIDDEN", ...)`。

### 1.3 测试切片模板（**所有 Controller 测试统一这么写**）

```java
@WebMvcTest(controllers = XxxController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class XxxControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean XxxService service;
    @MockBean JwtService jwtService;                 // 必须 mock 否则 JwtAuthFilter 找不到 bean

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));  // 或 "STUDIO_ADMIN" / "PLATFORM_ADMIN" / "COACH"
        when(jwtService.parse(any())).thenReturn(claims);
    }
}
```

每个用例 `.header("Authorization", "Bearer fake")` 才能让 `JwtAuthFilter` 解析 userId。未登录用例不带这个 header → `CurrentUser.getId()` 会抛 `BizException("UNAUTHORIZED")` → 期望 `status().isBadRequest()` + `code=UNAUTHORIZED`。

### 1.4 流程纪律（**违反会被回滚**）

每个 BE-* 子段（如 BE-014.3）按下列顺序在同一开发轮次内完成：

1. 写代码（实体/Repository/Service/DTO/Controller/Test）
2. 本地执行 `mvn test`，**必须 BUILD SUCCESS**；不通过先修复，不允许 push
3. `git add build/backend && git commit -m "feat(be): BE-XXX ..."`，提交信息包含：实体说明 / Service 关键状态流转 / 接口清单 / 测试覆盖项 / `mvn test → Tests run: N, Failures: 0, Errors: 0, BUILD SUCCESS` / `Closes #BE-XXX` 或 `Refs #BE-XXX`
4. `git push origin master`
5. **同一轮次内**追加 `build/开发历史记录.md`：
   - "开发日志"区块顶部插入按 SKILL 第 8 节六元格式：目标与背景 / 改动范围 / 关键问题与解决 / 方案选择理由 / 待办与遗留 / 关联
   - "当前上下文快照"更新到本轮 commit hash 与 tests run
   - "变更日志"v0.4.0 追加一行
   - `git commit -m "docs: BE-XXX 开发日志同步（N/N PASS）"` 然后 push
6. **禁忌**：commit message / 日志中不得出现 Claude / ChatGPT / Anthropic / OpenAI / Copilot / Cursor / AI / LLM / agent 等字样；不要修改 schema 已用字段类型；不要 push `.env*` / `application-local.yml` / `.claude/`；不要 `--force` 推 master。

---

## BE-014.3 ｜ Coach Ops + Admin 工作台 + 商家核销 + AuditLog AOP

### 目标

BE-014 收官段。把 BE-014.2 留下的 4 个待办收齐，并交付教练角色的运营接口与平台后台审核台：

1. **教练运营侧**（`/h5/coach/**`，角色 COACH）：教练资格摘要、个人主页编辑、经营看板、授课/Workshop 列表
2. **教练资质审核**（`/h5/coach/certifications` 用户侧 + `/admin/coach-certifications` 平台侧）
3. **Workshop 平台审核**（补 BE-014.2 的 independent 教练发起 Workshop 需要的 admin 审核）
4. **商家扫码核销**（`/merchant/workshop-orders/{id}/checkin` 与用户侧 BE-012 签到对称）
5. **Report 工作台**（处理 BE-011 写入的 report_ticket）
6. **AuditLog AOP**（`@AuditAction` 注解切面，统一记录 `/merchant/** /admin/**` 写操作）

预计加 **~25-30** 测试，目标 `mvn test` 在 **215+ PASS**。

### Schema 表（行号查 `bitdance_postgresql_schema.sql`）

| 表 | 行 | 关键字段 |
| --- | --- | --- |
| `coach_certification_application` | 476 | user_id、application_type（'independent' 默认）、application_status chk `pending/approved/rejected`、remark、reviewed_by_user_id、reviewed_at、review_remark |
| `coach` | 429 | 平台审核通过时自动建 coach 行：display_name、avg_rating=0、certification_status='approved'、user_id（uk 1:1） |
| `audit_log` | 1527 | actor_user_id、action（如 `studio.claim.approve`）、target_type、target_id、payload jsonb、ip、user_agent、result（success/fail）、created_at |
| `report_ticket` | 1500 | 已有，BE-011 写入：reporter_user_id、target_type chk 8 类、target_id、reason_code、report_status chk `pending/processing/closed/rejected`、handled_by_user_id、handled_at、handle_result |
| `workshop` | 1081 | 已有：audit_status chk `pending/approved/rejected`，publish_status chk `draft/published/offline/canceled` |
| `workshop_checkin` | 1190 | 已有 BE-012：uk(order_id)，checked_in_by_user_id、checkin_status chk `checked_in/manual_checked_in/no_show`、checkin_code |

注意 `audit_log.payload` 是 jsonb；H2 不能测，按 BE-006 的 `available_time_slots` 处理方式标 `insertable=false, updatable=false` 让 JPA 忽略写。**写入用 native query**（`INSERT INTO audit_log (...) VALUES (..., :payload::jsonb, ...)`）走 EntityManager.createNativeQuery，绕开 JPA 类型映射。

### 实体清单

新增（com.bitdance.merchant.domain 与 com.bitdance.audit.domain）：

- `CoachCertificationApplication`（merchant 包，与 StudioClaim 同域）
- `AuditLog`（独立 audit 包，全局共用）

### 详细子模块设计

#### 1. 教练运营侧 `/h5/coach/**`

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/h5/coach/me` | 当前用户的教练身份摘要：是否已认证（查 coach 表 user_id）、关联 studio_id 列表（studio_coach_relation 中 coach_id 对应 active 关系）、avg_rating |
| PUT | `/h5/coach/me/profile` | 编辑 coach 的 display_name / intro / teaching_style / cover_asset_id；不能改 user_id / certification_status / avg_rating |
| GET | `/h5/coach/dashboard` | 经营看板聚合：本月授课次数（workshop_session 与 course_schedule 中 coach_id 为我）、本月学员数（workshop_order paid + trial attended 去重 user）、本月收益（settlement_rule 留 BE-015 + 收入预估）、待回复评价数（review.target_type=coach AND target_id=我 AND 无 reply）、平均评分、近 30 天下单转化率 |
| GET | `/h5/coach/courses` | 我授课的课程（course.coach_id = 我） |
| GET | `/h5/coach/workshops` | 我发起的 Workshop（workshop.coach_id = 我） |

**关键约束**：
- 所有 `/h5/coach/**` 必须先 `requireCoachIdentity(userId)`：查 coach 表，无记录则抛 `COACH_NOT_FOUND`（提示用户先提交资质）。建议新建 `CoachAccessGuard` 类似 MerchantAccessGuard。
- dashboard 走多张表聚合，建议拆成多个 Service 方法（@Transactional readOnly），返回 record `CoachDashboard(monthSessions, monthStudents, monthIncome, pendingReplies, avgRating, conversionRate)`。

#### 2. 教练资质审核

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| POST | `/h5/coach/certifications` | 用户提交资质申请：body `{ applicationType: "independent", remark }`，写 application_status='pending'。防重复：已存在 pending 申请则 `CERT_DUPLICATED` |
| GET | `/h5/coach/certifications/mine` | 我的申请进度（倒序） |
| GET | `/admin/coach-certifications?status=pending&page&pageSize` | 平台工作台（Page<T>） |
| POST | `/admin/coach-certifications/{id}/approve` | 通过：写 `application_status='approved' + reviewed_by/at/remark`；**联动**：① 若 coach 表无该 user_id 则建 coach 行（display_name 取 user_profile.nickname 或 fallback `"教练" + userId`；certification_status='approved'）；② 给 user 绑 `COACH` 角色（user_role_binding，已有则跳过） |
| POST | `/admin/coach-certifications/{id}/reject` | 驳回：仅记录 review_remark |

#### 3. Workshop 平台审核

补 BE-014.2 的 `independent` 教练发起 Workshop 留下的 pending 单子。

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/admin/workshops?auditStatus=pending&page&pageSize` | 工作台列表 |
| POST | `/admin/workshops/{id}/approve` | audit_status pending→approved（不改 publish_status，让商家自己点 publish） |
| POST | `/admin/workshops/{id}/reject` | audit_status pending→rejected + 写 audit_remark 字段（schema 没有？检查后定，没有就借 admin handle_result 概念用一列） |

注：schema 上 workshop 表无 audit_remark 列，平台驳回只改 audit_status；驳回理由通过 audit_log payload 记录。

#### 4. 商家扫码核销

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| POST | `/merchant/workshop-orders/{id}/checkin` | body `{ code }`：guard 校验当前操作者是 workshop.studio_id 的认领管理员；找 workshop_checkin（uk order_id），校验 code 匹配 + 时间窗（session.start-1h ~ session.end）；命中后 checkin_status='manual_checked_in'（区分用户自助 checked_in 与商家代办 manual_checked_in）+ checked_in_by_user_id=actor + sessionRepo.incrementCheckin |

复用 BE-012 的 WorkshopCheckin / WorkshopSession Repository。

#### 5. Report 工作台

| 方法 | 路径 | 行为 |
| --- | --- | --- |
| GET | `/admin/report-tickets?status=pending&targetType=&page&pageSize` | 列表 |
| POST | `/admin/report-tickets/{id}/process` | pending → processing（开始处理） |
| POST | `/admin/report-tickets/{id}/close` | processing → closed，body `{ handleResult }`。**联动**：处理结果若决定隐藏目标，目标 status 标 hidden（content_post.post_status='hidden' / review.review_status='hidden' 等多态分发） |
| POST | `/admin/report-tickets/{id}/reject` | pending|processing → rejected（驳回举报） |

#### 6. AuditLog AOP（**重头戏**）

**目标**：所有 `/merchant/**` `/admin/**` 写操作自动记录到 audit_log，无需每个 service 内显式写日志。

**设计**：

```java
// 1. 注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditAction {
    String value();              // 如 "studio.claim.approve"
    String targetType() default ""; // 如 "studio_claim"
}

// 2. 实体（jsonb payload 走 native insert，避开 JPA 映射）
@Entity @Table(name = "audit_log")
public class AuditLog { ... }

// 3. Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByActorUserIdAndActionContaining(...);  // admin 查询
}

// 4. Service：用 native query 写 jsonb
@Service
public class AuditLogService {
    @PersistenceContext EntityManager em;
    @Transactional(propagation = REQUIRES_NEW)  // 独立事务，主业务回滚不影响审计
    public void write(long actor, String action, String targetType, Long targetId,
                      Map<String, Object> payload, String result) {
        em.createNativeQuery("""
            INSERT INTO audit_log (actor_user_id, action, target_type, target_id, payload, result, created_at)
            VALUES (:actor, :action, :type, :id, CAST(:payload AS jsonb), :result, now())
            """)
            .setParameter("actor", actor)
            .setParameter("action", action)
            .setParameter("type", targetType)
            .setParameter("id", targetId)
            .setParameter("payload", toJson(payload))
            .setParameter("result", result)
            .executeUpdate();
    }
}

// 5. AOP 切面
@Aspect @Component
public class AuditLogAspect {
    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, AuditAction audit) throws Throwable {
        Long actor = CurrentUser.getIdOrNull();
        try {
            Object ret = pjp.proceed();
            // 异步或同步写日志；MVP 同步即可
            auditLogService.write(actor, audit.value(), audit.targetType(),
                extractTargetId(pjp.getArgs(), ret), buildPayload(pjp.getArgs(), ret), "success");
            return ret;
        } catch (Throwable ex) {
            auditLogService.write(actor, audit.value(), audit.targetType(),
                null, buildErrorPayload(ex), "fail");
            throw ex;
        }
    }
}
```

**应用注解**到现有 service 方法（**不要侵入 controller**）：
- `StudioClaimService.approve` → `@AuditAction(value="studio.claim.approve", targetType="studio_claim")`
- `StudioClaimService.reject` → `@AuditAction("studio.claim.reject")`
- `CoachCertificationService.approve / reject`
- `MerchantWorkshopService.create / publish / offline`
- `MerchantWorkshopCheckinService.checkin`
- `AdminWorkshopService.approve / reject`
- `ReviewAppealService.approve / reject`
- `ReportTicketService.process / close / reject`
- `CoachRelationService.invite / update`

**测试要点**：
- AuditLog 实体单元测试（构造 + Getter）
- AuditLogService.write 走 H2 内存表（建议 @SpringBootTest 一个 IT 类专门测，或用 `@DataJpaTest` 跑 native insert；jsonb 在 H2 用 `MODE=PostgreSQL` 也写不进，可以把 payload 列在测试库改成 text + JdbcTypeCode SqlTypes.LONGVARCHAR 临时替代）
- AuditLogAspect 切面绑定测试：建一个小的 @Configuration + @Component 测试目标方法，验证调用后 AuditLog 行确实写入

**Tips**：jsonb 测试踩坑率高，建议本期 AOP 切面默认 try-catch 包住 audit 写入失败（log.warn 不抛），保证业务主流程不受审计模块影响。

### 接口清单（BE-014.3）

约 14 个新端点：

```
GET    /h5/coach/me
PUT    /h5/coach/me/profile
GET    /h5/coach/dashboard
GET    /h5/coach/courses
GET    /h5/coach/workshops

POST   /h5/coach/certifications
GET    /h5/coach/certifications/mine
GET    /admin/coach-certifications
POST   /admin/coach-certifications/{id}/approve|reject

GET    /admin/workshops?auditStatus=
POST   /admin/workshops/{id}/approve|reject

POST   /merchant/workshop-orders/{id}/checkin

GET    /admin/report-tickets
POST   /admin/report-tickets/{id}/process|close|reject

GET    /admin/audit-log?actor=&action=&from=&to=&page=&pageSize=
```

### 测试要点（至少 25 例）

- CoachOpsControllerTest 6：me 有/无认证、profile 更新 / 越权（不是教练）、courses 列表、workshops 列表
- CoachCertificationControllerTest 6：submit OK / duplicated / mine / admin 列表 / approve 自动建 coach 行 + 绑角色 / reject
- AdminWorkshopControllerTest 4：list pending / approve / reject / 状态冲突
- MerchantWorkshopCheckinControllerTest 4：checkin OK / 时间太早 / 越权 FORBIDDEN / code 错误
- ReportTicketControllerTest 4：list / process / close + 联动 hidden / reject
- AuditLogTest 3：切面绑定测试 + 写入成功 + 失败降级（不抛业务异常）

### 跨模块依赖

- catalog/CoachRepository、catalog/CourseRepository、catalog/CourseScheduleRepository
- workshop/WorkshopRepository、WorkshopSessionRepository、WorkshopCheckinRepository、WorkshopOrderRepository
- review/ReviewRepository、ReviewReplyRepository
- iam/AppUserRepository、UserRoleBindingRepository
- merchant/MerchantAccessGuard

### 与前端的契约对齐

打开 `build/frontend/src/api/coach.ts` 与 `build/frontend/src/api/coachOps.ts`，里面已经有所有这些 URL 与字段。后端实现必须一字不差对齐。

### 留待 BE-015 的事

- AuditLog 异步化（@Async + 线程池）
- AuditLog 真正用 jsonb 替代 text 占位
- Report 工作台的"批量处理"
- Workshop 驳回理由结构化存储（schema 需要 alter table 加列，或全部走 audit_log payload）

---

## BE-015 ｜ 部署 + 徽章规则引擎 + CI + 缓存 + 收尾

### 目标

把工程从"能跑 + 能单测"升级到"能部署 + 能联调 + 能持续集成"。这是 MVP 验收的最后一关。

预计加 **~10-15** 测试 + CI 脚本 + 多份配置文件，目标 `mvn test` 在 **225+ PASS**。

### 15.1 徽章发放规则引擎（M5 BE-010 留口）

**位置**：`com.bitdance.growth.badge`

**目标**：BE-010 已经写了 `GrowthBadge` 实体与 BadgeRepository 的读 API，但发放路径是空的。本期补规则驱动的发放引擎。

**Schema**（schema 已建 `badge_definition` 表，查 schema.sql）：

```sql
badge_definition (
  id, code uk, name, description, icon_asset_id,
  rule_type chk in ('checkin_streak', 'first_review', 'first_practice_completed', ...),
  rule_config jsonb,         -- 如 {"days": 7} 表示连续打卡 7 天
  badge_level smallint,
  status chk active/inactive
)

growth_badge (
  id, user_id, badge_id, granted_at,
  uk(user_id, badge_id)      -- 同一用户同一徽章只授一次
)
```

**设计**：

```java
public interface BadgeRule {
    String type();              // 与 badge_definition.rule_type 对齐
    boolean shouldGrant(Long userId, BadgeContext ctx, JsonNode config);
}

@Component class CheckinStreakRule implements BadgeRule { ... }
@Component class FirstReviewRule implements BadgeRule { ... }
@Component class FirstPracticeCompletedRule implements BadgeRule { ... }
@Component class FirstWorkPublishedRule implements BadgeRule { ... }
@Component class FirstWorkshopAttendedRule implements BadgeRule { ... }

@Service
public class BadgeRuleEngine {
    private final Map<String, BadgeRule> rules;       // 由 Spring 自动注入 Map<String,BadgeRule>
    private final BadgeDefinitionRepository defRepo;
    private final GrowthBadgeRepository badgeRepo;

    @Transactional
    public List<Long> evaluate(Long userId, String eventType) {
        List<Long> granted = new ArrayList<>();
        // 拉所有 active badge_definition；按 rule_type 筛选与 eventType 相关的
        // 对每条 def 调对应 rule.shouldGrant；命中且 growth_badge 未授予则 grant + 推送 notification
        return granted;
    }
}
```

**触发点接入**：

- `GrowthService.createCheckin` 调用 `badgeRuleEngine.evaluate(userId, "checkin")`
- `ReviewService.create` 调用 `badgeRuleEngine.evaluate(userId, "review")`
- `BuddyService.rate` 调用 `badgeRuleEngine.evaluate(userId, "practice_completed")`
- `GrowthWorksService.create` 调用 `badgeRuleEngine.evaluate(userId, "work_published")`
- `WorkshopService.checkin` 调用 `badgeRuleEngine.evaluate(userId, "workshop_attended")`

**测试要点**：
- 每条规则正反例至少 2 例
- 重复授予幂等（uk 防）
- 引擎按 eventType 分发正确

### 15.2 Redis 缓存接入

**目标**：BE-002 的 SmsCodeService 已经用过 StringRedisTemplate；其他热点接口本期接入。

**接入清单**：

| 接口 | Key | TTL | 失效触发 |
| --- | --- | --- | --- |
| `GET /public/studios/{id}` | `catalog:studio:detail:{id}` | 10m | StudioService.update（BE-014.x 若做编辑接口）/ favorite toggle |
| `GET /public/reviews/summary` | `review:summary:{type}:{id}` | 10m | ReviewService.create / delete / appeal approve |
| `GET /public/practices`（约练广场） | `practice:square:{cityId}:{styleId}` | 60s | PracticeService.create / cancel |
| `GET /public/community/feed?scope=recommend` | `community:feed:recommend:{cityId}:{styleId}:{page}` | 30s | postRepo write 触发整段失效 |

实现方式：在对应 Service 方法上加 `@Cacheable` / `@CacheEvict`（需要 `@EnableCaching` + RedisCacheManager bean）。

**测试**：用 Testcontainers 起 Redis 7 容器跑 IT；或简化做 mock RedisTemplate 验证 key 命中。

### 15.3 OpenAPI 导出

- SpringDoc 2.6 已引入，`/api/swagger-ui.html` 已可访问
- 给每个 Controller 加 `@Tag(name="M2 Review")` 等分组
- 启动后 curl `http://localhost:8080/api/v3/api-docs > build/docs/openapi.json` 并入库（用于前端 axios 类型生成或 Postman 导入）
- 加 `springdoc.api-docs.groups`：`public` / `h5` / `merchant` / `admin` 四组

### 15.4 Dockerfile + docker-compose

**backend/Dockerfile**：

```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/bitdance-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseG1GC","-Xms512m","-Xmx1g","-jar","/app/app.jar"]
```

**docker-compose.yml**（仓库根放）：

```yaml
services:
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
  backend:
    build: ./build/backend
    depends_on:
      redis:
        condition: service_healthy
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: ${PG_URL}
      SPRING_DATASOURCE_USERNAME: ${PG_USER}
      SPRING_DATASOURCE_PASSWORD: ${PG_PWD}
      BITDANCE_JWT_SECRET: ${JWT_SECRET}
      SPRING_REDIS_HOST: redis
  frontend:
    build: ./build/frontend
    ports: ["8081:80"]
```

**frontend/Dockerfile**：

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

`nginx.conf`：标准 SPA fallback 到 `index.html`，`/api/` 反代到 `backend:8080`。

### 15.5 GitHub Actions CI

`.github/workflows/ci.yml`：

```yaml
name: ci
on:
  push: { branches: [master] }
  pull_request: { branches: [master] }

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin', cache: maven }
      - run: cd build/backend && mvn -B -ntp test
  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20', cache: 'npm', cache-dependency-path: build/frontend/package-lock.json }
      - run: cd build/frontend && npm ci && npm run build
```

push / PR 任一失败 fail-fast。这是把 SKILL 5.3 节 Tester 闭环固化到 CI 的最后一步。

### 15.6 application-prod.yml + 凭证

**backend/src/main/resources/application-prod.yml**：

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate.ddl-auto: validate
  data.redis:
    host: ${SPRING_REDIS_HOST}
    port: ${SPRING_REDIS_PORT:6379}
bitdance:
  jwt.secret: ${BITDANCE_JWT_SECRET}
  sms:
    mock: false
logging.level.com.bitdance: WARN
```

**README 增加"部署清单"章节**：
- 数据库 schema 执行：`psql -f bitdance_postgresql_schema.sql`
- 环境变量列表
- docker-compose up -d
- 健康检查：`/api/actuator/health`

### 15.7 真支付 Gateway 切换接口预留

BE-012 已经把 MockPaymentGateway 抽到 `PaymentGateway` 接口。本期：

```java
@ConditionalOnProperty(name = "bitdance.payment.provider", havingValue = "wechat")
@Component
public class WechatPayGateway implements PaymentGateway {
    @Override public String charge(WorkshopOrder order) {
        // TODO: 接入微信支付 API
        throw new UnsupportedOperationException("WechatPay not implemented yet");
    }
}

@ConditionalOnMissingBean(PaymentGateway.class)
@Component
public class MockPaymentGateway implements PaymentGateway { ... }    // 已有
```

application.yml 默认走 mock，application-prod.yml 设 `bitdance.payment.provider=wechat`。真实接入留到产品决策后做。

### 15.8 unpaid 30 分钟自动关单（定时任务）

```java
@Component
public class CloseUnpaidWorkshopOrderJob {
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void run() {
        // SELECT id FROM workshop_order WHERE order_status='pending_payment' AND created_at < now() - interval '30 minutes'
        // → 批量 UPDATE order_status='canceled', canceled_at=now()
    }
}
```

### 15.9 性能与索引复核

跑 `EXPLAIN ANALYZE` 对热点查询验证索引：

- `studio` 表：复合索引 `(city_id, status)`、`(geo_hash, status)`
- `practice_post` 表：`(city_id, post_status, start_at)` 用于约练广场
- `content_post` 表：`(post_status, published_at desc)` 用于 feed
- `review` 表：`(target_type, target_id, review_status, published_at)`
- `workshop_session` 表：`(workshop_id, start_at)`
- `notification` 表：`(user_id, is_read, id desc)`

**只允许新增索引**，schema.sql 末尾追加 `CREATE INDEX IF NOT EXISTS ...`。

### 15.10 收官清单（W5 验收门禁）

- [ ] `mvn test` 全绿，覆盖率 ≥ 60%（如有要求）
- [ ] `npm run build` 走通
- [ ] CI master 最新 commit 通过
- [ ] `开发历史记录.md` 收录 BE-001~015 全部日志 + v0.5.0 变更日志
- [ ] `docker-compose up` 起来后 `curl http://localhost:8080/api/actuator/health` 返回 UP
- [ ] OpenAPI 在 `build/docs/openapi.json` 可访问
- [ ] `grep -rn "shixun123\|BitDance.*password" build/` 不命中任何源码

### BE-015 测试要点

- BadgeRuleEngine 单元测试：每条规则正反例 2 例（约 10 例）
- BadgeRuleEngine 触发点接入测试：在 GrowthService.createCheckin 调用 evaluate 后断言徽章授予
- Redis 缓存命中测试：用 SpyBean 验证第 2 次调用未走 Repository
- 定时任务 CloseUnpaidWorkshopOrderJob 单元测试
- WechatPayGateway 配置切换测试

---

## 2. 给下一棒的实操建议

### 2.1 启动检查

```bash
cd build/backend && mvn -q test    # 必须 186/186 PASS
git log --oneline -3                # 最新应是 4e4d9f9 docs: BE-014.2 同步
```

### 2.2 推进顺序建议

BE-014.3 拆 2 个 commit 更稳：
- **BE-014.3-a**：Coach Ops + Coach Certification（约 12 例）
- **BE-014.3-b**：Admin Workshop 审核 + 商家核销 + Report 工作台 + AuditLog AOP（约 15 例）

BE-015 拆 3 个 commit：
- **BE-015-a**：徽章规则引擎 + 触发点接入（约 10 例）
- **BE-015-b**：Redis 缓存 + OpenAPI 分组 + 定时关单
- **BE-015-c**：Dockerfile + docker-compose + CI + application-prod + README 部署清单

每个 commit 独立 `mvn test` 必绿，每个 commit 同 turn 补 `开发历史记录.md` 日志。

### 2.3 出错时的排查路径

`mvn test` 挂了：

```bash
mvn test 2>&1 | grep -E "Caused by|cannot find|Tests run" | head -10
```

最常见错误（已经在 BE-001/002 / 011 踩过）：
- `Cannot load driver class: org.h2.Driver` → `pom.xml` 漏 h2 test scope
- `No qualifying bean 'JwtService'` → 测试漏 `@MockBean JwtService`
- `ApplicationContext failure threshold exceeded` → 改 `@WebMvcTest(controllers=Xxx.class, excludeAutoConfiguration=SecurityAutoConfiguration.class)`
- jsonb 字段 H2 不识别 → 在测试库 schema 用 text 占位 或在实体上 `insertable=false, updatable=false`，native query 写入

### 2.4 与前端的契约对齐

每个 BE-014.3 子段开始前 grep 一下前端：

```bash
ls build/frontend/src/api/
# coach.ts coachOps.ts community.ts workshop.ts ...
```

前端 `src/api/coachOps.ts` 已经定义了所有 dashboard / 申诉 / 创建 Workshop / 核销的 URL 与字段，后端必须一字不差对齐——前端只需把 `VITE_USE_MOCK=false` 就能联调。

### 2.5 联系上下文

- 远端：`https://github.com/Asnowww/BitDance.git`
- 分支：`master`（不开 feature 分支）
- 历史：`git log --oneline` 看到 60+ 条提交
- 数据库：SKILL 第 3 节有凭证（云库 98.142.241.155，PostgreSQL，schema=bitdance）
- 团队：第五组，组长顾远

按本文档执行不会偏离 SKILL 与团队约定。**最重要的一条**：mvn test 必绿 → commit 代码 → 同 turn 内补 `开发历史记录.md` → 再 commit + push。这条纪律在 BE-001 与前端 FP 阶段各违反过一次，代价是返工。不要再犯。

祝顺利收官。
