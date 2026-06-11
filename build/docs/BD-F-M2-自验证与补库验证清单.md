# BD-F M2 自验证与补库验证清单

更新时间：2026-06-10

最新验证状态：2026-06-10 已在本地 `8082` 后端、`5173` 前端、SSH 数据库隧道和本地 Redis 环境下复核。当前账号 `13900000008` 已通过验证码登录，M2 发布评价、我的评价、回复评价三条 UI 路径均可走通。

## 说明

- 本清单把 M2 验收拆成两类：
  - `M2 自验证`：只依赖当前前后端评价模块即可完成。
  - `补库验证`：允许通过数据库注入前置事实或结果样例，辅助当前 M2 验收。
- 这里的“补库验证”不等于“可以替代真实业务验证”，而是为了更快准备样例数据或缩短手工链路。

## A. M2 自验证清单

| M2 功能点 | 验证目标 | 当前是否可直接验 | 推荐入口 | 验收重点 |
| --- | --- | --- | --- | --- |
| `M2-F01` 舞室评价 | 能提交舞室结构化评价并回看 | 可以 | 舞室详情页、`/publish/review`、`/me/reviews` | 维度、总分、内容、列表回流 |
| `M2-F02` 老师评价基础链路 | 能提交老师结构化评价并回看 | 可以 | 老师详情页、`/publish/review`、`/me/reviews` | 老师维度是否正确、对象是否正确 |
| `M2-F03` 课程评价基础链路 | 能提交课程结构化评价并回看 | 可以 | 课程详情页、`/publish/review`、`/me/reviews` | 课程维度、详情页评价区、我的评价 |
| `M2-F05` 评分聚合展示 | 汇总分、已验证数、各维度均分是否显示正确 | 可以 | 详情页评价聚合区、`/public/reviews/summary` | 聚合值与已有评价数据一致 |
| `M2-F07` 基础风控展示 | 本人是否能看到 `pending/folded/hidden` 等状态 | 可以 | `/me/reviews` | 风险说明、状态标签、回流一致性 |
| `M2-F08` 用户侧申诉 | 能提交申诉并在我的记录中回看 | 可以 | 评价页、`/me/reviews`、申诉入口 | 申诉状态、备注、结果回流 |
| `M2-F06` 媒体弱验收 | 已挂接媒体的评价能否显示 | 可以 | 公开评价区、我的评价 | 图片/视频缩略展示、空态兼容 |
| `M2-F08` 商家/教练回复 | 能读取真实评价队列、提交回复并回显 | 可以 | `/coach/replies` | 待回复/已回复切换、回复内容回显 |

## B. 可补库验证清单

| 目标 | 可补的表 | 关键字段 | 适合验证什么 | 不适合直接证明什么 |
| --- | --- | --- | --- | --- |
| 为课程/舞室评价准备试听来源事实 | `trial_booking` | `user_id`、`studio_id`、`course_id`、`booking_status` | 后续走真实写评价接口时的 verified 前置条件 | 不能直接证明评价服务真的会自动识别，仍需走创建评价 |
| 为新号/老号风控准备对照账号 | `app_user` | `id`、`created_at` | 新号降权、账号龄风控样例准备 | 不能只改最终 `review.weight_factor` 来宣称风控通过 |
| 为申诉结果回流准备样例 | `review_appeal`、`review` | `appeal_status`、`review_status`、`review_remark` | 我的申诉、我的评价、公开评价状态展示 | 不能证明审批接口权限和状态迁移逻辑正确 |
| 为评价媒体展示准备样例 | `media_asset`、`media_attachment` | `url`、`media_type`、`target_type`、`target_id` | 公开评价区和我的评价中的媒体展示 | 不能证明上传、审核、删除链路成立 |
| 为管理员/商家角色准备账号 | `user_role_binding` | `user_id`、`role_code` | 后续用不同账号调角色相关接口 | 不能单独证明前端入口、权限校验和审核闭环都正确 |
| 为 Workshop 来源事实准备样例 | `workshop_order`、`workshop_checkin` | 订单状态、签到状态、签到码 | 为 `order/checkin` 可信评价准备前置事实 | 不能证明 M6 已自动邀请评价，仍需活动入口触发验证 |
| 为回复队列准备样例 | `review`、`review_reply` | `review_status`、`reply_content`、`is_official` | 教练/商家回复页的待回复/已回复切换 | 不能证明角色权限隔离完整 |

## C. 建议执行方式

### 1. 纯 M2 自验证

- 先完成三类对象评价主链路：
  - 舞室
  - 老师
  - 课程
- 再检查两类读路径：
  - 公开评价与评分聚合
  - 我的评价与申诉回流

### 2. 补库 + 再走接口

- 这是当前最推荐的方式。
- 典型场景：
  - 先补 `trial_booking`
  - 再调用创建评价接口
  - 验 `isVerified`、`weightFactor`
- 已验证扩展场景：
  - 先补 `workshop_order`
  - 再调用创建评价接口并带 `sourceType=order`
  - 验 `isVerified=true`、`weightFactor=1.500`
- 已验证扩展场景：
  - 先补 `workshop_checkin`
  - 再调用创建评价接口并带 `sourceType=checkin`
  - 验 `isVerified=true`、`weightFactor=1.500`
- 再如：
  - 先改 `app_user.created_at`
  - 再用对应账号发评价
  - 验新号风控差异

### 3. 只补库看展示

- 适合的只有样例展示准备：
  - 评价媒体
  - 申诉结果态
  - 某些风险状态标签
- 不要把这类验证写成“业务已通过”，应明确标注为“结果态展示已核对”。

## D. 最小验收组合

### 组合 1：M2 本体最小闭环

- 提交 1 条舞室评价
- 提交 1 条老师评价
- 提交 1 条课程评价
- 回看 `/me/reviews`
- 查看任一对象的评价汇总

### 组合 2：补库辅助的可信评价闭环

- 补 1 条 `trial_booking`
- 用同一用户提交 1 条课程或舞室评价
- 检查返回结果中的 `isVerified` 与 `weightFactor`
- 已扩展验证：
  - 补 `workshop_order` 后提交 `sourceType=order` 舞室评价
  - 补 `workshop_checkin` 后提交 `sourceType=checkin` 老师评价
  - UI 回看 `/me/reviews`，确认已验证来源和权重展示

### 组合 3：补库辅助的申诉展示闭环

- 准备 1 条 `published` 或 `folded` 的评价
- 提交申诉或直接补 `review_appeal`
- 检查我的评价与我的申诉中的状态回流

### 组合 4：补库辅助的媒体展示闭环

- 给已有 `review` 挂 1 到 3 条 `media_asset` + `media_attachment`
- 检查公开评价区和我的评价中的媒体展示

### 组合 5：补库辅助的回复治理闭环

- 准备 1 条 `published` 或 `folded` 评价
- 打开 `/coach/replies`
- 在“待回复”中提交官方回复
- 切到“已回复”，检查刚提交的回复内容回显

## 当前验证记录

| 验证项 | 结果 | 当前证据 |
| --- | --- | --- |
| 登录态 | 通过 | UI 使用 `13900000008` + 验证码 `123456` 登录成功 |
| 我的评价 | 通过 | `/me/reviews` 展示新提交评价、种子评价、已验证来源、权重、媒体和申诉状态 |
| 发布评价 | 通过 | `/publish/review?targetType=coach&targetId=100001&sourceType=checkin&sourceRefId=190001` 提交后回流到我的评价 |
| 可信来源接口 | 通过 | `trial/order/checkin` 三类 `POST /api/h5/reviews` 均返回 `isVerified=true` |
| `M1-F08 -> M2-F03/M2-F04` 试听来源联验 | 通过 | 预约 `100014` 完成 `pending -> confirmed -> attended`；课程评价 `222100007` 返回 `isVerified=true`、`verifiedSourceType=trial`、`weightFactor=1.500`；`/me/reviews` 可回看已验证来源 |
| 回复队列 | 通过 | `/coach/replies` 读取真实评价队列，提交回复后在“已回复”回显 |
| 媒体展示弱验收 | 通过 | 种子评价在我的评价中显示图片和视频数量 |
| Redis | 通过 | Docker 容器 `backend-redis-1` 已运行，`6379` 本机监听 |
| 前端类型检查 | 通过 | `npm run type-check` |
| 前端生产构建 | 通过 | `npm run build`，仅有既有 Sass deprecation warning |
| 后端编译 | 通过 | `mvn -q -DskipTests compile` |
| M2 后端测试 | 通过 | `mvn -q -Dtest=ReviewControllerTest,ReviewReplyControllerTest,ReviewAppealControllerTest test` |
| 空白检查 | 通过 | `git diff --check` |
| 平台审批申诉 | 未完成 | 需管理员审批链路验证，不能仅靠补库认定完成 |
| 真实上传链路 | 未完成 | 当前为外链/模拟媒体元数据，未接对象存储上传 |

## 本清单结论

- 当前最适合先做的是 `M2 自验证`，因为评价主链路已经成型。
- 最有价值的补库辅助项是：
  - `trial_booking`
  - `workshop_order / workshop_checkin`
  - `app_user.created_at`
  - `review_appeal`
  - `review_reply`
  - `media_asset / media_attachment`
- 已完成“补库 + 再走接口 + UI 回看”的项目包括：
  - 试听来源评价
  - Workshop 订单来源评价
  - Workshop 签到来源评价
  - 商家/教练回复回显
- 若目标是“证明规则真实生效”，仍不要停在纯补库展示；必须保留接口创建和 UI 回看记录。
