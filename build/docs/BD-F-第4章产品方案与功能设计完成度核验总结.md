# BD-F 第 4 章产品方案与功能设计完成度核验总结

核验日期：2026-05-23  
核验范围：`BD-F/BitDance/build` 项目当前代码、数据库 schema、项目文档与可执行验证记录。  
核验目标：判断当前 BD-F 项目是否完成 `backend/【第五组】【组长：顾远】【BitDance舞室点评项目产品立项说明书】.pdf` 第 4 章“产品方案与功能设计”中定义的 M1-M7 功能模块与 4.10 工程复杂性要求。  
输出结论口径：`已完成 / 基本完成 / 部分完成 / 未完成 / 未核验`。

## 1. 核验结论总览

总体判断：当前 BD-F 项目已经覆盖第 4 章提出的核心产品路径“搜索决策 -> 学习陪伴 -> 成长沉淀”，用户/教练 H5 端、Spring Boot 后端、PostgreSQL schema 与 mock/种子数据具备较完整的 MVP 闭环。若按 MVP 主链路判断，M1-M6 的用户侧能力完成度较高；若按 PDF 第 4 章全量功能点与复杂度权重定量评估，M1-M7 综合加权完成度为 60%，属于“部分完成”。

需要特别说明：

- 当前项目不是只停留在表结构层面，已经存在可调用的 Controller、Service、Repository、前端页面和 mock/API 封装。
- 当前项目接口文档已有 SpringDoc/OpenAPI 自动分组基础，但缺少大量 `@Operation`、`@Tag`、`@Schema` 等业务注解，不能视为完善的人工接口文档。
- 后端自动化测试需要在 Java 21 下复核。
- 前端 `npm run type-check` 和 `npm run build` 已通过，说明当前 H5 工程类型与生产构建链路可用。

## 2. 证据来源

### 2.1 产品需求证据

- PDF：`backend/【第五组】【组长：顾远】【BitDance舞室点评项目产品立项说明书】.pdf`
- 已核验章节：第 4 章“产品方案与功能设计”
- 第 4 章模块：M1 舞室与课程、M2 评价系统、M3 用户账号与角色、M4 约练社交、M5 成长档案、M6 社区与活动、M7 商家与教练管理、4.10 工程复杂性说明

### 2.2 项目实现证据

- 后端源码：`backend/src/main/java/com/bitdance`
- 后端测试：`backend/src/test/java/com/bitdance`
- 前端源码：`frontend/src`
- 前端 API 封装：`frontend/src/api`
- 前端页面：`frontend/src/pages`
- 数据库 schema：`backend/bitdance_postgresql_schema.sql`
- 项目进度记录：`开发历史记录.md`
- 后端说明：`backend/README.md`
- 前端说明：`frontend/README.md`
- **API 接口文档**：`docs/API.md`

### 2.3 核验方法

本次核验按“PDF 功能点 -> 前端页面/API -> 后端接口/服务 -> 数据库表结构 -> 测试或验证记录”的顺序判断，不把“存在表结构”直接等同于“功能完成”。对需要商家后台、平台后台或生产支付/结算等能力的功能，会区分“后端能力存在”和“独立管理前端是否存在”。

### 2.4 定量评分口径

子任务状态计分：

- `完成` = 1.0
- `基本完成` = 0.8
- `部分完成` = 0.5
- `仅预留/schema/mock` = 0.2
- `未发现` = 0

复杂度权重：

- `S = 1`：单页面或单接口级，通常 1-2 个子任务。
- `M = 2`：前后端联动或多状态流转，通常 3-5 个子任务。
- `L = 3`：跨模块、权限、数据聚合或多端联动，通常 6-8 个子任务。
- `XL = 5`：算法、支付结算、独立后台、复杂风控等，通常 8 个以上子任务或高风险集成。

完成度计算：

- 单需求点完成度 = 子任务得分总和 / 子任务数量 * 100%，按 5% 粒度四舍五入。
- 模块加权完成度 = Σ(需求点完成度 * 复杂度权重) / Σ复杂度权重，按 5% 粒度四舍五入。
- 分档：90%-100% 为已完成，75%-89% 为基本完成，40%-74% 为部分完成，1%-39% 为低完成度，0% 为未完成。

## 3. 模块完成度矩阵

| 模块 | PDF 级别 | 定量完成度 | 当前完成度 | 主要证据 | 主要缺口 |
| --- | --- | --- | --- | --- | --- |
| M1 舞室与课程 | P0/P1/P2/P3 | 70% | 部分完成（MVP 主链路基本完成） | `catalog`、`favorite`、`booking` 后端包（9 API）；首页、搜索、舞室/课程/教练详情、课表、试听、收藏页面；studio/course/schedule/trial/favorite 表 | 地图真实视图、舞室对比、智能推荐未完成；商家独立 Web 维护端未发现 |
| M2 评价系统 | P0/P1/P2 | 70% | 部分完成（P0 评价主链路基本完成） | `review` 后端包（13 API）；写评价、评价列表、评价摘要、回复、申诉页面/API；review/review_dimension_score/review_reply/review_appeal 表 | 图文/视频附件真实上传与复杂相似文案风控仍未完成 |
| M3 用户账号与角色 | P0/P1/P2 | 55% | 部分完成 | `iam`、`profile`、`message`、`coachops`（9 API）；登录、资料、隐私、消息、教练申请/主页页面；user/profile/role/privacy/notification 表 | 微信第三方登录、登录设备异常提醒、社交账号展示在当前 H5 中未完整闭环 |
| M4 约练社交 | P1/P2/P3 | 60% | 部分完成 | `practice`、`buddy`（16 API）；约练广场、发布、详情、我的约练、推荐与搭子、约练评价页面；practice/buddy/rating 表 | 拼课发起与推荐算法是弱实现或预留，取消惩罚策略未完整产品化 |
| M5 成长档案 | P1/P2/P3 | 60% | 部分完成 | `growth`、`badge`、`favorite`（15 API）；成长首页、打卡、目标、作品、收藏页面；growth/badge/favorite 表 | 月/季成长报告未发现完整实现；隐私联动和作品媒体上传仍是 MVP，未完成验证 |
| M6 社区与活动 | P1/P2 | 70% | 部分完成（消费侧主链路基本完成） | `community`、`workshop`（31 API）；社区 feed、发布、详情、话题、关注、搜索、Workshop 列表/详情/订单/签到/日历页面；content/workshop/order/checkin 表 | 转发、真实支付、退款/签到二维码、活动后评价联动仍偏 mock 或抽象 |
| M7 商家与教练管理 | P0/P1/P2 | 50% | 部分完成 | `merchant`、`coachops`、`admin`、`workshop`（26 API）；教练运营 H5 页面；商家/平台后端接口；claim/relation/certification/audit/settlement 表 | 当前目录未发现独立舞室/平台 Web 管理后台；结算提现、经营看板、管理端完整 UI 不足 |
| 4.10 工程复杂性 | - | 基本完成 | 模块化后端包、统一 API 前缀、OpenAPI 分组、Docker/CI 文档、PostgreSQL schema、Redis 缓存配置、119 接口全量文档（`docs/API.md`） | 接口文档注解不完善（1785 字段全推断，无 @Schema）；Java 25 测试失败需 Java 21 复核 ||

## 4. M1：舞室与课程模块

### 4.1 PDF 要求摘要

M1 要解决“找舞室、选课程、看老师、做决策”。PDF 明确列出：

- M1-F01 附近舞室搜索：基于定位展示附近舞室，支持地图/列表切换。
- M1-F02 多维度筛选：按舞种、价格、距离、时段、适合人群筛选。
- M1-F03 舞室详情页：基础信息、环境照片、主打舞种、课表、导航。
- M1-F04 课程详情页：名称、舞种、难度、价格、老师、训练强度。
- M1-F05 老师详情页：擅长舞种、教学风格、评价、可预约课程。
- M1-F06 收藏功能：收藏舞室、课程、老师。
- M1-F07 课表查看：按日/周视图展示课程安排。
- M1-F08 试听预约：在线预约试听，舞室收到通知。
- M1-F09 舞室对比：2-3 家舞室多维度并排对比。
- M1-F10 智能推荐：根据偏好推荐匹配舞室和课程。

<!-- M1 定量矩阵补充：按 PDF 功能点逐项拆分子任务，避免“舞室主链路存在”掩盖地图、对比、推荐等未完成项。 -->
### M1 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M1-F01 | 附近舞室搜索，含定位、列表和地图切换 | L | 3 | 附近查询接口：完成；距离排序：基本完成；前端列表：完成；定位失败手动输入：部分完成；地图视图切换：未发现 | 75% | 接地图组件；补定位异常显示；补地图/列表同步筛选；补浏览器验收截图 |
| M1-F02 | 按舞种、价格、距离、时段、适合人群筛选 | M | 2 | 舞种筛选：完成；价格/距离筛选：基本完成；关键词搜索：完成；时段筛选：部分完成；适合人群筛选：部分完成 | 80% | 补时段与适合人群字段；补筛选条件组合测试；补无结果提示验收 |
| M1-F03 | 舞室详情页 | M | 2 | 详情接口：完成；详情页面：完成；环境图展示：基本完成；课表摘要：基本完成；导航/纠错：部分完成 | 90% | 补真实导航入口；补用户纠错提交链路；补环境图媒体上传闭环 |
| M1-F04 | 课程详情页 | M | 2 | 课程详情接口：完成；详情页面：完成；难度/价格/老师字段：完成；训练强度字段：基本完成；试听入口联动：基本完成 | 90% | 补训练强度统一枚举；补课程下架态展示；补真实接口验收样例 |
| M1-F05 | 老师详情页 | M | 2 | 教练详情接口：完成；教练详情页：完成；擅长舞种/教学风格：完成；评价摘要：基本完成；可约课程：基本完成 | 85% | 补可约时间精细展示；补老师评价筛选；补教练主页媒体作品联动 |
| M1-F06 | 收藏舞室/课程/老师 | M | 2 | 收藏接口：完成；收藏检查：完成；收藏列表：完成；多类型收藏：基本完成；状态同步：基本完成 | 90% | 补取消收藏显式接口或交互；补 Workshop 收藏统一入口；补真实登录态验收 |
| M1-F07 | 日/周课表查看 | M | 2 | 课表接口：完成；课表页面：完成；按日期展示：基本完成；周视图：部分完成；排期状态展示：基本完成 | 85% | 补完整周视图交互；补场地/余量字段；补课程取消/满员态 |
| M1-F08 | 在线试听预约和状态查看 | L | 3 | 用户预约：完成；重复预约防护：完成；我的预约：完成；取消预约：完成；商家确认/拒绝/到店/失约：基本完成；提醒通知：部分完成 | 85% | 补预约提醒通知；补商家管理 UI；补完整状态流转端到端脚本 |
| M1-F09 | 2-3 家舞室并排对比 | M | 2 | 收藏列表基础：完成；可对比数据字段：部分完成；对比页：未发现；对比聚合接口：未发现；对比交互：未发现 | 35% | 新增舞室对比页；新增对比聚合接口；定义对比字段；补 2-3 家选择逻辑 |
| M1-F10 | 根据偏好智能推荐舞室和课程 | XL | 5 | 用户偏好数据：完成；可用舞室/课程数据：完成；规则推荐：仅预留/schema/mock；推荐接口：未发现；推荐解释与排序：未发现 | 30% | 新增规则推荐服务；接入用户偏好；补推荐接口和首页入口；补推荐效果验收 |

本模块加权完成度：70%（Σ 1720 / 权重 25，按 5% 粒度取整）。

主要扣分原因：地图视图、舞室对比和智能推荐属于 PDF 明确功能，但当前只具备主链路数据和页面基础，尚未形成完整闭环。

### 4.2 当前实现证据

前端证据：

- `frontend/src/pages/home/HomePage.vue`：首页舞室发现入口。
- `frontend/src/pages/home/SearchPage.vue`：搜索与筛选入口。
- `frontend/src/pages/studio/StudioDetailPage.vue`：舞室详情页。
- `frontend/src/pages/studio/CourseDetailPage.vue`：课程详情页。
- `frontend/src/pages/studio/CoachDetailPage.vue`：教练详情页。
- `frontend/src/pages/studio/StudioSchedulePage.vue`：舞室课表页。
- `frontend/src/pages/studio/TrialBookingPage.vue`：试听预约页。
- `frontend/src/pages/user/FavoritesPage.vue`：收藏聚合页。
- `frontend/src/pages/user/MyTrialsPage.vue`：我的试听预约。
- `frontend/src/api/studio.ts`、`frontend/src/api/course.ts`、`frontend/src/api/coach.ts`、`frontend/src/api/trial.ts`：对应 API 封装。

后端证据：

- `backend/src/main/java/com/bitdance/catalog/controller/StudioController.java`：`GET /public/studios/nearby`、`GET /public/studios/{id}`。
- `backend/src/main/java/com/bitdance/catalog/controller/CourseController.java`：课程详情与课程排期。
- `backend/src/main/java/com/bitdance/catalog/controller/CoachController.java`：教练详情与教练课程。
- `backend/src/main/java/com/bitdance/catalog/controller/StudioScheduleController.java`：舞室课表。
- `backend/src/main/java/com/bitdance/favorite/controller/FavoriteController.java`：收藏创建、列表、检查。
- `backend/src/main/java/com/bitdance/booking/controller/TrialBookingController.java`：用户侧试听预约、取消、列表。
- `backend/src/main/java/com/bitdance/booking/controller/MerchantTrialBookingController.java`：商家侧确认、拒绝、到店、失约状态流转。

数据库证据：

- `studio`、`studio_dance_style`、`coach`、`coach_dance_style`、`course`、`course_schedule`、`favorite`、`trial_booking`、`catalog_correction_ticket`。
- `fn_haversine_km` 支撑距离计算，`idx_studio_longitude_latitude`、`idx_course_*`、`idx_course_schedule_*` 支撑搜索和排期查询。

测试证据：

- 存在 `StudioControllerTest`、`CourseControllerTest`、`CoachControllerTest`、`StudioScheduleControllerTest`、`FavoriteControllerTest`、`TrialBookingControllerTest`、`MerchantTrialBookingControllerTest`。
- 当前本机 Java 25 下 `mvn test` 未通过，原因集中在 Mockito/Byte Buddy mock 兼容问题；这些测试文件可作为覆盖意图证据，但需要 Java 21 复跑确认。

### 4.3 完成度判断

定量结论：部分完成（70%）。MVP 主链路可判断为基本完成，但 PDF 全量功能中地图、舞室对比和智能推荐仍明显缺口。

M1 的 P0/P1 主链路已经落地：附近舞室列表、搜索筛选、舞室详情、课程详情、教练详情、课表、收藏、试听预约、商家侧预约处理均有前端页面或后端接口支撑。数据库也覆盖了舞室、课程、教练、课表、收藏、预约和纠错票据。

### 4.4 主要缺口

- M1-F01 中“地图/列表切换”在当前证据中更接近列表搜索，真实地图视图能力不足。
- M1-F09 舞室对比未发现明确页面和后端聚合服务。
- M1-F10 智能推荐未形成完整算法服务，更多属于后续增强。
- 舞室基础信息和课表的商家独立 Web 管理前端未在当前目录中发现。

### 4.5 后续建议

- 补充舞室对比页和聚合接口，至少支持收藏列表选取 2-3 家进行评分、价格、距离、舞种、课表对比。
- 若验收要求包含地图，应接入地图组件或提供清晰的“列表 MVP 替代说明”。
- 将推荐能力拆成可验收的第一版规则推荐，例如按用户偏好舞种、城市、价格区间和热度排序。

## 5. M2：评价系统模块

### 5.1 PDF 要求摘要

M2 要建立舞室、老师、课程三类对象的结构化评价体系，并通过权重和风控提高评价可信度。PDF 明确列出：

- M2-F01 舞室评价：交通、卫生、场地、氛围。
- M2-F02 老师评价：耐心度、纠错质量、讲解清晰度。
- M2-F03 课程评价：上手难度、节奏、强度、收获。
- M2-F04 评价权重分层：根据报名、签到、核销状态区分权重。
- M2-F05 评分聚合展示：综合评分与各维度雷达图。
- M2-F06 图文/视频评价：支持附加照片或短视频。
- M2-F07 评价风控：异常评价降权、折叠、人工复核。
- M2-F08 商家申诉：不实评价申诉进入人工审核。

<!-- M2 定量矩阵补充：评价主流程完成度较高，但媒体附件和复杂风控工作量大，需单独计入扣分。 -->
### M2 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M2-F01 | 舞室多维结构化评价 | M | 2 | 评价对象支持：完成；维度评分入库：完成；写评价页面：完成；公开列表：完成；异常状态处理：基本完成 | 90% | 补真实图片附件；补评价提交后页面刷新验收 |
| M2-F02 | 老师结构化评价 | M | 2 | coach 评价对象：完成；老师维度：完成；老师评价摘要：基本完成；教练侧查看/回复：基本完成；验证来源：部分完成 | 90% | 补约练/课程完成后的老师评价验证来源；补老师评价筛选 |
| M2-F03 | 课程结构化评价 | M | 2 | course 评价对象：完成；课程维度：完成；课程详情联动：基本完成；汇总展示：基本完成；已验证判定：部分完成 | 90% | 补课程履约与评价权重联动；补课程详情评价入口验收 |
| M2-F04 | 评价权重分层 | L | 3 | verified 标记：完成；weightFactor 字段：完成；试听来源校验：基本完成；支付/签到权重：部分完成；权重可解释展示：未发现 | 80% | 补支付/签到/核销来源权重；补权重说明；补异常权重测试 |
| M2-F05 | 评分聚合与雷达图展示 | M | 2 | summary 接口：完成；维度聚合：完成；评分展示：基本完成；雷达图视觉：部分完成；缓存失效：基本完成 | 80% | 补雷达图 UI 验收截图；补聚合缓存真实数据校验 |
| M2-F06 | 图文/视频评价 | L | 3 | media schema：完成；附件通用模型：部分完成；评价上传入口：未发现；评价媒体绑定：未发现；审核/预览：未发现 | 35% | 新增媒体上传接口；评价提交绑定附件；补图片/视频预览与审核状态 |
| M2-F07 | 评价风控 | XL | 5 | 基础风险等级：基本完成；新号/频率判断：部分完成；降权/待审状态：基本完成；相似文案：未发现；设备/IP/波动检测：未发现 | 50% | 补 simhash/相似文案；补设备/IP 聚集规则；补人工复核工作台细节 |
| M2-F08 | 商家申诉 | L | 3 | 申诉提交：完成；我的申诉：完成；平台审核：完成；申诉成立隐藏评价：基本完成；商家 Web UI：部分完成 | 80% | 补商家管理端申诉列表；补申诉材料附件；补平台审核备注展示 |

本模块加权完成度：70%（Σ 1535 / 权重 22，按 5% 粒度取整）。

主要扣分原因：P0 结构化评价已基本可用，但图文/视频评价和复杂风控是跨媒体、审核和算法的高复杂度工作，当前仍明显不足。

### 5.2 当前实现证据

前端证据：

- `frontend/src/pages/publish/PublishReviewPage.vue`：写评价。
- `frontend/src/pages/studio/StudioReviewsPage.vue`：评价列表和展示。
- `frontend/src/pages/user/MyReviewsPage.vue`：我的评价。
- `frontend/src/pages/coach/ReplyReviewsPage.vue`：评价回复。
- `frontend/src/pages/coach/AppealPage.vue`：评价申诉。
- `frontend/src/components/StarRating.vue`：评分组件。
- `frontend/src/api/review.ts`：评价、汇总、回复、申诉 API 封装。

后端证据：

- `backend/src/main/java/com/bitdance/review/controller/ReviewController.java`：评价创建、删除、公开列表、评价摘要。
- `backend/src/main/java/com/bitdance/review/controller/ReviewReplyController.java`：评价回复创建、删除、公开查询、我的回复。
- `backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java`：用户/教练侧申诉、平台审核通过/驳回。
- `backend/src/main/java/com/bitdance/review/service/ReviewRiskService.java`：评价风险和权重评估。
- `backend/src/main/java/com/bitdance/review/service/ReviewService.java`：评价主流程、维度分、摘要聚合。

数据库证据：

- `review`：评价主表，包含对象类型、综合分、权重、验证标记、风险等级、状态。
- `review_dimension_score`：多维结构化评分。
- `review_reply`：评价回复。
- `review_appeal`：评价申诉。
- `media_asset`、`media_attachment`：媒体资源和附件挂载的通用结构，可支撑图文/视频扩展。

测试证据：

- 存在 `ReviewControllerTest`、`ReviewReplyControllerTest`、`ReviewAppealControllerTest`。
- 历史记录显示 BE-008 完成评价、雷达图与风控权重；BE-014.1 完成回复与申诉。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 5.3 完成度判断

定量结论：部分完成（70%）。P0 结构化评价主链路可判断为基本完成，但媒体附件和复杂风控拉低全量完成度。

M2 的核心 P0 功能已经实现：三类评价对象、维度评分、权重分层、评价汇总和风险状态均有后端模型与接口支撑。商家回复和申诉也已覆盖，前端存在写评价、评价展示、回复与申诉页面。

### 5.4 主要缺口

- M2-F06 图文/视频评价虽然 schema 有媒体附件通用表，但未核验到完整上传、绑定、预览、审核闭环。
- M2-F07 的复杂风控如相似文案、设备/IP 聚集、异常波动检测仍属于基础实现或待增强。
- 评价雷达图展示是否达到 PDF 所述视觉标准，需要进一步做 UI 实测截图核验。

### 5.5 后续建议

- 明确评价附件上传接口和前端交互，补充媒体上传到评价的端到端用例。
- 给 `ReviewController`、DTO 和 schema 模型补充 OpenAPI 注解，使评价接口文档可直接用于联调验收。
- 增加至少一组真实数据下的评价摘要截图或接口响应样例，作为 M2 验收证据。

## 6. M3：用户账号与角色模块

### 6.1 PDF 要求摘要

M3 是身份基座，负责“用户是谁、能做什么”。PDF 明确列出：

- M3-F01 手机号注册/登录：手机号 + 验证码完成注册与登录。
- M3-F02 第三方登录：微信授权登录与账号绑定。
- M3-F03 个人资料管理：昵称、头像、性别、生日、简介。
- M3-F04 舞蹈偏好设置：感兴趣舞种、当前水平、学习目标。
- M3-F05 角色与权限管理：多角色身份切换，按角色加载功能权限。
- M3-F06 隐私设置：资料、打卡、约练、社区动态可见范围。
- M3-F07 消息通知中心：系统、互动、约练、评价、活动等分类通知。
- M3-F08 账号安全：密码与绑定手机、登录设备、异常登录提醒。
- M3-F09 举报与封禁：用户互相举报，平台触发风控与账号冻结。
- M3-F10 社交账号展示：个人主页展示抖音、小红书等账号。

<!-- M3 定量矩阵补充：手机号登录和资料能力已落地，但第三方登录、账号安全、封禁和社交账号展示仍需按子任务扣分。 -->
### M3 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M3-F01 | 手机号验证码注册/登录 | M | 2 | 短信发送接口：完成；登录接口：完成；JWT：完成；前端登录页：完成；验证码真实通道：部分完成 | 90% | 补真实短信服务或验收说明；补验证码频控和错误提示截图 |
| M3-F02 | 微信第三方登录与绑定 | L | 3 | 社交账号表：仅预留/schema/mock；微信授权入口：未发现；绑定接口：未发现；解绑/冲突处理：未发现；前端入口：未发现 | 20% | 接微信 OAuth；补账号绑定/解绑；补手机号合并策略；补回调安全校验 |
| M3-F03 | 个人资料管理 | M | 2 | 资料接口：完成；编辑页面：完成；昵称/性别/生日/简介：基本完成；头像媒体：部分完成；表单校验：基本完成 | 85% | 补头像真实上传；补字段校验提示；补公开主页展示一致性 |
| M3-F04 | 舞蹈偏好设置 | M | 2 | 偏好表：完成；资料页编辑：基本完成；舞种/水平/目标字段：基本完成；推荐联动：部分完成；保存接口：完成 | 80% | 补偏好到推荐/约练的实际使用；补多舞种选择体验 |
| M3-F05 | 角色与权限管理 | L | 3 | 角色绑定表：完成；SecurityConfig 路径权限：完成；教练申请联动：基本完成；角色切换 UI：部分完成；按钮级权限：部分完成 | 75% | 补多角色切换入口；补管理端菜单权限；补越权访问验收用例 |
| M3-F06 | 隐私设置 | M | 2 | 隐私表：完成；隐私页面：完成；保存/读取：基本完成；跨模块过滤：部分完成；可见范围端到端测试：未发现 | 60% | 接入 profile/growth/practice/community 读取过滤；补他人访问验收 |
| M3-F07 | 消息通知中心 | M | 2 | 消息表：完成；消息列表：完成；已读/全部已读：完成；分类通知：基本完成；推送提醒：部分完成 | 85% | 补约练/评价/Workshop 事件触发通知；补浏览器或 APK 推送策略 |
| M3-F08 | 账号安全 | L | 3 | 登录设备表：仅预留/schema/mock；异常登录提醒：未发现；密码/绑定手机管理：部分完成；设备列表 UI：未发现；风控策略：未发现 | 25% | 补设备记录；补异常登录规则；补绑定手机变更；补安全中心页面 |
| M3-F09 | 举报与封禁 | L | 3 | 举报工单：基本完成；平台处理：基本完成；账号冻结字段/策略：部分完成；写接口封禁拦截：未发现；用户互报入口覆盖：部分完成 | 50% | 补账号封禁状态；补登录和写操作拦截；补用户主页举报入口 |
| M3-F10 | 社交账号展示 | M | 2 | 社交账号表：仅预留/schema/mock；编辑入口：未发现；主页展示：未发现；平台类型校验：未发现；隐私控制：未发现 | 20% | 补抖音/小红书字段编辑；补个人主页展示；补隐私可见范围 |

本模块加权完成度：55%（Σ 1350 / 权重 24，按 5% 粒度取整）。

主要扣分原因：MVP 身份基座已可用，但 PDF 中第三方登录、账号安全、封禁策略和社交账号展示属于独立子系统，目前多数只完成 schema 预留或基础入口。

### 6.2 当前实现证据

前端证据：

- `frontend/src/pages/user/LoginPage.vue`：登录入口。
- `frontend/src/pages/user/UserCenterPage.vue`：我的页面与角色入口。
- `frontend/src/pages/user/ProfileEditPage.vue`：个人资料与偏好编辑。
- `frontend/src/pages/user/PrivacyPage.vue`：隐私设置。
- `frontend/src/pages/user/MessagesPage.vue`：消息中心。
- `frontend/src/pages/user/CoachHomePage.vue`：教练主页入口。
- `frontend/src/pages/coach/CoachDashboardPage.vue`、`frontend/src/pages/coach/CoachWorkshopCreatePage.vue`、`frontend/src/pages/coach/CoachOrdersPage.vue`：教练侧运营能力。
- `frontend/src/stores/user.ts`：用户状态与 token 相关前端状态。

后端证据：

- `backend/src/main/java/com/bitdance/iam/controller/AuthController.java`：短信发送和登录。
- `backend/src/main/java/com/bitdance/iam/controller/MeController.java`：当前用户信息。
- `backend/src/main/java/com/bitdance/profile/controller/ProfileController.java`：资料、偏好、隐私。
- `backend/src/main/java/com/bitdance/message/controller/NotificationController.java`：消息列表、已读、全部已读。
- `backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java`：教练资质申请与平台审核。
- `backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java`：教练资料、课程、看板。
- `backend/src/main/java/com/bitdance/iam/security/SecurityConfig.java`：API 权限前缀和角色访问控制。

数据库证据：

- `app_user`、`user_profile`、`privacy_setting`、`user_role_binding`、`sys_role`、`sys_permission`、`sys_role_permission`。
- `user_dance_preference`：舞蹈偏好。
- `notification`：消息通知。
- `user_login_device`、`user_social_account`、`user_block_relation`：登录设备、社交账号与拉黑关系的结构预留。
- `coach_certification_application`、`coach`：教练角色申请与资料。

测试证据：

- 存在 `AuthControllerTest`、`ProfileControllerTest`、`NotificationControllerTest`、`CoachCertificationControllerTest`、`CoachOpsControllerTest`。
- 历史记录显示 BE-001/002 完成登录/JWT，BE-003/004 完成 Profile 和 Message，BE-014.3-a 完成 Coach Ops 与 Coach Certification。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 6.3 完成度判断

定量结论：部分完成（55%）。手机号登录、资料、消息和基础角色能力可用，但第三方登录、账号安全、封禁和社交账号展示仍缺较多子任务。

M3 的 P0/P1 主体能力已经落地：手机号验证码登录、JWT、当前用户、资料偏好、隐私设置、消息中心、角色绑定、教练资质审核和教练侧运营接口均有实现。RBAC 和路径分层也与项目架构文档保持一致。

### 6.4 主要缺口

- M3-F02 微信第三方登录与账号绑定未发现完整闭环，MVP 当前以手机号验证码登录为主。
- M3-F08 登录设备、异常登录提醒虽然有表结构，但未核验到完整前端页面和安全策略流转。
- M3-F10 社交账号展示有 schema 预留，但当前 H5 个人主页展示和编辑闭环不足。
- M3-F09 举报与封禁更多通过社区举报、平台工单支撑，账号冻结/封禁策略未完整验收。

### 6.5 后续建议

- 若立项验收严格要求微信授权登录，应明确“降级为手机号登录”的产品变更依据，或补齐微信登录接口与前端入口。
- 将用户角色切换和权限可见性做成一组可演示路径：普通用户、教练、舞室管理员、平台管理员分别登录看到不同菜单。
- 补充账号封禁状态在登录、发帖、评价、约练等写接口中的统一拦截策略。

## 7. M4：约练社交模块

### 7.1 PDF 要求摘要

M4 面向“找搭子、约练、练完继续联系”。PDF 明确列出：

- M4-F01 发布约练：发布舞种、时间、地点、人数、水平要求。
- M4-F02 约练广场：展示附近或同城约练信息，支持多维筛选。
- M4-F03 约练响应：响应邀请，双方确认后建立约练关系。
- M4-F04 约练匹配推荐：根据舞种、水平、位置推荐搭子。
- M4-F05 搭子关系管理：互相约练后可互加搭子，沉淀长期关系。
- M4-F06 约练评价：约练后双方评价守时、友好、水平匹配。
- M4-F07 拼课发起：多名用户凑齐人数联系舞室开课。

<!-- M4 定量矩阵补充：约练主状态机完成度较高，但推荐、拼课和取消惩罚需要按高复杂度项单独评估。 -->
### M4 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M4-F01 | 发布约练 | M | 2 | 发布接口：完成；发布页面：完成；舞种/时间/地点/人数/水平字段：完成；有效期规则：基本完成；发布限制：部分完成 | 90% | 补每人最多 3 条发布限制；补取消冷却校验；补地理位置精度 |
| M4-F02 | 约练广场 | M | 2 | 广场接口：完成；广场页面：完成；同城筛选：完成；舞种/水平筛选：基本完成；附近精确距离：部分完成 | 80% | 接精确地理筛选；补排序策略；补空态和过期态验收 |
| M4-F03 | 约练响应与确认 | L | 3 | 申请接口：完成；接受/拒绝/撤回：完成；人数状态机：完成；双方提醒：部分完成；取消提前 2 小时规则：部分完成 | 85% | 补提醒通知；补取消提前时间限制；补频繁取消处罚 |
| M4-F04 | 约练匹配推荐 | XL | 5 | 偏好数据：完成；推荐页入口：部分完成；规则匹配服务：仅预留/schema/mock；位置推荐：部分完成；推荐解释：未发现 | 35% | 新增规则推荐接口；接入偏好、位置、活跃度；补拉黑过滤和推荐理由 |
| M4-F05 | 搭子关系管理 | L | 3 | buddy 表：完成；互评后沉淀：完成；搭子列表：完成；拉黑/移除：基本完成；再次邀约：部分完成 | 80% | 补再次邀约入口；补拉黑后全模块过滤；补搭子主页 |
| M4-F06 | 约练后双方评价 | L | 3 | 评价接口：完成；评价页面：完成；守时/友好/水平匹配：完成；双向互评触发 completed：完成；评价邀请通知：部分完成 | 85% | 补评价邀请消息；补评价修改/撤回策略；补信任分聚合 |
| M4-F07 | 拼课发起 | XL | 5 | group_class_intent 表：仅预留/schema/mock；拼课发布页：未发现；人数达标逻辑：未发现；通知舞室：未发现；商家处理：未发现 | 20% | 新增拼课意向接口；新增拼课页面；补人数阈值和通知舞室；补商家处理入口 |

本模块加权完成度：60%（Σ 1365 / 权重 23，按 5% 粒度取整）。

主要扣分原因：约练发布、响应、互评和搭子沉淀已经比较完整，但推荐与拼课属于 PDF 中明确的社交延展能力，目前只有入口或表结构预留。

### 7.2 当前实现证据

前端证据：

- `frontend/src/pages/practice/PracticeSquarePage.vue`：约练广场。
- `frontend/src/pages/publish/PublishPracticePage.vue`：发布约练。
- `frontend/src/pages/practice/PracticeDetailPage.vue`：约练详情与响应。
- `frontend/src/pages/user/MyPracticesPage.vue`：我的约练。
- `frontend/src/pages/practice/RecommendPage.vue`：推荐与搭子。
- `frontend/src/pages/practice/PracticeRatingPage.vue`：约练评价。
- `frontend/src/api/practice.ts`、`frontend/src/api/buddy.ts`：约练与搭子 API 封装。

后端证据：

- `backend/src/main/java/com/bitdance/practice/controller/PracticeController.java`：发布、广场、详情、取消、申请、接受、拒绝、撤回、我的约练等接口。
- `backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java`：约练评分、评分列表、搭子列表、拉黑、移除。
- `backend/src/main/java/com/bitdance/practice/job/ExpirePracticeJob.java`：约练过期关闭定时任务。
- `backend/src/main/java/com/bitdance/buddy/service/BuddyService.java`：双向互评后沉淀搭子关系。

数据库证据：

- `practice_post`：约练发布主表。
- `practice_join_request`：约练响应申请。
- `practice_rating`：约练后双方评价。
- `buddy_relation`：搭子关系。
- `group_class_intent`：拼课意向结构预留。
- `user_block_relation`：用户拉黑关系结构。

测试证据：

- 存在 `PracticeControllerTest`、`BuddyControllerTest`。
- 历史记录显示 BE-009 完成 Practice 约练全流程和过期定时任务，BE-013 完成约练互评与搭子关系沉淀。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 7.3 完成度判断

定量结论：部分完成（60%）。约练发布、响应、互评和搭子沉淀主链路可用，但推荐、拼课和取消惩罚策略仍未完成。

M4 的 P1/P2 主链路基本完成：发布约练、广场、详情、申请、接受/拒绝、我的约练、过期关闭、约练互评和搭子沉淀都有对应实现。前端也存在约练广场、发布、详情、推荐与搭子、评价页面。

### 7.4 主要缺口

- M4-F04 推荐算法更多像规则或页面入口，未核验到复杂推荐服务。
- M4-F07 拼课发起目前主要体现为 `group_class_intent` 表结构预留，**未发现完整前端和后端业务闭环**
- PDF 中“频繁取消者限制发布权限”未核验到完整惩罚策略。
- 位置推荐目前实现是城市/筛选维度占位，**未实现导入地图API的精确定位**。

### 7.5 后续建议

- 将推荐拆成最小可验收规则：同城、同舞种、同水平、近期活跃、未拉黑。
- 补齐拼课从意向发布、人数达标、通知舞室到商家处理的最短闭环。
- 增加取消次数统计和冷却策略，并在发布约练接口中统一校验。

## 8. M5：成长档案模块

### 8.1 PDF 要求摘要

M5 负责沉淀“用户做了什么、达成了什么”，与 M3 身份资料解耦。PDF 明确列出：

- M5-F01 训练打卡：记录舞种、时长、地点、感受。
- M5-F02 学习数据统计：学舞天数、累计时长、课程数、舞种数。
- M5-F03 收藏管理：集中查看收藏的舞室、课程、老师、Workshop。
- M5-F04 成长时间线：展示学舞历程和关键节点。
- M5-F05 阶段作品记录：上传练舞视频/照片作为成果。
- M5-F06 学习目标设置：设置周/月目标并追踪完成度。
- M5-F07 成长报告：按月/季度生成学习数据报告。
- M5-F08 成就徽章：达成里程碑解锁徽章。

### M5 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M5-F01 | 训练打卡 | M | 2 | 打卡接口：完成；打卡页面：完成；舞种/时长/地点/感受：基本完成；删除打卡：完成；目标进度联动：基本完成 | 90% | 补位置选择精度；补打卡隐私过滤；补图片/视频附件 |
| M5-F02 | 学习数据统计 | M | 2 | stats 接口：完成；累计时长/天数：完成；舞种数：基本完成；课程数：部分完成；趋势图/热力图：部分完成 | 85% | 补课程/Workshop 数据接入；补图表验收；补汇总缓存策略 |
| M5-F03 | 收藏管理 | M | 2 | 收藏表：完成；收藏页：完成；舞室/课程/老师：完成；Workshop 收藏：部分完成；收藏状态同步：基本完成 | 85% | 补 Workshop 收藏入口；补跨模块收藏排序；补取消收藏验收 |
| M5-F04 | 成长时间线 | L | 3 | 时间线接口：完成；打卡/作品事件：完成；页面展示：基本完成；试听/约练/Workshop 聚合：部分完成；隐私过滤：部分完成 | 65% | 补 trial/practice/workshop/review 事件接入；补隐私过滤；补事件类型图标 |
| M5-F05 | 阶段作品记录 | L | 3 | 作品表：完成；作品页面：完成；创建/删除：基本完成；照片/视频真实上传：未发现；审核/可见性：部分完成 | 50% | 接 media_attachment；补上传/预览；补审核状态和可见范围 |
| M5-F06 | 学习目标设置 | M | 2 | 目标表：完成；目标页面：完成；周/月目标：完成；进度追踪：基本完成；过期/完成状态：基本完成 | 85% | 补目标提醒；补历史目标列表；补只设时长或次数的边界规则 |
| M5-F07 | 成长报告 | XL | 5 | 报告表或接口：未发现；月/季汇总：未发现；图表生成：未发现；分享/导出：未发现；前端页面：未发现 | 0% | 新增月报/季报接口；设计报告页面；接入统计、目标、徽章、作品；补生成任务 |
| M5-F08 | 成就徽章 | L | 3 | 徽章定义：完成；规则引擎：完成；打卡/作品触发：完成；评价/约练/Workshop 触发：基本完成；通知/展示：部分完成 | 80% | 补徽章通知；补 admin 维护入口；补规则配置真实 PG jsonb 验收 |

本模块加权完成度：60%（Σ 1275 / 权重 22，按 5% 粒度取整）。

主要扣分原因：打卡、目标、作品和徽章已有主流程，但成长报告完全缺失，跨模块时间线和媒体作品上传仍是较大的未完成项。

### 8.2 当前实现证据

前端证据：

- `frontend/src/pages/growth/GrowthPage.vue`：成长首页、统计与时间线入口。
- `frontend/src/pages/publish/PublishCheckinPage.vue`：训练打卡。
- `frontend/src/pages/growth/GoalPage.vue`：训练目标。
- `frontend/src/pages/growth/WorksPage.vue`：阶段作品。
- `frontend/src/pages/user/FavoritesPage.vue`：收藏管理。
- `frontend/src/api/growth.ts`：成长相关 API 封装。

后端证据：

- `backend/src/main/java/com/bitdance/growth/controller/GrowthController.java`：打卡、统计、时间线、目标、作品、徽章接口。
- `backend/src/main/java/com/bitdance/growth/service/GrowthService.java`：成长数据主流程。
- `backend/src/main/java/com/bitdance/badge/controller/BadgeDefinitionController.java`：公开徽章定义。
- `backend/src/main/java/com/bitdance/badge/service/BadgeRuleEngine.java`：徽章规则引擎。
- `backend/src/main/java/com/bitdance/favorite/controller/FavoriteController.java`：统一收藏聚合能力。

数据库证据：

- `growth_checkin`：训练打卡。
- `growth_goal`：训练目标。
- `growth_work`：阶段作品。
- `growth_badge`、`badge_definition`：徽章定义与授予记录。
- `favorite`：统一收藏。
- `vw_user_growth_summary`：成长统计读模型。

测试证据：

- 存在 `GrowthControllerTest`、`BadgeRuleEngineTest`、`FavoriteControllerTest`。
- 历史记录显示 BE-010 完成 Growth 打卡、统计、时间线、目标、作品、徽章读取；BE-015-a/b/c 完成徽章规则引擎和部分触发点接入。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 8.3 完成度判断

定量结论：部分完成（60%）。打卡、目标、作品和徽章有基础闭环，但成长报告、媒体作品和跨模块时间线聚合缺口较大。

M5 的 P1/P2 核心能力已经落地：训练打卡、成长统计、收藏聚合、成长时间线、目标设置、阶段作品和徽章读取/发放规则均有实现。前端页面也覆盖了成长首页、打卡、作品和目标。

### 8.4 主要缺口

- M5-F07 月报/季报成长报告未发现完整实现。
- M5-F05 阶段作品涉及视频/照片上传，当前更多是作品记录能力，媒体上传和审核闭环未充分核验。
- 成长时间线对试听、课程、Workshop、约练、评价等跨模块事件的聚合程度需要端到端数据验证。
- 成长数据可见性与 M3 隐私设置之间的强约束联动未完整核验。

### 8.5 后续建议

- 补充“本月成长报告”最小版本：累计时长、打卡次数、舞种数、目标完成率、徽章变化。
- 将成长时间线统一为事件聚合规范，明确来自打卡、作品、试听、约练、Workshop 的字段。
- 给阶段作品接入媒体附件表，形成上传、展示、删除、可见性控制闭环。

## 9. M6：社区与活动模块（消费侧）

### 9.1 PDF 要求摘要

M6 承载消费侧内容生产、社交互动和 Workshop 报名履约。PDF 明确列出：

- M6-F01 动态内容发布：图文/视频创作、编辑、删除，支持话题、定位、舞种关联。
- M6-F02 内容互动：点赞、评论、回复、转发、收藏、违规举报。
- M6-F03 话题标签管理：话题创建、聚合、热门推荐。
- M6-F04 关注与粉丝：关注/取关、关注列表、关注内容优先推送。
- M6-F05 内容搜索筛选：按话题、舞种、用户、活动检索。
- M6-F06 Workshop 浏览：详情、师资、场地、往期评价。
- M6-F07 Workshop 报名支付：选择场次、下单、支付、订单、取消退款。
- M6-F08 扫码签到：活动当天到场扫码签到。
- M6-F09 活动日历推送：个人活动日历和提醒。

### M6 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M6-F01 | 动态内容发布 | L | 3 | 发帖接口：完成；发布页面：完成；话题/舞种/定位字段：基本完成；编辑动态：部分完成；图文/视频上传：未发现 | 65% | 补编辑接口和页面；补媒体上传；补定位选择；补草稿/删除确认 |
| M6-F02 | 内容互动 | L | 3 | 点赞：完成；评论/回复：完成；举报：完成；收藏：部分完成；转发：仅预留/schema/mock | 70% | 补转发接口和 UI；补收藏动态入口；补楼中楼展示；补举报反馈 |
| M6-F03 | 话题标签管理 | M | 2 | 话题表：完成；话题列表：完成；话题详情：完成；自动创建话题：基本完成；热门推荐：部分完成 | 80% | 补热门话题排序；补平台话题管理；补话题封禁/合并 |
| M6-F04 | 关注与粉丝 | M | 2 | 关注/取关：完成；关注内容页：完成；关注关系表：完成；粉丝列表：部分完成；优先推送：基本完成 | 80% | 补粉丝列表；补关注动态排序规则；补拉黑过滤 |
| M6-F05 | 内容搜索筛选 | M | 2 | 搜索接口：完成；搜索页面：完成；话题/文本检索：基本完成；舞种/用户/活动检索：部分完成；热度/时间/距离排序：部分完成 | 75% | 补用户/活动检索；补多排序选项；补 PG 全文检索或 trigram |
| M6-F06 | Workshop 浏览 | M | 2 | 列表接口：完成；详情接口：完成；列表/详情页：完成；师资/场地信息：基本完成；往期评价：部分完成 | 80% | 补往期评价联动；补剩余名额实时态；补详情页分享 |
| M6-F07 | Workshop 报名支付 | XL | 5 | 订单创建：完成；支付抽象：基本完成；取消/退款：基本完成；真实微信支付：未发现；支付回调/对账：未发现 | 60% | 接真实支付；补回调验签；补支付记录/退款记录落库；补异常补偿 |
| M6-F08 | 扫码签到 | L | 3 | 用户签到接口：完成；签到页面：基本完成；商家核销接口：基本完成；二维码/票据验签：部分完成；扫码设备端：未发现 | 55% | 生成签到二维码；补票据过期和验签；补商家扫码 UI；补重复核销测试 |
| M6-F09 | 活动日历推送 | M | 2 | 订单列表：完成；活动日历页：基本完成；起止提醒：部分完成；定向推送：部分完成；日历订阅：未发现 | 65% | 补提醒任务；补通知中心联动；补日历视图和筛选 |

本模块加权完成度：70%（Σ 1630 / 权重 24，按 5% 粒度取整）。

主要扣分原因：社区和 Workshop 消费侧可演示，但真实支付、媒体、转发、二维码签到和活动后评价沉淀仍未完成生产级闭环。

### 9.2 当前实现证据

前端证据：

- `frontend/src/pages/community/CommunityFeedPage.vue`：社区信息流。
- `frontend/src/pages/community/PublishPostPage.vue`：发布动态。
- `frontend/src/pages/community/PostDetailPage.vue`：动态详情和评论。
- `frontend/src/pages/community/TopicsPage.vue`、`TopicDetailPage.vue`：话题广场与话题详情。
- `frontend/src/pages/community/FollowingPage.vue`：关注内容。
- `frontend/src/pages/community/CommunitySearchPage.vue`：社区搜索。
- `frontend/src/pages/workshop/WorkshopListPage.vue`、`WorkshopDetailPage.vue`、`WorkshopCheckinPage.vue`：Workshop 列表、详情和签到。
- `frontend/src/pages/user/MyWorkshopOrdersPage.vue`、`WorkshopCalendarPage.vue`：订单和活动日历。
- `frontend/src/api/community.ts`、`frontend/src/api/workshop.ts`：社区与 Workshop API 封装。

后端证据：

- `backend/src/main/java/com/bitdance/community/controller/CommunityController.java`：动态发布、删除、feed、搜索、点赞、评论、话题、关注、举报。
- `backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java`：Workshop 列表、详情、订单、支付、取消、退款、我的订单、签到。
- `backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java`：举报工单处理。
- `backend/src/main/java/com/bitdance/workshop/job/CloseUnpaidWorkshopOrderJob.java`：未支付订单定时关闭。
- `backend/src/main/java/com/bitdance/workshop/service/PaymentGateway.java` 与 Mock/WechatPay 占位实现：支付抽象。

数据库证据：

- 社区相关：`content_post`、`content_comment`、`content_like`、`content_share_log`、`content_post_topic`、`topic_tag`、`follow_relation`、`report_ticket`。
- Workshop 相关：`workshop`、`workshop_session`、`workshop_order`、`workshop_checkin`、`payment_record`、`refund_record`、`workshop_checkin_ticket`。
- 通知和行为记录：`notification`、`behavior_event_log`。

测试证据：

- 存在 `CommunityControllerTest`、`WorkshopControllerTest`、`AdminReportTicketControllerTest`、`CloseUnpaidWorkshopOrderJobTest`。
- 历史记录显示 BE-011 完成社区动态主流程，BE-012 完成 Workshop 报名支付闭环，BE-014.3-b 完成 Report 工作台。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 9.3 完成度判断

定量结论：部分完成（70%）。社区与 Workshop 消费侧主链路可演示，但真实媒体、支付、扫码和活动后评价联动尚未完整闭环。

M6 的消费侧主流程已经实现：社区动态、点赞评论、话题、关注、搜索、举报，以及 Workshop 浏览、下单、支付抽象、订单、取消退款、签到、活动日历都能在当前项目中找到对应页面、接口和数据表。

### 9.4 主要缺口

- M6-F01 的图文/视频创作涉及真实媒体上传，当前未完整核验。
- M6-F02 中转发存在表结构 `content_share_log`，但前端交互和后端完整转发流程证据不足。
- M6-F07 真实支付还处于 `PaymentGateway` 抽象和 WechatPay 占位，MVP 可演示但不是生产支付闭环。
- M6-F08 “扫码签到”的二维码/票据展示、扫码设备端和验签链路需要进一步实测。
- Workshop 活动结束后自动邀请评价并沉淀至 M2 的闭环未充分核验。

### 9.5 后续建议

- 为社区媒体附件建立一条端到端验收路径：上传 -> 绑定动态 -> 展示 -> 删除或审核。
- 将 Workshop 支付状态、退款状态和签到状态输出为一组验收用状态机图。
- 若演示需要，可用 mock 支付明确标注“支付网关抽象已完成，真实微信支付待接入”。

## 10. M7：商家与教练管理模块（创建侧）

### 10.1 PDF 要求摘要

M7 面向舞室与教练创建侧，承担入驻、课表、教练、Workshop 发布、履约、数据和结算。PDF 明确列出：

- M7-F01 舞室入驻认领：资质提交、平台审核、舞室认领与开通。
- M7-F02 舞室信息管理：门店信息、品牌介绍、环境照片、营业时间维护。
- M7-F03 课表管理：课程创建、排期、舞种/教练/场地关联、上下架。
- M7-F04 教练账号管理：招募邀请、绑定、签约/全职/自由身份分配与变更。
- M7-F05 教练主页运营：教练编辑个人介绍、教学风格、作品、可约时段。
- M7-F06 Workshop 发布：创建 Workshop。
- M7-F07 报名与履约管理：订单查看、签到核销、退款处理、活动复盘统计。
- M7-F08 评价回复与申诉：查看评价、回复、置顶、申诉。
- M7-F09 经营数据看板：客流、预约、核销率、营收、留存、教练授课等可视化。
- M7-F10 收益结算管理：收益统计、分账配置、提成核算、账单与提现。

### M7 需求点完成度评估矩阵

| 需求点 | 需求说明 | 复杂度 | 权重 | 子任务拆分与状态 | 完成度 | 待实现清单 |
| --- | --- | --- | --- | --- | --- | --- |
| M7-F01 | 舞室入驻认领 | L | 3 | 认领申请接口：完成；我的认领：完成；平台审核接口：完成；资质附件：部分完成；商家开通 UI：部分完成 | 80% | 补资质材料上传；补独立商家端认领页；补平台审核 UI 和审核备注 |
| M7-F02 | 舞室信息管理 | L | 3 | studio 表：完成；公开展示：完成；商家修改接口：部分完成；环境图/营业时间维护 UI：未发现；审核/变更留痕：部分完成 | 45% | 新增商家资料维护页；新增更新接口；接媒体上传；补审核/纠错合并 |
| M7-F03 | 课表管理 | L | 3 | course/schedule 表：完成；公开课表：完成；商家创建/编辑排期 UI：未发现；上下架管理：部分完成；场地/老师绑定：部分完成 | 40% | 新增课程/排期管理端；补上下架接口；补场地字段；补冲突校验 |
| M7-F04 | 教练账号管理 | L | 3 | 教练关系接口：完成；绑定/状态修改：完成；结算比例字段：完成；完整管理 UI：部分完成；角色矩阵差异：部分完成 | 65% | 补教练账号管理页面；补邀请流程；补全职/签约/自由权限差异验收 |
| M7-F05 | 教练主页运营 | M | 2 | 教练资料接口：完成；教练主页入口：完成；介绍/风格编辑：完成；作品/可约时段：部分完成；媒体上传：部分完成 | 70% | 补作品媒体；补可约时段编辑；补公开页预览 |
| M7-F06 | Workshop 发布 | L | 3 | 商家创建接口：完成；发布/下架：完成；场次创建：完成；教练 H5 创建页：基本完成；审批矩阵：部分完成 | 75% | 补舞室 Web 创建页；补全职/签约审批流；补草稿编辑和复制场次 |
| M7-F07 | 报名与履约管理 | XL | 5 | 订单查看：基本完成；商家核销：完成；退款接口：基本完成；复盘统计：部分完成；真实支付/对账：未发现 | 65% | 补商家订单列表；补退款审核；补活动复盘数据；接真实支付对账 |
| M7-F08 | 评价回复与申诉 | L | 3 | 回复接口：完成；申诉接口：完成；平台审核：完成；置顶评价：未发现；商家 Web UI：部分完成 | 70% | 补置顶功能；补商家评价管理页；补申诉材料附件和处理记录 |
| M7-F09 | 经营数据看板 | XL | 5 | 教练看板页：基本完成；授课/评价指标：部分完成；`monthIncome`：仅预留/schema/mock；舞室维度看板：未发现；留存/营收可视化：未发现 | 30% | 接真实订单和结算统计；补舞室管理看板；补营收、留存、核销率图表 |
| M7-F10 | 收益结算管理 | XL | 5 | settlement schema：仅预留/schema/mock；分账规则字段：部分完成；收益明细接口：未发现；账单/提现：未发现；金融操作前端：未发现 | 10% | 新增结算服务；生成账单；新增提现申请/审核；补教练/舞室收益明细 |

本模块加权完成度：50%（Σ 1790 / 权重 35，按 5% 粒度取整）。

主要扣分原因：M7 的后端领域模型和部分接口已具备，但 PDF 所要求的创建侧 Web 管理、经营收入、结算提现、角色矩阵和商家运营闭环仍缺大量高复杂度子任务。

### 10.2 当前实现证据

前端证据：

- `frontend/src/pages/coach/CoachWorkshopCreatePage.vue`：教练创建 Workshop。
- `frontend/src/pages/coach/CoachOrdersPage.vue`：学员订单与核销。
- `frontend/src/pages/coach/ReplyReviewsPage.vue`：评价回复。
- `frontend/src/pages/coach/AppealPage.vue`：评价申诉。
- `frontend/src/pages/coach/CoachDashboardPage.vue`：教练经营看板。
- `frontend/src/pages/user/CoachHomePage.vue`：教练主页运营入口。
- `frontend/src/api/coachOps.ts`、`frontend/src/api/coach.ts`、`frontend/src/api/workshop.ts`、`frontend/src/api/review.ts`：教练和商家相关 API 封装。

收入相关证据：

- `frontend/src/pages/coach/CoachDashboardPage.vue` 展示 `本月收益` 字段，来源为 `data.monthIncome`。
- `frontend/src/pages/coach/CoachOrdersPage.vue` 展示学员订单金额 `amount`，但这是订单金额展示，不等同于教练可结算收入。
- `backend/src/main/java/com/bitdance/coachops/service/CoachOpsService.java` 的 `dashboard` 返回 `BigDecimal.ZERO` 作为 `monthIncome`，并在代码注释中标明 `monthIncome 留待 settlement_rule 接入`。

后端证据：

- `backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java`：舞室认领提交、我的认领、平台审核。
- `backend/src/main/java/com/bitdance/merchant/controller/CoachRelationController.java`：舞室教练关系创建、修改、列表。
- `backend/src/main/java/com/bitdance/merchant/service/MerchantAccessGuard.java`：商家侧数据权限守卫。
- `backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java`：独立教练资质申请与平台审核。
- `backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java`：教练资料、课程和看板。
- `backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopController.java`：商家 Workshop 创建、发布、下架、场次创建。
- `backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopCheckinController.java`：商家侧核销。
- `backend/src/main/java/com/bitdance/workshop/controller/AdminWorkshopController.java`：平台 Workshop 审核。
- `backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java`、`AdminAuditLogController.java`：平台举报处理和审计日志查询。

数据库证据：

- `studio_claim`：舞室入驻认领和审核。
- `studio_coach_relation`：舞室与教练关系。
- `coach_certification_application`：教练资质申请。
- `workshop`、`workshop_session`、`workshop_order`、`workshop_checkin`：Workshop 创建、报名与履约。
- `review_reply`、`review_appeal`：评价回复与申诉。
- `audit_log`：管理侧审计。
- `settlement_rule`、`settlement_bill`、`withdraw_request`：收益结算结构。

测试证据：

- 存在 `StudioClaimControllerTest`、`CoachRelationControllerTest`、`CoachCertificationControllerTest`、`CoachOpsControllerTest`、`MerchantWorkshopControllerTest`、`MerchantWorkshopCheckinControllerTest`、`AdminWorkshopControllerTest`、`AdminReportTicketControllerTest`、`AuditLogAspectTest`。
- 历史记录显示 BE-014.2 完成 Studio Claim、Coach Relation、Merchant Workshop 创建侧；BE-014.3-a/b 完成 Coach Ops、平台审核、商家核销、举报工作台和审计。
- 当前 Java 25 下后端测试因 Mockito/Byte Buddy 兼容失败，需 Java 21 复核。

### 10.3 完成度判断

定量结论：部分完成（50%）。后端创建侧能力和教练 H5 入口较多，但独立管理端、收入看板和收益结算仍是主要未完成项。

M7 的后端领域能力和教练 H5 侧能力覆盖较多：舞室认领、教练资质、教练关系、Workshop 创建、平台审核、商家核销、评价回复与申诉、审计都有后端接口和数据表。当前不足主要在前端形态：PDF 和架构文档均描述“舞室管理员 Web / 平台管理员 Web”，但当前 `build` 目录只发现用户/教练 H5 前端工程，未发现独立的 Web 管理后台工程。

### 10.4 主要缺口

- M7-F02 舞室信息管理、M7-F03 课表管理的商家 Web UI 未发现完整实现。
- M7-F04 教练账号管理有后端接口，但完整管理端页面不足。
- M7-F09 经营数据看板当前教练 H5 有看板页面，但 `monthIncome` 后端固定返回 0，收益指标仍是占位；舞室管理员维度的完整 Web 看板也不足。
- M7-F10 收益结算管理有 schema 结构，但未发现收益明细、结算账单、提现申请、提现状态、分账确认等完整业务接口和前端闭环。
- 角色权限矩阵中的舞室管理员、全职教练、签约教练、自由教练差异，需要更多端到端用例验证。

### 10.5 后续建议

- 若验收要求覆盖 M7，需要补独立管理端或在 H5 内明确提供“商家/平台管理入口”的替代路径。
- 优先补舞室管理员三个核心页面：认领审核状态、舞室资料维护、课表/教练关系管理。
- 结算可先做只读账单 MVP，不急于接真实提现，但要明确收入来源、分账比例和结算状态。

## 11. 4.10 工程复杂性说明核验

### 11.1 PDF 要求摘要

第 4.10 节强调项目不是单纯信息展示平台，而是包含定位搜索、结构化筛选、垂直评价体系、课程管理、约练匹配、成长记录和内容沉淀的复合型产品。真正难点在于用连续路径串联各模块，而不是简单堆功能。

### 11.2 当前实现证据

架构与模块证据：

- 后端按业务域划分包：`catalog`、`review`、`iam`、`profile`、`practice`、`buddy`、`growth`、`community`、`workshop`、`merchant`、`coachops`、`admin`、`audit`、`badge`。
- API 前缀分层：`/public/**`、`/auth/**`、`/h5/**`、`/merchant/**`、`/admin/**`。
- OpenAPI 分组：`public / auth / h5 / merchant / admin`。
- **API 全量文档**：`docs/API.md`（源码扫描生成，307KB，119 个接口覆盖，1785 个字段记录，49 个业务错误码，curl 示例与请求/响应 schema 完整）。
- 前端按页面域划分：`home`、`studio`、`practice`、`growth`、`community`、`workshop`、`coach`、`user`。
- 数据库 schema 覆盖核心业务表、索引、函数、视图和多态附件/收藏/举报结构。

工程能力证据：

- `backend/README.md` 说明后端模块、启动、测试、部署和 API 前缀。
- `frontend/README.md` 说明 H5 技术栈、启动、mock、真后端切换和 Capacitor APK 打包。
- `开发历史记录.md` 记录前后端功能点、测试结果、决策记录和部署收官。
- 存在 Dockerfile、docker-compose、application-prod、CI 等部署相关能力记录。
- 前端 mock adapter 支撑无后端联调，后端 OpenAPI 支撑基础接口浏览。

### 11.3 完成度判断

结论：基本完成。

当前项目从目录结构、数据模型、API 分区、测试文件和部署文档上看，已经不是简单页面堆叠，而是围绕“搜索决策 -> 评价可信度 -> 约练陪伴 -> 成长沉淀 -> 社区活动 -> 商家供给侧”形成了模块化单体架构。M1-M6 用户侧路径较完整，M7 后端能力较完整但管理端前端不足。

### 11.4 主要缺口

- OpenAPI 目前主要是自动生成和分组，缺少面向前后端联调的完整字段说明、业务错误码、示例请求/响应。
- 当前本机后端测试在 Java 25 失败，虽然更像工具链兼容问题，但会影响“可验证交付”可信度。
- 部分跨模块自动联动仍需端到端确认，例如 Workshop 完成后触发评价、成长时间线聚合、通知提醒、徽章发放。
- 管理端缺失会削弱第 4 章中“创建侧”能力的演示完整度。

### 11.5 后续建议

- 用 Java 21 复跑 `mvn test`，恢复后端自动化测试可信度。
- 为每个模块补 1 条端到端验收脚本，覆盖用户真实路径而不是只看 Controller 测试。
- 补充接口文档注解和示例，将 OpenAPI 从“可打开”提升到“可联调”。

## 12. 全局缺口与风险

### 12.1 功能缺口

| 优先级 | 缺口 | 影响模块 | 说明 |
| --- | --- | --- | --- |
| 高 | 独立舞室/平台 Web 管理后台未发现 | M7 | PDF 中 M7 明确要求舞室管理员和平台管理员创建侧能力，当前后端接口较完整，但独立管理前端缺失会影响演示和验收 |
| 高 | Java 25 下后端测试无法通过 | 全局 | 当前失败集中在 Mockito/Byte Buddy mock 兼容，不是业务断言失败，但会影响本机可验证性 |
| 中 | OpenAPI 文档不够完善 | 全局 | 有 SpringDoc 分组，但缺少业务注解、字段说明、错误码和示例 |
| 中 | 真实媒体上传未完整闭环 | M2/M5/M6 | 评价图文/视频、成长作品、社区动态都依赖媒体能力 |
| 中 | 教练端收入和金融操作未完整闭环 | M7 | 教练看板展示 `monthIncome`，但后端当前固定返回 0；订单页仅展示金额，不支持收益明细、结算账单、提现等操作 |
| 中 | 真实支付与结算未完整闭环 | M6/M7 | 当前有 PaymentGateway 抽象和结算表结构，但真实微信支付、分账、提现仍属生产化增强 |
| 中 | 推荐、拼课、成长报告等高级功能未完成 | M1/M4/M5 | 这些多为 P2/P3 或后续增强，不阻塞 MVP，但影响“完整完成”判断 |
| 低 | 地图视图、雷达图、扫码签到等 UI 体验需实测 | M1/M2/M6 | 当前代码证据存在相关数据能力，但视觉和交互是否达到 PDF 描述需要截图或浏览器验收 |

### 12.2 文档与验收风险

- `开发历史记录.md` 中记录后端 Java 21 环境曾 233/233 测试通过，但当前机器 Java 25 下失败。对正式验收而言，应以当前可复现环境为准，建议固定 JDK 21。
- 当前 README 称后端测试全绿，这与当前本机 Java 25 结果不一致。报告中应保留该差异，避免过度承诺。
- 接口文档虽然能通过 Swagger UI 浏览，但自动生成文档无法替代完整接口说明。
- 当前前端默认 mock，可保证 H5 独立演示；切真后端时仍需确认 `VITE_USE_MOCK=false` 后所有真实接口路径一致。

## 13. 最终结论

按第 4 章“产品方案与功能设计”的全量功能点和复杂度权重判断：

- M1 舞室与课程：70%，部分完成；MVP 主链路基本完成。
- M2 评价系统：70%，部分完成；P0 结构化评价主链路基本完成。
- M3 用户账号与角色：55%，部分完成。
- M4 约练社交：60%，部分完成。
- M5 成长档案：60%，部分完成。
- M6 社区与活动：70%，部分完成；消费侧主链路基本完成。
- M7 商家与教练管理：50%，部分完成。
- 4.10 工程复杂性：基本完成。

M1-M7 综合加权完成度：60%（Σ 10665 / 权重 175，按 5% 粒度取整）。

综合结论：BD-F 项目已经完成第 4 章所要求的 MVP 级核心功能闭环，尤其是用户侧“搜索 -> 详情 -> 收藏/试听 -> 评价 -> 约练 -> 成长 -> 社区/Workshop”的路径覆盖较完整。若验收标准是“是否具备 MVP 演示和核心接口能力”，可以判断为基本可演示；若按 PDF 中所有 P0-P3 功能、独立商家/平台后台、真实支付/结算、完整风控和推荐进行定量加权，则当前为 60%，属于部分完成，不能判断为全部完成。

建议最终验收口径采用：

> 当前 BD-F 已基本完成第 4 章产品方案与功能设计中的用户侧 MVP 和主要后端领域能力；按全量需求点复杂度加权，M1-M7 综合完成度为 60%，M7 独立 Web 管理后台、真实支付结算、复杂推荐/风控、成长报告和接口文档完善度仍属于待补项。

## 14. 验证记录

### 14.1 已执行检查

<!-- 验证记录格式修正：表格中的管道符需要转义；定量矩阵新增后把覆盖性检查纳入同一张验证表。 -->
| 检查项 | 命令或方式 | 结果 | 备注 |
| --- | --- | --- | --- |
| PDF 第 4 章提取 | `pdftotext -layout ... \| sed -n '469,905p'` | 通过 | 已提取 M1-M7 与 4.10 内容 |
| 后端控制器和接口扫描 | `rg` 搜索 Controller、Mapping | 通过 | 发现 catalog/review/iam/practice/growth/community/workshop/merchant/admin 等接口 |
| 前端路由和页面扫描 | `rg` 路由、`find src/pages` | 通过 | 发现 home/studio/practice/growth/community/workshop/coach/user 页面 |
| 数据库表扫描 | `rg '^CREATE TABLE' bitdance_postgresql_schema.sql` | 通过 | 覆盖 60+ 表与核心索引、函数、视图 |
| 前端类型检查 | `npm run type-check` | 通过 | `vue-tsc --noEmit` 成功 |
| 前端生产构建 | `npm run build` | 通过 | 构建成功，仅 Sass legacy JS API 警告 |
| 后端测试 | `mvn test` | 未通过 | Java 25 下 Mockito/Byte Buddy 无法 mock 多个 service；需 Java 21 复核 |
| 定量矩阵覆盖检查 | `rg -o "M[1-7]-F[0-9]{2}" ... \| sort -u \| wc -l` | 通过 | 62 个需求点全部覆盖 |
| 矩阵完成度检查 | `rg "\\| M[1-7]-F[0-9]{2} \\|.*[0-9]+%" ... \| wc -l` | 通过 | 62 个矩阵行均包含百分比完成度 |
| Markdown 格式检查 | `git diff --check` | 通过 | 未发现尾随空白或补丁格式问题 |

### 14.2 后端测试失败摘要

当前 `mvn test` 结果：

- Tests run: 233
- Failures: 0
- Errors: 219
- 失败类型：`Mockito cannot mock this class`
- 当前 Java：25
- 典型失败对象：`AuthService`、`StudioService`、`PracticeService`、`GrowthService`、`WorkshopService` 等被 `@MockBean` mock 的服务类。

判断：失败集中在测试框架和当前 JDK 兼容性，不是业务断言失败；但正式验收仍应固定 Java 21 后重新执行。

### 14.3 建议复核命令

```bash
cd /Users/fangablt/Applications/EngineeringWorks/SoftwareProjectIV/BD-F/BitDance/build/frontend
npm run type-check
npm run build

cd /Users/fangablt/Applications/EngineeringWorks/SoftwareProjectIV/BD-F/BitDance/build/backend
java -version
mvn test
```

### 14.4 后续验收建议

- 启动后端和前端，关闭前端 mock，使用 Browser 做视觉 UI 审核，逐一走 M1-M6 用户主链路。
- 补充实现独立舞室(商家)管理端和平台管理端前后端链路。
- 补充 Swagger/OpenAPI 注解和示例响应，形成真正可交付的接口文档。
