# BD-F 第 4 章产品方案与功能设计完成度核验总结

核验日期：2026-05-23  
核验范围：`BD-F/BitDance/build` 项目当前代码、数据库 schema、项目文档与可执行验证记录。  
核验目标：判断当前 BD-F 项目是否完成 `backend/【第五组】【组长：顾远】【BitDance舞室点评项目产品立项说明书】.pdf` 第 4 章“产品方案与功能设计”中定义的 M1-M7 功能模块与 4.10 工程复杂性要求。  
输出结论口径：`已完成 / 基本完成 / 部分完成 / 未完成 / 未核验`。

## 1. 核验结论总览

总体判断：当前 BD-F 项目已经覆盖第 4 章提出的核心产品路径“搜索决策 -> 学习陪伴 -> 成长沉淀”，用户/教练 H5 端、Spring Boot 后端、PostgreSQL schema 与 mock/种子数据具备较完整的 MVP 闭环。M1-M6 的用户侧主链路完成度较高，M7 的后端和教练侧能力较完整，但独立的舞室/平台 Web 管理后台在当前目录中未发现独立前端工程，因此 M7 按“部分完成”处理。

需要特别说明：

- 当前项目不是只停留在表结构层面，已经存在可调用的 Controller、Service、Repository、前端页面和 mock/API 封装。
- 当前项目接口文档已有 SpringDoc/OpenAPI 自动分组基础，但缺少大量 `@Operation`、`@Tag`、`@Schema` 等业务注解，不能视为完善的人工接口文档。
- 当前本机 `mvn test` 在 Java 25 环境下失败，失败主因是 Mockito/Byte Buddy 无法 mock 服务类；项目声明目标 Java 21，历史记录中 Java 21 环境曾记录 233/233 通过，因此后端自动化测试需要在 Java 21 下复核。
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

### 2.3 核验方法

本次核验按“PDF 功能点 -> 前端页面/API -> 后端接口/服务 -> 数据库表结构 -> 测试或验证记录”的顺序判断，不把“存在表结构”直接等同于“功能完成”。对需要商家后台、平台后台或生产支付/结算等能力的功能，会区分“后端能力存在”和“独立管理前端是否存在”。

## 3. 模块完成度矩阵

| 模块 | PDF 级别 | 当前完成度 | 主要证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| M1 舞室与课程 | P0/P1/P2/P3 | 基本完成 | `catalog`、`favorite`、`booking` 后端包；首页、搜索、舞室/课程/教练详情、课表、试听、收藏页面；studio/course/schedule/trial/favorite 表 | 地图真实视图、舞室对比、智能推荐偏弱；商家独立 Web 维护端未发现 |
| M2 评价系统 | P0/P1/P2 | 基本完成 | `review` 后端包；写评价、评价列表、评价摘要、回复、申诉页面/API；review/review_dimension_score/review_reply/review_appeal 表 | 图文/视频附件真实上传与复杂相似文案风控仍偏弱 |
| M3 用户账号与角色 | P0/P1/P2 | 基本完成 | `iam`、`profile`、`message`、`coachops`；登录、资料、隐私、消息、教练申请/主页页面；user/profile/role/privacy/notification 表 | 微信第三方登录、登录设备异常提醒、社交账号展示在当前 H5 中未完整闭环 |
| M4 约练社交 | P1/P2/P3 | 基本完成 | `practice`、`buddy`；约练广场、发布、详情、我的约练、推荐与搭子、约练评价页面；practice/buddy/rating 表 | 拼课发起与推荐算法是弱实现或预留，取消惩罚策略未完整产品化 |
| M5 成长档案 | P1/P2/P3 | 基本完成 | `growth`、`badge`、`favorite`；成长首页、打卡、目标、作品、收藏页面；growth/badge/favorite 表 | 月/季成长报告未发现完整实现；隐私联动和作品媒体上传仍偏 MVP |
| M6 社区与活动 | P1/P2 | 基本完成 | `community`、`workshop`；社区 feed、发布、详情、话题、关注、搜索、Workshop 列表/详情/订单/签到/日历页面；content/workshop/order/checkin 表 | 转发、真实支付、退款/签到二维码、活动后评价联动仍偏 mock 或抽象 |
| M7 商家与教练管理 | P0/P1/P2 | 部分完成 | `merchant`、`coachops`、`admin`、`workshop`；教练运营 H5 页面；商家/平台后端接口；claim/relation/certification/audit/settlement 表 | 当前目录未发现独立舞室/平台 Web 管理后台；结算提现、经营看板、管理端完整 UI 不足 |
| 4.10 工程复杂性 | - | 基本完成 | 模块化后端包、统一 API 前缀、OpenAPI 分组、Docker/CI 文档、PostgreSQL schema、Redis 缓存配置 | 当前接口文档注解不完善；Java 25 测试环境失败需 Java 21 复核 |

<!-- 写入批次 2：M1 舞室与课程、M2 评价系统。 -->

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

结论：基本完成。

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

结论：基本完成。

M2 的核心 P0 功能已经实现：三类评价对象、维度评分、权重分层、评价汇总和风险状态均有后端模型与接口支撑。商家回复和申诉也已覆盖，前端存在写评价、评价展示、回复与申诉页面。

### 5.4 主要缺口

- M2-F06 图文/视频评价虽然 schema 有媒体附件通用表，但未核验到完整上传、绑定、预览、审核闭环。
- M2-F07 的复杂风控如相似文案、设备/IP 聚集、异常波动检测仍属于基础实现或待增强。
- 评价雷达图展示是否达到 PDF 所述视觉标准，需要进一步做 UI 实测截图核验。

### 5.5 后续建议

- 明确评价附件上传接口和前端交互，补充媒体上传到评价的端到端用例。
- 给 `ReviewController`、DTO 和 schema 模型补充 OpenAPI 注解，使评价接口文档可直接用于联调验收。
- 增加至少一组真实数据下的评价摘要截图或接口响应样例，作为 M2 验收证据。

<!-- 写入批次 3：M3 用户账号与角色、M4 约练社交。 -->

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

结论：基本完成。

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

结论：基本完成。

M4 的 P1/P2 主链路基本完成：发布约练、广场、详情、申请、接受/拒绝、我的约练、过期关闭、约练互评和搭子沉淀都有对应实现。前端也存在约练广场、发布、详情、推荐与搭子、评价页面。

### 7.4 主要缺口

- M4-F04 推荐算法更多像规则或页面入口，未核验到复杂推荐服务。
- M4-F07 拼课发起目前主要体现为 `group_class_intent` 表结构预留，未发现完整前端和后端业务闭环。
- PDF 中“频繁取消者限制发布权限”未核验到完整惩罚策略。
- 位置推荐目前更偏城市/筛选维度，精确地理匹配能力有限。

### 7.5 后续建议

- 将推荐拆成最小可验收规则：同城、同舞种、同水平、近期活跃、未拉黑。
- 补齐拼课从意向发布、人数达标、通知舞室到商家处理的最短闭环。
- 增加取消次数统计和冷却策略，并在发布约练接口中统一校验。

<!-- 写入批次 4：M5 成长档案、M6 社区与活动。 -->

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

结论：基本完成。

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

结论：基本完成。

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

<!-- 写入批次 5：M7 商家与教练管理、4.10 工程复杂性。 -->

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

### 10.2 当前实现证据

前端证据：

- `frontend/src/pages/coach/CoachWorkshopCreatePage.vue`：教练创建 Workshop。
- `frontend/src/pages/coach/CoachOrdersPage.vue`：学员订单与核销。
- `frontend/src/pages/coach/ReplyReviewsPage.vue`：评价回复。
- `frontend/src/pages/coach/AppealPage.vue`：评价申诉。
- `frontend/src/pages/coach/CoachDashboardPage.vue`：教练经营看板。
- `frontend/src/pages/user/CoachHomePage.vue`：教练主页运营入口。
- `frontend/src/api/coachOps.ts`、`frontend/src/api/coach.ts`、`frontend/src/api/workshop.ts`、`frontend/src/api/review.ts`：教练和商家相关 API 封装。

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

结论：部分完成。

M7 的后端领域能力和教练 H5 侧能力覆盖较多：舞室认领、教练资质、教练关系、Workshop 创建、平台审核、商家核销、评价回复与申诉、审计都有后端接口和数据表。当前不足主要在前端形态：PDF 和架构文档均描述“舞室管理员 Web / 平台管理员 Web”，但当前 `build` 目录只发现用户/教练 H5 前端工程，未发现独立的 Web 管理后台工程。

### 10.4 主要缺口

- M7-F02 舞室信息管理、M7-F03 课表管理的商家 Web UI 未发现完整实现。
- M7-F04 教练账号管理有后端接口，但完整管理端页面不足。
- M7-F09 经营数据看板当前教练 H5 有看板页面，舞室管理员维度的完整 Web 看板不足。
- M7-F10 收益结算管理有 schema 结构，但未发现完整业务接口和前端闭环。
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

<!-- 写入批次 6：缺口、风险、最终结论、验证命令记录。 -->

## 12. 全局缺口与风险

### 12.1 功能缺口

| 优先级 | 缺口 | 影响模块 | 说明 |
| --- | --- | --- | --- |
| 高 | 独立舞室/平台 Web 管理后台未发现 | M7 | PDF 中 M7 明确要求舞室管理员和平台管理员创建侧能力，当前后端接口较完整，但独立管理前端缺失会影响演示和验收 |
| 高 | Java 25 下后端测试无法通过 | 全局 | 当前失败集中在 Mockito/Byte Buddy mock 兼容，不是业务断言失败，但会影响本机可验证性 |
| 中 | OpenAPI 文档不够完善 | 全局 | 有 SpringDoc 分组，但缺少业务注解、字段说明、错误码和示例 |
| 中 | 真实媒体上传未完整闭环 | M2/M5/M6 | 评价图文/视频、成长作品、社区动态都依赖媒体能力 |
| 中 | 真实支付与结算未完整闭环 | M6/M7 | 当前有 PaymentGateway 抽象和结算表结构，但真实微信支付、分账、提现仍属生产化增强 |
| 中 | 推荐、拼课、成长报告等高级功能偏弱 | M1/M4/M5 | 这些多为 P2/P3 或后续增强，不阻塞 MVP，但影响“完整完成”判断 |
| 低 | 地图视图、雷达图、扫码签到等 UI 体验需实测 | M1/M2/M6 | 当前代码证据存在相关数据能力，但视觉和交互是否达到 PDF 描述需要截图或浏览器验收 |

### 12.2 文档与验收风险

- `开发历史记录.md` 中记录后端 Java 21 环境曾 233/233 测试通过，但当前机器 Java 25 下失败。对正式验收而言，应以当前可复现环境为准，建议固定 JDK 21。
- 当前 README 称后端测试全绿，这与当前本机 Java 25 结果不一致。报告中应保留该差异，避免过度承诺。
- 接口文档虽然能通过 Swagger UI 浏览，但自动生成文档无法替代完整接口说明。
- 当前前端默认 mock，可保证 H5 独立演示；切真后端时仍需确认 `VITE_USE_MOCK=false` 后所有真实接口路径一致。

## 13. 最终结论

按第 4 章“产品方案与功能设计”的功能覆盖度判断：

- M1 舞室与课程：基本完成。
- M2 评价系统：基本完成。
- M3 用户账号与角色：基本完成。
- M4 约练社交：基本完成。
- M5 成长档案：基本完成。
- M6 社区与活动：基本完成。
- M7 商家与教练管理：部分完成。
- 4.10 工程复杂性：基本完成。

综合结论：BD-F 项目已经完成第 4 章所要求的 MVP 级核心功能闭环，尤其是用户侧“搜索 -> 详情 -> 收藏/试听 -> 评价 -> 约练 -> 成长 -> 社区/Workshop”的路径覆盖较完整。若验收标准是“是否具备 MVP 演示和核心接口能力”，可以判断为基本完成；若验收标准是“PDF 中所有 P0-P3 功能、独立商家/平台后台、真实支付/结算、完整风控和推荐全部完成”，则不能判断为全部完成。

建议最终验收口径采用：

> 当前 BD-F 已基本完成第 4 章产品方案与功能设计中的用户侧 MVP 和主要后端领域能力；M7 独立 Web 管理后台、真实支付结算、复杂推荐/风控、成长报告和接口文档完善度仍属于待补项。

## 14. 验证记录

### 14.1 已执行检查

| 检查项 | 命令或方式 | 结果 | 备注 |
| --- | --- | --- | --- |
| PDF 第 4 章提取 | `pdftotext -layout ... | sed -n '469,905p'` | 通过 | 已提取 M1-M7 与 4.10 内容 |
| 后端控制器和接口扫描 | `rg` 搜索 Controller、Mapping | 通过 | 发现 catalog/review/iam/practice/growth/community/workshop/merchant/admin 等接口 |
| 前端路由和页面扫描 | `rg` 路由、`find src/pages` | 通过 | 发现 home/studio/practice/growth/community/workshop/coach/user 页面 |
| 数据库表扫描 | `rg '^CREATE TABLE' bitdance_postgresql_schema.sql` | 通过 | 覆盖 60+ 表与核心索引、函数、视图 |
| 前端类型检查 | `npm run type-check` | 通过 | `vue-tsc --noEmit` 成功 |
| 前端生产构建 | `npm run build` | 通过 | 构建成功，仅 Sass legacy JS API 警告 |
| 后端测试 | `mvn test` | 未通过 | Java 25 下 Mockito/Byte Buddy 无法 mock 多个 service；需 Java 21 复核 |

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

- 用 Java 21 重新执行后端全量测试，确认是否恢复 233/233 通过。
- 启动后端和前端，关闭前端 mock，逐一走 M1-M6 用户主链路。
- 若需要验收 M7，应补独立管理端或准备明确的接口级演示脚本。
- 补充 Swagger/OpenAPI 注解和示例响应，形成真正可交付的接口文档。
