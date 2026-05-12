# BitDance 后端开发交接文档（BE-011 ~ BE-015）

> 写作时间：2026-05-12
> 写作目的：上一位执行者（Claude）额度用尽，把 W4/W5 剩余 5 个功能点的开发目标、契约、约束、测试方式、流程纪律一次性交底，让下一位接手者无需回看对话历史即可上手。
> 适用对象：任何能读 Java/Spring Boot 3 + JPA + Spring Security + JJWT 的 AI 或人类开发者。

---

## 0. 上手前必读（共同前置）

### 0.1 项目结构

```
build/
  frontend/                       Vue 3 + Vite + TS H5 工程（已完工，FP-001~048）
  backend/                        Spring Boot 3 + Java 21 工程（本文档范围）
    pom.xml
    src/main/java/com/bitdance/
      BitDanceApplication.java
      common/{web,exception,audit,config}        统一返回、异常、TraceId、JPA Auditing、CORS
      iam/{domain,repository,service,controller,jwt,security,dto}
                                                 M3：AppUser、UserRoleBinding、SmsCodeService、JwtService、JwtAuthFilter、SecurityConfig、CurrentUser
      profile/                                    M3 Profile + Privacy + DancePreference
      message/                                    M3 Notification
      catalog/                                    M1 Studio/Coach/Course/Schedule
      booking/                                    M1 TrialBooking 用户侧（商家侧 confirm/reject/attend 留 BE-014）
      review/                                     M2 Review + 风控权重
      practice/                                   M4 Practice（搭子/互评留 BE-013）
      growth/                                     M5 Growth
      favorite/                                   多态收藏 Favorite
    src/main/resources/
      application.yml                             默认配置，context-path=/api、Asia/Shanghai、JPA validate-only
      application-local.yml.example               本地凭证模板（实际 application-local.yml 已 gitignore）
    src/test/
      java/com/bitdance/*/                        @WebMvcTest 切片测试，已 83/83 PASS
      resources/application-test.yml              H2 测试库

  bitdance_postgresql_schema.sql                  schema 唯一权威，行号见各 BE-* 章节
  BitDance_architecture_mvp.md                    架构与端侧落位
  BitDance_后端代码规范文档.md                    分层/命名/事务/异常/审计/日志规则
  开发历史记录.md                                 SKILL 强制：每个 BE-* 必须同 turn 内追加日志
  docs/backend-handoff-BE011-BE015.md             本文档
  .claude/skills/bitdance-dev/SKILL.md            协作 SKILL，包含禁区清单
```

### 0.2 已完工功能与累计测试

| BE | 模块 | commit | tests |
| --- | --- | --- | --- |
| BE-001/002 | 工程骨架 + M3 验证码登录 + JWT | `cf77033` + `6ec75e6` | 4 |
| BE-003 | M3 Profile/Privacy/Preference | `ae542ef` 一部分 | 4 |
| BE-004 | M3 Message | `ae542ef` 一部分 | 4 |
| BE-005 | M1 Studio 附近搜索 + Favorite | `b67b34b` | 10 |
| BE-006 | M1 Coach/Course/Schedule | `99d193f` | 9 |
| BE-007 | M1 Trial Booking 用户侧 | `0e3896d` | 9 |
| BE-008 | M2 Review + 风控权重 | `c497807` | 9 |
| BE-009 | M4 Practice 主流程 + 过期定时 | `9e9384c` | 17 |
| BE-010 | M5 Growth | `183ff71` | 17 |

`mvn test` 当前 **83/83 PASS**。

### 0.3 流程纪律（**违反会被回滚**）

按 SKILL `bitdance-dev` 第 5.3 + 8 节，每个 BE-* 必须按下列顺序在同一开发轮次内完成：

1. 写代码（实体/Repository/Service/DTO/Controller/Test）
2. 本地执行 `mvn test`，**必须 BUILD SUCCESS**；不通过先修复，不允许 push
3. `git add build/backend && git commit -m "feat(be): BE-XXX ..."`，提交信息必须包含：
   - 实体与字段说明
   - Service 关键状态流转
   - 接口清单
   - 测试覆盖项
   - `mvn test → Tests run: N, Failures: 0, Errors: 0, BUILD SUCCESS`
   - `Closes #BE-XXX`
4. `git push origin master`
5. **同一轮次内**追加 `开发历史记录.md`：
   - 在"开发日志"区块顶部插入一条新条目（按 SKILL 第 8 节六元格式：目标与背景 / 改动范围 / 关键问题与解决 / 方案选择理由 / 待办与遗留 / 关联）
   - 在 v0.4.0 变更日志下追加 BE-XXX 一行
   - 更新"当前上下文快照"
   - `git commit -m "docs: BE-XXX 开发日志同步（N/N PASS、关键设计要点）"` 然后 push
6. 不要把代码 commit 和日志 commit 合到一起；分开 push 方便回滚

**禁忌**：
- 不要写测试但不跑 `mvn test`；BE-002 第一次就这么挂了（H2 驱动缺 + 整栈拉起）
- 不要在 commit message 或日志里出现 `Claude`、`ChatGPT`、`Anthropic`、`OpenAI`、`Copilot`、`Cursor`、`AI`、`LLM`、`agent` 等字样（SKILL 第 7 节硬要求）
- 不要修改 schema 中已使用表的字段类型；只允许新增字段或新增表
- 不要 push `.env*`、`application-local.yml`、`.claude/`、JWT secret 等敏感文件
- 不要使用 `git push --force` 推 master

### 0.4 测试基础设施

所有 Controller 测试统一用切片模式：

```java
@WebMvcTest(controllers = XxxController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class XxxControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean XxxService service;
    @MockBean JwtService jwtService;          // 必须 mock，否则 JwtAuthFilter 会找不到 bean

    @BeforeEach
    void stubJwt() {
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("42");
        when(claims.getOrDefault(eq("roles"), any())).thenReturn(List.of("USER"));
        when(jwtService.parse(any())).thenReturn(claims);
    }
    // 用例需带 .header("Authorization", "Bearer fake") 才能让 JwtAuthFilter 解析出 userId=42
}
```

H2 内存库无法跑 `fn_haversine_km` / `fn_close_expired_practice_posts` / jsonb / text[] 等 PostgreSQL 专属能力。所有依赖这些能力的 Service 逻辑都用 `@MockBean` 在 Controller 层验证形态，真值留到能连云库时跑集成测试。

### 0.5 通用约定

- 返回包装：所有接口走 `com.bitdance.common.web.ApiResponse.ok(...)` 或 `.fail(code, msg)`
- 异常：业务错误 throw `BizException("ERR_CODE", "中文消息")` → `GlobalExceptionHandler` 包装成 400 + `{ code: "ERR_CODE" }`
- 鉴权：用 `CurrentUser.getId()`（强制登录）或 `CurrentUser.getIdOrNull()`（公开接口选填用户态）
- 接口前缀：`/public/**` 公开 · `/auth/**` 登录 · `/h5/**` 用户端鉴权 · `/merchant/**` 舞室管理员（hasAnyRole STUDIO_ADMIN / PLATFORM_ADMIN）· `/admin/**` 平台管理员（hasRole PLATFORM_ADMIN）· `/callback/**` 第三方回调
- 时间字段：JPA 实体里 `created_at` 用 `insertable=false, updatable=false` 让数据库 default now() 兜底；服务端写入时用 `OffsetDateTime.now()`
- 实体不要用 Lombok `@Data`；schema 已定义所有字段，逐字段写 getter/setter，避免 jsonb / text[] 等特殊列误生成 setter
- 多态字段（target_type+target_id）：targetType 校验集合必须与 schema CHECK 约束完全一致

### 0.6 Git 工作流

```bash
# 远端
git remote -v
# origin  https://github.com/Asnowww/BitDance.git (fetch/push)

# 分支
git branch
# * master   ← 一切提交都打到这条

# commit + push 标准流程
cd "C:/Users/patri/OneDrive - bjtu.edu.cn/Files/实训IV/"
git add build/backend
git commit -m "feat(be): BE-XXX ..."
git push origin master
```

如果工作目录在 `build/` 下，git 还是会找到 `实训IV/.git`，路径要从仓库根写。

---

## BE-011 ｜ M6 社区动态主流程

### 目标

把前端社区 FP-033/034/035/036/037/038 的 mock 数据替换为真后端：动态发布、推荐/关注双 tab feed、详情互动（点赞/收藏/评论/举报）、话题聚合、关注/粉丝、内容搜索。

### Schema 表（schema.sql 行号）

| 表 | 行 | 关键字段与约束 |
| --- | --- | --- |
| `content_post` | 1336 | id、user_id、target_type/target_id（多态，可挂 studio/course/coach/workshop/practice_post）、post_type、content_text、location_name、longitude/latitude、cover_asset_id、`publish_status` chk in ('draft','published','folded','hidden')、`risk_level` smallint、view_count/like_count/comment_count/share_count/favorite_count、`is_pinned`、published_at |
| `content_post_topic` | 1377 | post_id + topic_id 双主键 |
| `content_comment` | 1396 | id、post_id、user_id、parent_comment_id（楼中楼）、content_text、`comment_status` chk in ('published','folded','hidden')、like_count、created_at |
| `content_like` | 1423 | id、user_id、target_type chk in ('content_post','content_comment')、target_id、uk(user, target_type, target_id) |
| `topic_tag` | 1311 | id、topic_code、topic_name、`status` chk in ('active','inactive')、post_count、related_dance_style_id |
| `follow_relation` | 1458 | id、follower_user_id、followee_user_id、`relation_status` chk in ('active','muted','blocked')、uk(follower, followee) |
| `report_ticket` | 1500 | 多态举报，target_type chk in ('content_post','content_comment','review','practice_post','app_user','workshop','studio','coach')、`report_status` chk in ('pending','processing','closed','rejected')、reason_code |
| `user_block_relation` | 1570 | 拉黑关系，blocker + blocked，可用于 feed 过滤 |

### 实体清单

- `com.bitdance.community.domain.ContentPost`
- `com.bitdance.community.domain.ContentComment`
- `com.bitdance.community.domain.ContentLike`
- `com.bitdance.community.domain.TopicTag`
- `com.bitdance.community.domain.ContentPostTopic`（双主键 IdClass）
- `com.bitdance.community.domain.FollowRelation`
- `com.bitdance.community.domain.ReportTicket`（多态，社区 + 后续 BE-014 复用）

### 接口契约（与前端 `frontend/src/api/community.ts` 已有签名对齐）

```
POST   /h5/community/posts          创建动态
GET    /public/community/feed?scope=recommend|follow&topic=&style=&page=&pageSize=
GET    /public/community/posts/{id} 详情
DELETE /h5/community/posts/{id}     删除自己的动态（软删，置 publish_status='hidden'）
POST   /h5/community/posts/{id}/like     toggle like
POST   /h5/community/posts/{id}/collect  toggle collect → 复用 favorite 表 (target_type='content_post')
POST   /h5/community/posts/{id}/report   举报，写 report_ticket
GET    /public/community/posts/{id}/comments
POST   /h5/community/posts/{id}/comments 发评论
GET    /public/community/topics          话题广场，按 post_count desc，hot=count>=4
GET    /public/community/topics/{name}/posts  话题详情下的动态
POST   /h5/community/follow/{userId}     toggle follow
GET    /h5/community/follow/me           我关注的人列表
GET    /public/community/search?q=
```

### 关键业务规则

1. **发布**：`publish_status='published'`、`risk_level` 用与 BE-008 类似的简化算法（账号龄 < 7 天 risk+1、相似文案占位 TODO）；`content_text` 长度 1-5000；最多 3 个话题
2. **feed scope=follow**：JOIN `follow_relation` 过滤 followee_user_id IN (我关注的) AND relation_status='active'
3. **feed scope=recommend**：先按 `published_at desc` + 随机权重；后续接推荐算法
4. **拉黑过滤**：feed 都要 `WHERE post.user_id NOT IN (我拉黑的 + 拉黑我的)`
5. **点赞**：用 `content_like` 表（不是 favorite），like_count 字段写在 content_post 上做反向计数（注意并发：用 `@Modifying UPDATE content_post SET like_count = like_count + 1`）
6. **收藏**：复用 BE-005 的 `favorite` 表，target_type='content_post'
7. **评论计数**：与 like 同样的反向计数模式
8. **举报**：写 `report_ticket`，report_status='pending'；BE-014 平台后台处理

### 测试要点（至少 15 例）

- 发布成功（published）+ 文本过长 INVALID_ARGUMENT + 话题数>3 INVALID_ARGUMENT
- feed scope=recommend / scope=follow 各一例
- 详情返回 + 不存在 POST_NOT_FOUND
- 删除自己 + 删除他人 FORBIDDEN
- 点赞 / 取消点赞 / 重复点赞幂等
- 评论发布 + 评论列表
- 举报成功
- 话题广场列表 + 话题详情
- 关注 / 取关 + 关注列表
- 搜索命中 + 无结果
- 未登录 UNAUTHORIZED

### 跨模块依赖

- `favorite` 表（BE-005）已存在，直接复用做"收藏动态"
- `report_ticket` 是多态举报，本期由 BE-011 创建表对应实体；BE-014 商家/平台后台的举报处理会复用

### 待办与边界

- 楼中楼评论 `parent_comment_id`：本期只支持一级评论，二级折叠展示由 BE-014 优化
- 风控相似文案：留 TODO 注释
- view_count 累加：用 Redis 计数 + 定时回写策略；本期可以先简单 `UPDATE ... SET view_count = view_count + 1`，BE-015 优化

---

## BE-012 ｜ M6 Workshop 报名支付闭环

### 目标

Workshop 完整业务链路：浏览 → 详情 → 选场次 → 报名生单 → 支付 → 签到 → 复盘。前端 FP-027~031 已 mock 完工。

### Schema 表

| 表 | 行 | 关键字段 |
| --- | --- | --- |
| `workshop` | 1081 | id、creator_user_id、studio_id、coach_id、city_id、dance_style_id、workshop_name、cover_asset_id、`publish_status` chk in ('draft','pending_review','published','offline','rejected')、price_amount、currency、target_audience text[]、description、created_at |
| `workshop_session` | 1130 | id、workshop_id、session_name、start_at、end_at、capacity、registered_count、`session_status` chk in ('scheduled','completed','canceled')、location_name |
| `workshop_order` | 1156 | id、workshop_id、workshop_session_id、buyer_user_id、order_no（uk）、price_amount、`order_status` chk in ('unpaid','paid','canceled','refunded','closed')、idempotency_token（防重复）、paid_at、closed_at |
| `workshop_checkin` | 1190 | id、order_id（uk）、workshop_session_id、checkin_code、checked_in_at |
| `workshop_checkin_ticket` | 1714 | 一次性签到码记录 |

### 实体清单

`com.bitdance.workshop.domain.{Workshop, WorkshopSession, WorkshopOrder, WorkshopCheckin, WorkshopCheckinTicket}`

支付相关：本期实现 **Mock 支付适配器**，即 `POST /workshop-orders/{id}/pay` 直接把 order_status 改 paid 并占座，不接真微信支付。真支付留到上线前再补，留好 `PaymentGateway` 接口抽象。

### 接口契约

```
GET    /public/workshops?cityId=&styleId=&page=&pageSize=   列表
GET    /public/workshops/{id}                                详情（含 sessions、past reviews 摘要）
POST   /h5/workshop-orders                                   下单 body { workshopId, sessionId, idempotencyToken }
POST   /h5/workshop-orders/{id}/pay                          支付（mock 直接成功）
POST   /h5/workshop-orders/{id}/cancel                       取消（仅 unpaid）
POST   /h5/workshop-orders/{id}/refund                       退款（paid → refunded，释放座位）
GET    /h5/workshop-orders/mine                              我的订单
POST   /h5/workshop-orders/{id}/checkin                      用户扫码签到 body { code }
GET    /h5/workshop-orders/{id}/checkin-code                 获取签到码（生成 + Redis 短期）
```

### 关键业务规则

1. **下单**：用 `idempotency_token` 防重复（Redis SETNX `idem:workshop-order:{token}`，10 分钟 TTL）；校验 session 未满（registered_count < capacity）+ 未截止；session.registered_count 暂不递增，等支付成功才占座
2. **支付**：mock 适配器，order_status unpaid→paid + 生成签到码 + session.registered_count+1；并发用 `UPDATE workshop_session SET registered_count = registered_count + 1 WHERE id = ? AND registered_count < capacity` 原子保护
3. **取消**：仅 unpaid 状态可取消；paid 走退款流程
4. **退款**：paid → refunded + session.registered_count - 1
5. **签到**：用户扫码或手输 code → 校验等于 order.checkin_code + session_status=scheduled + 时间在 session 区间内
6. **签到码**：order 创建时不生成，paid 成功才生成 6-12 位随机串，写入 workshop_order.checkin_code 与 workshop_checkin_ticket

### 测试要点（至少 15 例）

- 列表 / 详情 / 详情不存在
- 下单成功 + 满员 WORKSHOP_FULL + 重复 idempotency_token 直接返回原订单 + 截止时间已过
- 支付成功（状态 paid + 签到码生成）
- 取消（unpaid 成功 / paid 走拒绝 → 引导退款）
- 退款（paid → refunded + 座位释放）
- 我的订单
- 签到成功 + 签到码错误 + 重复签到
- 未登录 UNAUTHORIZED

### 跨模块依赖

- `coach` 表（BE-006）：详情聚合教练公开摘要
- `studio` 表（BE-005）：详情聚合舞室公开摘要
- `review` 表（BE-008）：往期评价摘要，target_type='workshop' 走 ReviewService.summary

### 待办

- 真支付：留 `PaymentGateway` 接口，本期 `MockPaymentGateway` 实现，BE-015 部署时换 `WechatPayGateway`
- 订单关单定时任务：unpaid 超 30 分钟自动 closed
- 退款回调：等接真微信支付再做
- Workshop 创建侧（教练发起）：BE-014

---

## BE-013 ｜ M4 Buddy 互评 + 搭子关系沉淀

### 目标

把 BE-009 Practice 状态机里留下的"约练完成后双向评价 + 自动建立搭子关系"补齐。

### Schema 表

| 表 | 行 | 关键字段 |
| --- | --- | --- |
| `buddy_relation` | 866 | id、user_id_low、user_id_high（chk low<high）、source_practice_post_id、`relation_status` chk in ('active','blocked','inactive')、uk(low, high) |
| `practice_rating` | 892 | id、practice_post_id、rater_user_id、rated_user_id、punctuality_score 1-5、friendliness_score 1-5、level_match_score 1-5、comment_text、uk(post_id, rater, rated)、created_at |

注意：`buddy_relation` 用**有序双键**存无向关系，写入时必须保证 user_id_low < user_id_high。Schema 中有 `fn_normalize_buddy_relation_pair()` 触发器（schema.sql 2260 行）自动规范化；但 JPA 写入时还是建议在 service 里就排好序。

### 实体清单

`com.bitdance.buddy.domain.{BuddyRelation, PracticeRating}`

### 接口契约

```
POST   /h5/practices/{postId}/ratings        提交约练后评价 body { ratedUserId, punctuality, friendliness, levelMatch, comment }
GET    /h5/buddies                            我的搭子列表
POST   /h5/buddies/{userId}/block            拉黑（status: active → blocked）
DELETE /h5/buddies/{userId}                   解除（status → inactive）
GET    /h5/practices/{postId}/ratings         约练评价列表（仅参与者可看）
```

### 关键业务规则

1. **提交评价**：
   - 校验 post.postStatus ∈ {confirmed, completed}
   - 校验 currentUser 与 ratedUser 都参与了该约练（creator 或 join_request.applicant 且 join_status='accepted'）
   - uk(post_id, rater, rated) 防重复
   - 写完判定：若双方都已评价过对方 → 触发自动创建 buddy_relation（status='active'），同时把 post.postStatus 转 completed
2. **buddy_relation 写入**：service 层先 sort(userA, userB) 保证 low<high；用 ON CONFLICT DO NOTHING 风格（JPA：findByUkOrInsert）
3. **拉黑**：buddy_relation.status='blocked'；连带在 practice 广场过滤（BE-009 已留挂载点）+ community feed 过滤（BE-011 拉黑列表使用）
4. **practice_post → completed**：双向评价完成自动转 completed；也支持 creator 主动标记 completed（schema 已留 completed 状态）

### 测试要点（至少 8 例）

- 评价成功（双方都未评过）
- 评价后对方也评 → 自动建立 buddy_relation
- 双方评价后 practice_post 转 completed
- 重复评价 RATING_DUPLICATED
- 非参与者评价 FORBIDDEN
- 拉黑搭子（active → blocked）
- 解除搭子（→ inactive）
- 评价列表（仅参与者）

### 跨模块依赖

- `practice_post` + `practice_join_request`（BE-009）：必须能查参与者
- `community.feed` 与 `practice.square`（BE-009、BE-011）：要在查询时过滤 buddy_relation.status='blocked' 的用户

### 待办

- 与 BE-011 拉黑联动：BE-011 完成时若 BE-013 还没做，先用 `user_block_relation` 表（schema.sql 1570 行）；BE-013 完成后两者合并语义
- BE-009 的 `practice_post.cancel_limit_hours` 联动 + 爽约惩罚留到阶段 D，不在本期范围

---

## BE-014 ｜ M7 商家与教练运营侧 + 平台后台

### 目标

把所有需要"商家管理员"或"教练身份"或"平台管理员"才能调用的接口一次性补齐。涉及 7+ 个子域，建议按子模块拆 commit，但仍属同一个 BE-* 序号。

### Schema 表

| 表 | 行 | 用途 |
| --- | --- | --- |
| `studio_claim` | 398 | 舞室认领申请，`claim_status` pending/approved/rejected |
| `coach_certification_application` | 476 | 教练资质审核，application_status pending/approved/rejected |
| `studio_coach_relation` | 502 | 教练与舞室关系，`relation_type` full_time/contracted/freelance、`relation_status` active/pending/ended |
| `review_reply` | 742 | 商家/教练对评价的回复 |
| `review_appeal` | 763 | 申诉，`appeal_status` pending/approved/rejected |
| `audit_log` | 1527 | 后台关键操作审计 |

### 子模块清单

1. **Merchant 舞室管理员**（`/merchant/**`）
   - `POST /merchant/studio-claims` 提交认领
   - `GET /merchant/studio-claims/mine` 我的认领进度
   - `PUT /merchant/studios/{id}` 编辑已认领舞室信息
   - `POST/PUT/DELETE /merchant/courses` 课程 CRUD
   - `POST/PUT/DELETE /merchant/course-schedules` 课表 CRUD
   - `POST /merchant/coach-invitations` 邀请教练绑定
   - `PUT /merchant/coach-relations/{id}` 调整关系类型/有效期

2. **Trial 商家侧**（补 BE-007 留口）
   - `POST /merchant/trial-bookings/{id}/confirm`
   - `POST /merchant/trial-bookings/{id}/reject`
   - `POST /merchant/trial-bookings/{id}/attend`
   - `POST /merchant/trial-bookings/{id}/no-show`
   - 状态机：pending → confirmed → attended | no_show；pending → rejected

3. **Workshop 创建侧**（补 BE-012 留口）
   - `POST /merchant/workshops` 提交（独立教练走审核流，签约教练直发）
   - `PUT /merchant/workshops/{id}` 编辑
   - `POST /merchant/workshops/{id}/publish` 上架
   - `POST /merchant/workshops/{id}/offline` 下架
   - `POST /merchant/workshop-sessions` 增加场次
   - `POST /merchant/workshop-orders/{id}/checkin` 商家扫码核销（与 BE-012 用户侧签到对称）

4. **Coach Ops 教练运营**（角色 COACH，路径仍 `/h5/coach/**`）
   - `GET /h5/coach/me` 我的教练身份摘要
   - `PUT /h5/coach/me/profile` 编辑教练公开主页（intro / teaching_style / available_time_slots / styles）
   - `POST /h5/coach/certifications` 提交独立教练资质
   - `GET /h5/coach/dashboard` 经营看板（授课次数 / 学员数 / 月收益 / 待回复评价数 / 平均评分 / 下单转化率）
   - `GET /h5/coach/review-replies` 待回复评价列表

5. **Review Reply + Appeal**（教练/商家共用）
   - `POST /h5/review-replies` body { reviewId, replyContent }
   - `DELETE /h5/review-replies/{id}` 删除自己的回复
   - `POST /h5/review-appeals` body { reviewId, appealReason, evidenceNote }
   - `GET /h5/review-appeals/mine` 我提交的申诉
   - 平台侧：`POST /admin/review-appeals/{id}/approve|reject`

6. **平台管理员**（`/admin/**`）
   - `POST /admin/studio-claims/{id}/approve|reject` 审核认领
   - `POST /admin/coach-certifications/{id}/approve|reject` 教练资质审核
   - `GET /admin/review-appeals?status=pending` 申诉工作台
   - `GET /admin/report-tickets?status=pending&type=` 举报工作台
   - `POST /admin/report-tickets/{id}/process|close|reject` 处理
   - `GET /admin/audit-log?actor=&action=&from=&to=` 审计日志查询

7. **Audit Log**
   - 所有 /merchant/**、/admin/** 写操作要写 `audit_log`：actor_user_id、action、target_type、target_id、payload jsonb、ip、user_agent
   - 用 AOP 切面 `@AuditAction("studio.claim.approve")` 注解化最省事

### 关键业务规则

1. **舞室认领状态机**：pending → approved（开通商家后台） | rejected
2. **教练资质状态机**：pending → approved（自动建 coach 行 + user_role_binding 加 COACH 角色） | rejected
3. **教练发布 Workshop 权限**：
   - 签约教练（studio_coach_relation.relation_type='contracted'）：直发 published
   - 自由教练（freelance）：走 pending_review 由所属舞室管理员审核
   - 平台审核降级方案：所有教练新发都走 pending_review，BE-015 优化
4. **数据权限**：
   - 舞室管理员：只能操作自己认领的舞室，studio_id ∈ (我的 studio_claim where claim_status='approved')
   - 教练：只能编辑自己的 coach 行（coach.user_id = 当前用户）
   - 平台管理员：全可见
5. **Trial 商家侧 confirm**：pending → confirmed，confirmed_by_user_id 填操作人；带 trial_booking.studio_id 数据权限校验
6. **review_appeal approve**：把对应 review.review_status 改 'hidden'；reject 不动 review

### 测试要点（每个子模块 3-5 例，总计至少 30 例）

按 SKILL，BE-014 工程量大，可以拆 2-3 个 commit 推（仍属同一 BE 号）：
- commit 1：Merchant 舞室 + 课程 + 教练关系
- commit 2：Trial 商家侧 + Workshop 创建侧
- commit 3：Coach Ops + Review Reply/Appeal + Admin + Audit Log

每个子 commit 独立跑 `mvn test`，全绿后再 push。

### 跨模块依赖

- 涉及几乎所有先前模块：iam（角色检查）、catalog（studio/course/coach 写入）、booking（trial 状态流转）、workshop（创建侧）、review（reply/appeal）

### 待办

- 子账号管理（舞室管理员可建多个子账号）：阶段 D
- 收益结算 `settlement_bill`：阶段 D
- 商家批量数据导入 `data_import_batch`：阶段 D

---

## BE-015 ｜ 部署 + 徽章规则引擎 + CI + 缓存 + 收尾

### 目标

把整个工程从"能跑 + 能测"升级到"能部署 + 能联调 + 能持续集成"。

### 任务清单

#### 15.1 徽章发放规则引擎（M5 BE-010 留口）

- `badge_definition.rule_type` 与 `rule_config jsonb` 驱动
- 实现 `BadgeRuleEngine` 接口 + 几个内置规则：
  - `CHECKIN_STREAK_7` / `CHECKIN_STREAK_30`
  - `FIRST_WORK_PUBLISHED`
  - `FIRST_REVIEW`
  - `FIRST_PRACTICE_COMPLETED`
- 触发点：在 BE-010 GrowthService.createCheckin、BE-008 ReviewService.create、BE-013 buddy 建立时调用 `BadgeRuleEngine.evaluate(userId, eventType)`
- 命中即写 `growth_badge`（uk 防重复授予）+ 推送一条 notification

#### 15.2 Redis 缓存接入

- `RedisTemplate` 已经在 application.yml 配过；BE-002 SmsCodeService 已用过
- 加缓存的接口：
  - `GET /public/studios/{id}` → key `catalog:studio:detail:{id}` TTL 10 分钟
  - `GET /public/reviews/summary` → key `review:summary:{type}:{id}` TTL 10 分钟
  - `GET /public/practices`（约练广场）→ key `practice:square:{cityId}:{styleId}` TTL 1 分钟
- 写操作（评价、修改舞室等）触发对应 key 失效

#### 15.3 OpenAPI 导出

- SpringDoc 2.6 已引入，`/api/swagger-ui.html` 已可访问
- 把 OpenAPI JSON 导出到 `docs/openapi.json`，供前端 axios 类型生成或 Postman 导入
- 给每个 Controller 加 `@Tag(name="M2 Review")` 之类的分组标签

#### 15.4 Dockerfile + docker-compose

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:21-jre
COPY target/bitdance-backend.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
# docker-compose.yml（开发期可选）
services:
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
  backend:
    build: ./backend
    ports: ["8080:8080"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://98.142.241.155:5432/bitdance?currentSchema=bitdance
      SPRING_DATASOURCE_USERNAME: ${PG_USER}
      SPRING_DATASOURCE_PASSWORD: ${PG_PWD}
      BITDANCE_JWT_SECRET: ${JWT_SECRET}
      SPRING_REDIS_HOST: redis
```

#### 15.5 GitHub Actions CI

`.github/workflows/ci.yml`：
- 触发：push / pull_request to master
- 步骤：
  - actions/setup-java@v4 with java-version: '21'
  - cache maven
  - `cd build/backend && mvn -B test`
  - actions/setup-node@v4 with node-version: '20'
  - `cd build/frontend && npm ci && npm run build`
- 任一步失败 fail-fast

#### 15.6 application-prod.yml + 凭证

- `application-prod.yml` 模板，全部走 env：`${SPRING_DATASOURCE_URL}` 等
- 把 SKILL 第 3 节的云库凭证写到 `application-local.yml`（已 gitignore），不要入库
- README 增加"部署清单"章节

#### 15.7 Practice / Workshop 真支付收尾

- 把 BE-012 的 `MockPaymentGateway` 切换接口，预留 `WechatPayGateway` 实现入口（不实现，留 TODO）
- 把 trial_booking 商家侧（BE-014 已实现）与前端 mock 对齐

#### 15.8 性能与索引复核

- 跑 `EXPLAIN ANALYZE` 检查：附近搜索、约练广场、社区 feed、评价摘要的查询计划
- 不足索引补到 schema.sql 末尾"INDEXES"区块（**只允许新增，不允许删除既有索引**）

### 测试要点

- BadgeRuleEngine 单元测试：每条规则正反例至少 2 例
- Redis 缓存：跑 `@SpringBootTest` 集成测 + Testcontainers Redis
- OpenAPI：跑一次启动，断言 `/v3/api-docs` 返回 JSON 且包含核心 path

### 待办（非阻塞 MVP）

- 监控告警：留到生产化阶段
- 日志聚合：留到生产化阶段
- 限流：留到流量上来后做

---

## 1. 给下一位开发者的额外提示

### 1.1 出错时的排查路径

`mvn test` 挂了：

```bash
cd build/backend
mvn test 2>&1 | grep -E "Caused by|cannot find|Tests run" | head -10
```

最常见错误：
- "Cannot load driver class: org.h2.Driver" → `pom.xml` 漏了 `com.h2database:h2` test scope
- "No qualifying bean of type 'com.bitdance.iam.jwt.JwtService'" → 测试类漏加 `@MockBean JwtService jwtService`
- "ApplicationContext failure threshold exceeded" → 用 `@WebMvcTest(controllers=Xxx.class, excludeAutoConfiguration=SecurityAutoConfiguration.class)` 切片，不要用 `@SpringBootTest`
- "数据库默认 schema 错误" → 检查 application-test.yml 是否覆盖了 `hibernate.default_schema`（H2 不能用 bitdance schema 应该置空）

### 1.2 重要的隐性约束

- 所有 `chk_*_status` CHECK 约束都是 schema 写死的；service 必须用 `Set<String>` 白名单校验，否则上真库时 PSQLException
- 多态字段 (target_type, target_id) 的 target_type 校验集合必须与 schema CHECK 字面一致，不要自由发挥
- 时间字段一律 `OffsetDateTime`（schema 是 `timestamptz`），不要用 `LocalDateTime`
- `numeric(x, y)` schema 列必须映射成 `BigDecimal` + `@Column(precision, scale)`，不要用 double

### 1.3 当前已知留待集成测试的清单

| 范围 | 留 TODO 的事 |
| --- | --- |
| BE-005 | `fn_haversine_km` 真值排序 |
| BE-006 | jsonb / text[] 字段读写、course_schedule 时区行为 |
| BE-009 | 真库 `fn_close_expired_practice_posts()` 替代 JPA closeExpired |
| BE-010 | timeline 接 trial/practice/review 事件总线 |
| 全局 | Redis 多实例下分布式锁、定时任务集群幂等 |

### 1.4 与前端的契约对齐

前端 `frontend/src/api/*.ts` 已用 mock 实现所有调用，本次后端实现的 URL 与字段命名必须与那些 ts 文件保持一致，前端只需把 `.env.development` 的 `VITE_USE_MOCK=true` 改成 `false` 就能联调。

每个 BE-* 开始前先 grep 一下对应的前端文件：

```bash
ls build/frontend/src/api/
# auth.ts, community.ts, workshop.ts, coach.ts, growth.ts, practice.ts, review.ts, studio.ts, trial.ts, favorite.ts, user.ts, message.ts ...
```

打开对应文件看其调用的 URL 与字段，后端契约对齐到一字不差。

### 1.5 当一切顺利通过 W5

收官清单：
- `mvn test`：全绿，覆盖率（如有要求）≥ 60%
- `npm run build`：✓ built
- CI：master 上最新 commit 通过
- 文档：`开发历史记录.md` 收录 BE-001~015 全部日志 + v0.5.0 变更日志
- 部署：`application-local.yml` 模板可用 + docker-compose 可起 + OpenAPI 可访问
- 凭证：所有敏感信息 env / gitignore，仓库 grep 不到 schema 第 3 节的密码字符串

---

## 2. 联系上下文

- 远端：`https://github.com/Asnowww/BitDance.git`
- 分支：`master`（不开 feature 分支，每个 BE-* 直接打 master，已与团队约定）
- 历史：`git log --oneline` 看到 50+ 条提交，最近的是 `c676d4a docs: BE-010 ...`
- 数据库：SKILL 第 3 节有凭证（公网云库 98.142.241.155，PostgreSQL，schema=bitdance）
- 团队：第五组，组长顾远（产品立项书首页可查）

按本文档执行不会偏离 SKILL 与团队约定。**最重要的一条**：mvn test 必绿 → commit 代码 → 同 turn 内补 `开发历史记录.md` → 再 commit + push。不要破坏这条纪律。

祝顺利。
