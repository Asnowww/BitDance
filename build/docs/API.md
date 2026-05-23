# BitDance 后端 API 文档

本文档由当前仓库源码扫描生成。项目为 BitDance 舞室点评/课程/社区/约练/活动平台后端，基于 Spring Boot 3.3.4、Java 21、Spring MVC、Spring Security、Spring Data JPA、Redis、PostgreSQL 与 springdoc-openapi 构建，提供 public、auth、h5、merchant、admin 等分层 REST API。

## Base URL

- 本地默认：`http://localhost:8080/api`
- 配置来源：`backend/src/main/resources/application.yml` 中 `server.port: 8080`、`server.servlet.context-path: /api`

## 鉴权方式说明

- `/api/public/**`、`/api/auth/**`、`/api/callback/**`：匿名可访问。
- `/api/h5/**`：需要 `Authorization: Bearer <JWT>`，任意已登录用户。
- `/api/merchant/**`：需要 `Authorization: Bearer <JWT>`，角色 `STUDIO_ADMIN` 或 `PLATFORM_ADMIN`。
- `/api/admin/**`：需要 `Authorization: Bearer <JWT>`，角色 `PLATFORM_ADMIN`。
- 服务端使用 stateless JWT，不使用 Session。

## 统一响应格式示例

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```

## 统一错误码表

| code | message | 触发场景 |
|------|---------|----------|
| ALREADY_CHECKED_IN | 已签到的订单不可退款 | workshop/service/WorkshopService.java |
| APPEAL_DUPLICATED | 已有待处理的申诉 | review/service/ReviewAppealService.java |
| APPEAL_NOT_FOUND | 申诉不存在 | review/service/ReviewAppealService.java |
| APPEAL_STATE_CONFLICT | 申诉状态  | review/service/ReviewAppealService.java |
| BOOKING_DUPLICATED | 已有未完结的同课程预约 | booking/service/TrialBookingService.java |
| BOOKING_NOT_FOUND | 预约不存在 | booking/service/MerchantTrialBookingService.java<br>booking/service/TrialBookingService.java |
| BOOKING_STATE_CONFLICT | 当前状态  | booking/service/MerchantTrialBookingService.java<br>booking/service/TrialBookingService.java |
| BUDDY_NOT_FOUND | 搭子关系不存在 | buddy/service/BuddyService.java |
| CERT_DUPLICATED | 已有待处理的资质申请 | coachops/service/CoachCertificationService.java |
| CERT_NOT_FOUND | 资质申请不存在 | coachops/service/CoachCertificationService.java |
| CERT_STATE_CONFLICT | 申请状态  | coachops/service/CoachCertificationService.java |
| CHECKIN_CODE_INVALID | 签到码错误 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| CHECKIN_NOT_FOUND | 打卡不存在 | growth/service/GrowthService.java |
| CHECKIN_TICKET_NOT_FOUND | 签到码不存在 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| CHECKIN_TOO_EARLY | 签到时间未到（开课前 1 小时内） | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| CHECKIN_TOO_LATE | 签到时间已过 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| CLAIM_DUPLICATED | 已有待处理的认领申请 | merchant/service/StudioClaimService.java |
| CLAIM_NOT_FOUND | 认领申请不存在 | merchant/service/StudioClaimService.java |
| CLAIM_STATE_CONFLICT | 申请状态  | merchant/service/StudioClaimService.java |
| COACH_NOT_APPROVED | 教练资质尚未通过审核 | coachops/service/CoachAccessGuard.java |
| COACH_NOT_FOUND | 教练不存在 | catalog/service/CoachService.java<br>coachops/service/CoachAccessGuard.java<br>merchant/service/CoachRelationService.java |
| COMMENT_NOT_FOUND | 评论不存在 | community/service/CommunityService.java |
| COURSE_NOT_FOUND | 课程不存在 | booking/service/TrialBookingService.java<br>catalog/service/CourseService.java |
| COURSE_OFFLINE | 课程不可预约 | booking/service/TrialBookingService.java<br>catalog/service/CourseService.java |
| FORBIDDEN | 无权操作他人预约 | booking/service/TrialBookingService.java<br>buddy/service/BuddyService.java<br>community/service/CommunityService.java<br>growth/service/GrowthService.java<br>... |
| INTERNAL_ERROR | 服务异常，请稍后再试 | GlobalExceptionHandler.java |
| INVALID_ARGUMENT | 不能评价自己 | buddy/service/BuddyService.java<br>catalog/service/CourseService.java<br>community/service/CommunityService.java<br>favorite/service/FavoriteService.java<br>... |
| JOIN_DUPLICATED | 已有未结束的申请 | practice/service/PracticeService.java |
| JOIN_REQUEST_NOT_FOUND | 申请不存在 | practice/service/PracticeService.java |
| JOIN_STATE_CONFLICT | 当前状态  | practice/service/PracticeService.java |
| NOTIFICATION_NOT_FOUND | 消息不存在 | message/service/NotificationService.java |
| ORDER_NOT_FOUND | 订单不存在 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| ORDER_STATE_CONFLICT | 仅已支付订单可核销 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| PAYMENT_FAILED | 支付失败： | workshop/service/WorkshopService.java |
| POST_NOT_FOUND | 动态不存在 | community/service/CommunityService.java |
| PRACTICE_FULL | 已满员 | practice/service/PracticeService.java |
| PRACTICE_NOT_FOUND | 约练不存在 | buddy/service/BuddyService.java<br>practice/service/PracticeService.java |
| PRACTICE_STATE_CONFLICT | 当前状态  | buddy/service/BuddyService.java<br>practice/service/PracticeService.java |
| RATING_DUPLICATED | 已对该参与者评价过 | buddy/service/BuddyService.java |
| RELATION_DUPLICATED | 已有进行中的合作关系 | merchant/service/CoachRelationService.java |
| RELATION_NOT_FOUND | 合作关系不存在 | merchant/service/CoachRelationService.java |
| REPLY_NOT_FOUND | 回复不存在 | review/service/ReviewReplyService.java |
| REPORT_DUPLICATED | 已存在未处理的举报 | community/service/CommunityService.java |
| REPORT_NOT_FOUND | 举报工单不存在 | admin/service/AdminReportTicketService.java |
| REPORT_STATE_CONFLICT | 工单状态  | admin/service/AdminReportTicketService.java |
| REVIEW_NOT_FOUND | 评价不存在 | review/service/ReviewAppealService.java<br>review/service/ReviewReplyService.java<br>review/service/ReviewService.java |
| REVIEW_STATE_CONFLICT | 当前评价状态不可申诉 | review/service/ReviewAppealService.java<br>review/service/ReviewReplyService.java |
| SESSION_NOT_AVAILABLE | 场次不可报名 | workshop/service/WorkshopService.java |
| SESSION_NOT_FOUND | 场次不存在 | workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/WorkshopService.java |
| SESSION_STARTED | 场次已开始 | workshop/service/WorkshopService.java |
| SIGNUP_CLOSED | 报名已截止 | workshop/service/WorkshopService.java |
| SMS_COOLDOWN | 请稍后再试 | iam/service/SmsCodeService.java |
| SMS_EXPIRED | 验证码已过期 | iam/service/SmsCodeService.java |
| SMS_INVALID | 验证码错误 | iam/service/SmsCodeService.java |
| STUDIO_INACTIVE | 舞室已下架 | catalog/service/StudioService.java |
| STUDIO_NOT_FOUND | 舞室不存在 | catalog/service/StudioService.java<br>merchant/service/StudioClaimService.java |
| TOPIC_NOT_FOUND | 话题不存在 | community/service/CommunityService.java |
| UNAUTHORIZED | 未登录 | iam/security/CurrentUser.java |
| USER_NOT_FOUND | 用户不存在 | coachops/service/CoachCertificationService.java<br>iam/controller/MeController.java<br>profile/service/ProfileService.java<br>review/service/ReviewService.java |
| WORK_NOT_FOUND | 作品不存在 | growth/service/GrowthService.java |
| WORKSHOP_AUDIT_STATE_CONFLICT | 审核状态  | workshop/service/AdminWorkshopService.java |
| WORKSHOP_FULL | 场次已满 | workshop/service/WorkshopService.java |
| WORKSHOP_NOT_APPROVED | Workshop 未通过审核，无法上架 | workshop/service/MerchantWorkshopService.java |
| WORKSHOP_NOT_FOUND | Workshop 不存在 | workshop/service/AdminWorkshopService.java<br>workshop/service/MerchantWorkshopCheckinService.java<br>workshop/service/MerchantWorkshopService.java<br>workshop/service/WorkshopService.java |
| WORKSHOP_NOT_PUBLISHED | Workshop 未上架 | workshop/service/WorkshopService.java |
| WORKSHOP_STATE_CONFLICT | 当前状态  | workshop/service/MerchantWorkshopService.java |

## 平台管理

### 搜索
- **路径**：`GET /api/admin/audit-log`
- **描述**：搜索接口（由 AdminAuditLogController.search 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | actor | query | integer | 否 | actor 字段（推断） | 1 |
  | action | query | String | 否 | action 字段（推断） | TODO: 待补充 |
  | targetType | query | String | 否 | 目标类型（推断） | studio |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/audit-log?actor=1&action=demo&targetType=studio&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<AuditLogDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.actorUserId | integer | actorUserId 字段（推断） |
  | data.actorRoleCode | String | actorRoleCode 字段（推断） |
  | data.actionCode | String | actionCode 字段（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "actorUserId": 1,
        "actorRoleCode": "TODO: 待补充",
        "actionCode": "TODO: 待补充",
        "targetType": "studio",
        "targetId": 1,
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/admin/controller/AdminAuditLogController.java:24；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 列表查询
- **路径**：`GET /api/admin/report-tickets`
- **描述**：列表查询接口（由 AdminReportTicketController.list 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | status | query | String | 否 | 状态（推断） | pending |
  | targetType | query | String | 否 | 目标类型（推断） | studio |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/report-tickets?status=pending&targetType=studio&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<ReportTicketDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reporterUserId | integer | reporterUserId 字段（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.reasonCode | String | reasonCode 字段（推断） |
  | data.reasonDetail | String | reasonDetail 字段（推断） |
  | data.reportStatus | String | reportStatus 字段（推断） |
  | data.handledByUserId | integer | handledByUserId 字段（推断） |
  | data.handledAt | OffsetDateTime | handledAt 字段（推断） |
  | data.handleResult | String | handleResult 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "reporterUserId": 1,
        "targetType": "studio",
        "targetId": 1,
        "reasonCode": "spam",
        "reasonDetail": "TODO: 待补充",
        "reportStatus": "pending",
        "handledByUserId": 1,
        "handledAt": "2026-05-23T10:00:00+08:00",
        "handleResult": "TODO: 待补充",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java:28；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 开始处理
- **路径**：`POST /api/admin/report-tickets/{id}/process`
- **描述**：开始处理接口（由 AdminReportTicketController.process 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/report-tickets/1/process" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReportTicketDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reporterUserId | integer | reporterUserId 字段（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.reasonCode | String | reasonCode 字段（推断） |
  | data.reasonDetail | String | reasonDetail 字段（推断） |
  | data.reportStatus | String | reportStatus 字段（推断） |
  | data.handledByUserId | integer | handledByUserId 字段（推断） |
  | data.handledAt | OffsetDateTime | handledAt 字段（推断） |
  | data.handleResult | String | handleResult 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reporterUserId": 1,
    "targetType": "studio",
    "targetId": 1,
    "reasonCode": "spam",
    "reasonDetail": "TODO: 待补充",
    "reportStatus": "pending",
    "handledByUserId": 1,
    "handledAt": "2026-05-23T10:00:00+08:00",
    "handleResult": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java:38；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 关闭
- **路径**：`POST /api/admin/report-tickets/{id}/close`
- **描述**：关闭接口（由 AdminReportTicketController.close 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | handleResult | body | String | 否 | handleResult 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/report-tickets/1/close" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "handleResult": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReportTicketDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reporterUserId | integer | reporterUserId 字段（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.reasonCode | String | reasonCode 字段（推断） |
  | data.reasonDetail | String | reasonDetail 字段（推断） |
  | data.reportStatus | String | reportStatus 字段（推断） |
  | data.handledByUserId | integer | handledByUserId 字段（推断） |
  | data.handledAt | OffsetDateTime | handledAt 字段（推断） |
  | data.handleResult | String | handleResult 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reporterUserId": 1,
    "targetType": "studio",
    "targetId": 1,
    "reasonCode": "spam",
    "reasonDetail": "TODO: 待补充",
    "reportStatus": "pending",
    "handledByUserId": 1,
    "handledAt": "2026-05-23T10:00:00+08:00",
    "handleResult": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/admin/report-tickets/{id}/reject`
- **描述**：拒绝接口（由 AdminReportTicketController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | handleResult | body | String | 否 | handleResult 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/report-tickets/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "handleResult": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReportTicketDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reporterUserId | integer | reporterUserId 字段（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.reasonCode | String | reasonCode 字段（推断） |
  | data.reasonDetail | String | reasonDetail 字段（推断） |
  | data.reportStatus | String | reportStatus 字段（推断） |
  | data.handledByUserId | integer | handledByUserId 字段（推断） |
  | data.handledAt | OffsetDateTime | handledAt 字段（推断） |
  | data.handleResult | String | handleResult 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reporterUserId": 1,
    "targetType": "studio",
    "targetId": 1,
    "reasonCode": "spam",
    "reasonDetail": "TODO: 待补充",
    "reportStatus": "pending",
    "handledByUserId": 1,
    "handledAt": "2026-05-23T10:00:00+08:00",
    "handleResult": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/admin/controller/AdminReportTicketController.java:51；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 徽章

### 徽章 listActive
- **路径**：`GET /api/public/badges/definitions`
- **描述**：徽章 listActive接口（由 BadgeDefinitionController.listActive 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/badges/definitions" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<BadgeDefinitionDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.badgeCode | String | badgeCode 字段（推断） |
  | data.badgeName | String | badgeName 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.iconAssetId | integer | iconAssetId 字段（推断） |
  | data.ruleType | String | ruleType 字段（推断） |
  | data.ruleConfig | String | ruleConfig 字段（推断） |
  | data.status | String | 状态（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "badgeCode": "TODO: 待补充",
      "badgeName": "TODO: 待补充",
      "description": "TODO: 待补充",
      "iconAssetId": 1,
      "ruleType": "TODO: 待补充",
      "ruleConfig": "TODO: 待补充",
      "status": "pending"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/badge/controller/BadgeDefinitionController.java:23；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 体验预约

### 确认
- **路径**：`POST /api/merchant/trial-bookings/{id}/confirm`
- **描述**：确认接口（由 MerchantTrialBookingController.confirm 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/trial-bookings/1/confirm" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/MerchantTrialBookingController.java:25；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/merchant/trial-bookings/{id}/reject`
- **描述**：拒绝接口（由 MerchantTrialBookingController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | reason | body | String | 否 | 原因（推断）；校验：Size(max = 500) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/trial-bookings/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reason": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/MerchantTrialBookingController.java:30；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 标记到店
- **路径**：`POST /api/merchant/trial-bookings/{id}/attend`
- **描述**：标记到店接口（由 MerchantTrialBookingController.attend 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/trial-bookings/1/attend" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/MerchantTrialBookingController.java:39；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 标记未到店
- **路径**：`POST /api/merchant/trial-bookings/{id}/no-show`
- **描述**：标记未到店接口（由 MerchantTrialBookingController.noShow 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/trial-bookings/1/no-show" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/MerchantTrialBookingController.java:44；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建
- **路径**：`POST /api/h5/trial-bookings`
- **描述**：创建接口（由 TrialBookingController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | courseId | body | integer | 是 | 课程 ID（推断） | 1 |
  | courseScheduleId | body | integer | 否 | 课程排期 ID（推断） | 1 |
  | contactPhone | body | String | 否 | contactPhone 字段（推断） | 13800138000 |
  | bookingNote | body | String | 否 | bookingNote 字段（推断）；校验：Size(max = 500) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/trial-bookings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "courseId": 1,
  "courseScheduleId": 1,
  "contactPhone": 13800138000,
  "bookingNote": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/TrialBookingController.java:29；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 取消
- **路径**：`POST /api/h5/trial-bookings/{id}/cancel`
- **描述**：取消接口（由 TrialBookingController.cancel 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | reason | body | String | 否 | 原因（推断）；校验：Size(max = 500) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/trial-bookings/1/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reason": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BookingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 1,
    "courseScheduleId": 1,
    "studioId": 1,
    "bookingStatus": "pending",
    "contactPhone": 13800138000,
    "bookingNote": "TODO: 待补充",
    "confirmedAt": "2026-05-23T10:00:00+08:00",
    "attendedAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "cancelReason": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/TrialBookingController.java:34；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/trial-bookings`
- **描述**：查询我的列表接口（由 TrialBookingController.listMine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/trial-bookings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<BookingDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.bookingStatus | String | bookingStatus 字段（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.bookingNote | String | bookingNote 字段（推断） |
  | data.confirmedAt | OffsetDateTime | confirmedAt 字段（推断） |
  | data.attendedAt | OffsetDateTime | attendedAt 字段（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.cancelReason | String | cancelReason 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "courseId": 1,
      "courseScheduleId": 1,
      "studioId": 1,
      "bookingStatus": "pending",
      "contactPhone": 13800138000,
      "bookingNote": "TODO: 待补充",
      "confirmedAt": "2026-05-23T10:00:00+08:00",
      "attendedAt": "2026-05-23T10:00:00+08:00",
      "canceledAt": "2026-05-23T10:00:00+08:00",
      "cancelReason": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BOOKING_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BOOKING_DUPLICATED、BOOKING_NOT_FOUND、BOOKING_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/booking/controller/TrialBookingController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 搭子互评

### 提交互评
- **路径**：`POST /api/h5/practices/{postId}/ratings`
- **描述**：提交互评接口（由 BuddyController.rate 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | postId | path | integer | 是 | postId 字段（推断） | 1 |
  | toUserId | body | integer | 是 | toUserId 字段（推断） | 1 |
  | punctuality | body | integer | 是 | punctuality 字段（推断）；校验：Min(1), Max(5) | 1 |
  | friendliness | body | integer | 是 | friendliness 字段（推断）；校验：Min(1), Max(5) | 1 |
  | skillMatch | body | integer | 是 | skillMatch 字段（推断）；校验：Min(1), Max(5) | 1 |
  | comment | body | String | 否 | comment 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practices/1/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "toUserId": 1,
  "punctuality": 1,
  "friendliness": 1,
  "skillMatch": 1,
  "comment": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | RatingDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.fromUserId | integer | fromUserId 字段（推断） |
  | data.toUserId | integer | toUserId 字段（推断） |
  | data.punctualityScore | integer | punctualityScore 字段（推断） |
  | data.friendlinessScore | integer | friendlinessScore 字段（推断） |
  | data.skillMatchScore | integer | skillMatchScore 字段（推断） |
  | data.ratingComment | String | ratingComment 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "practicePostId": 1,
    "fromUserId": 1,
    "toUserId": 1,
    "punctualityScore": 1,
    "friendlinessScore": 1,
    "skillMatchScore": 1,
    "ratingComment": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BUDDY_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BUDDY_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、RATING_DUPLICATED、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询互评
- **路径**：`GET /api/h5/practices/{postId}/ratings`
- **描述**：查询互评接口（由 BuddyController.ratings 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | postId | path | integer | 是 | postId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/practices/1/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<RatingDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.fromUserId | integer | fromUserId 字段（推断） |
  | data.toUserId | integer | toUserId 字段（推断） |
  | data.punctualityScore | integer | punctualityScore 字段（推断） |
  | data.friendlinessScore | integer | friendlinessScore 字段（推断） |
  | data.skillMatchScore | integer | skillMatchScore 字段（推断） |
  | data.ratingComment | String | ratingComment 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "practicePostId": 1,
      "fromUserId": 1,
      "toUserId": 1,
      "punctualityScore": 1,
      "friendlinessScore": 1,
      "skillMatchScore": 1,
      "ratingComment": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BUDDY_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BUDDY_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、RATING_DUPLICATED、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java:39；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的搭子
- **路径**：`GET /api/h5/buddies`
- **描述**：查询我的搭子接口（由 BuddyController.myBuddies 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | status | query | String | 否 | 状态（推断） | pending |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/buddies?status=pending" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<BuddyDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.relationId | integer | relationId 字段（推断） |
  | data.peerUserId | integer | peerUserId 字段（推断） |
  | data.sourcePracticePostId | integer | sourcePracticePostId 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "relationId": 1,
      "peerUserId": 1,
      "sourcePracticePostId": 1,
      "relationStatus": "pending",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BUDDY_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BUDDY_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java:44；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拉黑
- **路径**：`POST /api/h5/buddies/{userId}/block`
- **描述**：拉黑接口（由 BuddyController.block 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | userId | path | integer | 是 | 用户 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/buddies/1/block" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BuddyDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.relationId | integer | relationId 字段（推断） |
  | data.peerUserId | integer | peerUserId 字段（推断） |
  | data.sourcePracticePostId | integer | sourcePracticePostId 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "relationId": 1,
    "peerUserId": 1,
    "sourcePracticePostId": 1,
    "relationStatus": "pending",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BUDDY_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BUDDY_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、USER_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java:51；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 移除
- **路径**：`DELETE /api/h5/buddies/{userId}`
- **描述**：移除接口（由 BuddyController.remove 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | userId | path | integer | 是 | 用户 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/buddies/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | BuddyDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.relationId | integer | relationId 字段（推断） |
  | data.peerUserId | integer | peerUserId 字段（推断） |
  | data.sourcePracticePostId | integer | sourcePracticePostId 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "relationId": 1,
    "peerUserId": 1,
    "sourcePracticePostId": 1,
    "relationStatus": "pending",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "BUDDY_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：BUDDY_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、USER_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/buddy/controller/BuddyController.java:56；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 门店课程目录

### 详情查询
- **路径**：`GET /api/public/coaches/{id}`
- **描述**：详情查询接口（由 CoachController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/coaches/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CoachDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.displayName | String | displayName 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.teachingStyle | String | teachingStyle 字段（推断） |
  | data.availableTimeSlots | String | availableTimeSlots 字段（推断） |
  | data.certificationStatus | String | certificationStatus 字段（推断） |
  | data.homeStudioId | integer | homeStudioId 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.avgRating | number | avgRating 字段（推断） |
  | data.styles | List<CoachStyleDto> | styles 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "displayName": "TODO: 待补充",
    "intro": "TODO: 待补充",
    "teachingStyle": "TODO: 待补充",
    "availableTimeSlots": "TODO: 待补充",
    "certificationStatus": "pending",
    "homeStudioId": 1,
    "coverAssetId": 1,
    "avgRating": 99,
    "styles": [
      {
        "danceStyleId": 1,
        "proficiencyLevel": "TODO: 待补充"
      }
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/CoachController.java:25；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询课程
- **路径**：`GET /api/public/coaches/{id}/courses`
- **描述**：查询课程接口（由 CoachController.courses 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/coaches/1/courses" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<CourseCard> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.courseName | String | courseName 字段（推断） |
  | data.difficultyLevel | String | difficultyLevel 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.durationMinutes | integer | durationMinutes 字段（推断） |
  | data.zeroBasicFriendly | Boolean | zeroBasicFriendly 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "studioId": 1,
      "coachId": 1,
      "danceStyleId": 1,
      "courseName": "TODO: 待补充",
      "difficultyLevel": "TODO: 待补充",
      "priceAmount": 99,
      "durationMinutes": 1,
      "zeroBasicFriendly": true,
      "coverAssetId": 1
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、COURSE_NOT_FOUND、COURSE_OFFLINE、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/CoachController.java:30；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 详情查询
- **路径**：`GET /api/public/courses/{id}`
- **描述**：详情查询接口（由 CourseController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/courses/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CourseDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.courseName | String | courseName 字段（推断） |
  | data.difficultyLevel | String | difficultyLevel 字段（推断） |
  | data.targetAudience | String | targetAudience 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.durationMinutes | integer | durationMinutes 字段（推断） |
  | data.intensityLevel | String | intensityLevel 字段（推断） |
  | data.courseType | String | courseType 字段（推断） |
  | data.zeroBasicFriendly | Boolean | zeroBasicFriendly 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.status | String | 状态（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "danceStyleId": 1,
    "courseName": "TODO: 待补充",
    "difficultyLevel": "TODO: 待补充",
    "targetAudience": "TODO: 待补充",
    "priceAmount": 99,
    "durationMinutes": 1,
    "intensityLevel": "TODO: 待补充",
    "courseType": "TODO: 待补充",
    "zeroBasicFriendly": true,
    "description": "TODO: 待补充",
    "coverAssetId": 1,
    "status": "pending",
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COURSE_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COURSE_NOT_FOUND、COURSE_OFFLINE、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/CourseController.java:28；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 门店课程目录 schedules
- **路径**：`GET /api/public/courses/{id}/schedules`
- **描述**：门店课程目录 schedules接口（由 CourseController.schedules 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | from | query | LocalDate | 否 | from 字段（推断） | 2026-05-23 |
  | to | query | LocalDate | 否 | to 字段（推断） | 2026-05-23 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/courses/1/schedules?from=2026-05-23&to=2026-05-23" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<ScheduleItem> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.classroomName | String | classroomName 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.capacity | integer | capacity 字段（推断） |
  | data.bookedCount | integer | bookedCount 字段（推断） |
  | data.status | String | 状态（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "studioId": 1,
      "coachId": 1,
      "classroomName": "TODO: 待补充",
      "startAt": "2026-05-23T10:00:00+08:00",
      "endAt": "2026-05-23T10:00:00+08:00",
      "capacity": 1,
      "bookedCount": 1,
      "status": "pending"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COURSE_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COURSE_NOT_FOUND、COURSE_OFFLINE、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/CourseController.java:33；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 附近查询
- **路径**：`GET /api/public/studios/nearby`
- **描述**：附近查询接口（由 StudioController.nearby 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | cityId | query | integer | 否 | 城市 ID（推断） | 1 |
  | latitude | query | number | 否 | 纬度（推断） | 99.00 |
  | longitude | query | number | 否 | 经度（推断） | 99.00 |
  | distanceKm | query | number | 否 | distanceKm 字段（推断） | 99.00 |
  | keyword | query | String | 否 | 搜索关键词（推断） | hiphop |
  | danceStyleId | query | integer | 否 | 舞种 ID（推断） | 1 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/studios/nearby?cityId=1&latitude=99.00&longitude=99.00&distanceKm=99.00&keyword=hiphop&danceStyleId=1&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<StudioCard> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "TODO: 待补充",
        "address": "TODO: 待补充",
        "cityId": 1,
        "businessDistrictId": 1,
        "coverAssetId": 1,
        "distanceKm": 99,
        "latitude": 99,
        "longitude": 99,
        "favored": true
      }
    ],
    "page": 1,
    "pageSize": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/StudioController.java:24；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 详情查询
- **路径**：`GET /api/public/studios/{id}`
- **描述**：详情查询接口（由 StudioController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/studios/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.name | String | name 字段（推断） |
  | data.brandName | String | brandName 字段（推断） |
  | data.address | String | address 字段（推断） |
  | data.transportInfo | String | transportInfo 字段（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.businessDistrictId | integer | businessDistrictId 字段（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.longitude | number | 经度（推断） |
  | data.contactPhone | String | contactPhone 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.danceStyleIds | List<integer> | danceStyleIds 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "name": "TODO: 待补充",
    "brandName": "TODO: 待补充",
    "address": "TODO: 待补充",
    "transportInfo": "TODO: 待补充",
    "cityId": 1,
    "businessDistrictId": 1,
    "latitude": 99,
    "longitude": 99,
    "contactPhone": 13800138000,
    "intro": "TODO: 待补充",
    "coverAssetId": 1,
    "claimStatus": "pending",
    "danceStyleIds": [
      1
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/StudioController.java:41；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 门店课程目录 schedules
- **路径**：`GET /api/public/studios/{studioId}/schedules`
- **描述**：门店课程目录 schedules接口（由 StudioScheduleController.schedules 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | studioId | path | integer | 是 | 舞室 ID（推断） | 1 |
  | from | query | LocalDate | 否 | from 字段（推断） | 2026-05-23 |
  | to | query | LocalDate | 否 | to 字段（推断） | 2026-05-23 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/studios/1/schedules?from=2026-05-23&to=2026-05-23" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<ScheduleItem> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.courseId | integer | 课程 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.classroomName | String | classroomName 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.capacity | integer | capacity 字段（推断） |
  | data.bookedCount | integer | bookedCount 字段（推断） |
  | data.status | String | 状态（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "studioId": 1,
      "coachId": 1,
      "classroomName": "TODO: 待补充",
      "startAt": "2026-05-23T10:00:00+08:00",
      "endAt": "2026-05-23T10:00:00+08:00",
      "capacity": 1,
      "bookedCount": 1,
      "status": "pending"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/catalog/controller/StudioScheduleController.java:26；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 教练运营

### 提交
- **路径**：`POST /api/h5/coach/certifications`
- **描述**：提交接口（由 CoachCertificationController.submit 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | applicationType | body | String | 否 | applicationType 字段（推断）；可选值：independent / studio_affiliated | independent |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 2000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/coach/certifications" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "applicationType": "independent",
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CertificationDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.applicationType | String | applicationType 字段（推断） |
  | data.applicationStatus | String | applicationStatus 字段（推断） |
  | data.remark | String | 备注（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "applicationType": "independent",
    "applicationStatus": "pending",
    "remark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CERT_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CERT_DUPLICATED、CERT_NOT_FOUND、CERT_STATE_CONFLICT、COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/coach/certifications/mine`
- **描述**：查询我的列表接口（由 CoachCertificationController.mine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/coach/certifications/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<CertificationDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.applicationType | String | applicationType 字段（推断） |
  | data.applicationStatus | String | applicationStatus 字段（推断） |
  | data.remark | String | 备注（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "applicationType": "independent",
      "applicationStatus": "pending",
      "remark": "TODO: 待补充",
      "reviewedByUserId": 1,
      "reviewedAt": "2026-05-23T10:00:00+08:00",
      "reviewRemark": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CERT_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CERT_DUPLICATED、CERT_NOT_FOUND、CERT_STATE_CONFLICT、COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java:36；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 按状态查询列表
- **路径**：`GET /api/admin/coach-certifications`
- **描述**：按状态查询列表接口（由 CoachCertificationController.listByStatus 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | status | query | String | 否 | 状态（推断） | pending |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/coach-certifications?status=pending&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<CertificationDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.applicationType | String | applicationType 字段（推断） |
  | data.applicationStatus | String | applicationStatus 字段（推断） |
  | data.remark | String | 备注（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "applicationType": "independent",
        "applicationStatus": "pending",
        "remark": "TODO: 待补充",
        "reviewedByUserId": 1,
        "reviewedAt": "2026-05-23T10:00:00+08:00",
        "reviewRemark": "TODO: 待补充",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CERT_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CERT_DUPLICATED、CERT_NOT_FOUND、CERT_STATE_CONFLICT、COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java:41；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 审核通过
- **路径**：`POST /api/admin/coach-certifications/{id}/approve`
- **描述**：审核通过接口（由 CoachCertificationController.approve 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/coach-certifications/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CertificationDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.applicationType | String | applicationType 字段（推断） |
  | data.applicationStatus | String | applicationStatus 字段（推断） |
  | data.remark | String | 备注（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "applicationType": "independent",
    "applicationStatus": "pending",
    "remark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CERT_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CERT_DUPLICATED、CERT_NOT_FOUND、CERT_STATE_CONFLICT、COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java:50；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/admin/coach-certifications/{id}/reject`
- **描述**：拒绝接口（由 CoachCertificationController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/coach-certifications/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CertificationDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.applicationType | String | applicationType 字段（推断） |
  | data.applicationStatus | String | applicationStatus 字段（推断） |
  | data.remark | String | 备注（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "applicationType": "independent",
    "applicationStatus": "pending",
    "remark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CERT_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CERT_DUPLICATED、CERT_NOT_FOUND、CERT_STATE_CONFLICT、COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachCertificationController.java:58；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询当前用户
- **路径**：`GET /api/h5/coach/me`
- **描述**：查询当前用户接口（由 CoachOpsController.me 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/coach/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CoachMeDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.certified | boolean | certified 字段（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.displayName | String | displayName 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.teachingStyle | String | teachingStyle 字段（推断） |
  | data.certificationStatus | String | certificationStatus 字段（推断） |
  | data.homeStudioId | integer | homeStudioId 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.avgRating | number | avgRating 字段（推断） |
  | data.activeStudioIds | List<integer> | activeStudioIds 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "certified": true,
    "coachId": 1,
    "displayName": "TODO: 待补充",
    "intro": "TODO: 待补充",
    "teachingStyle": "TODO: 待补充",
    "certificationStatus": "pending",
    "homeStudioId": 1,
    "coverAssetId": 1,
    "avgRating": 99,
    "activeStudioIds": [
      1
    ]
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java:28；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 更新资料
- **路径**：`PUT /api/h5/coach/me/profile`
- **描述**：更新资料接口（由 CoachOpsController.updateProfile 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | displayName | body | String | 否 | displayName 字段（推断）；校验：Size(max = 100) | TODO: 待补充 |
  | intro | body | String | 否 | intro 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |
  | teachingStyle | body | String | 否 | teachingStyle 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |
  | coverAssetId | body | integer | 否 | coverAssetId 字段（推断） | 1 |
  | homeStudioId | body | integer | 否 | homeStudioId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X PUT "http://localhost:8080/api/h5/coach/me/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "displayName": "示例内容",
  "intro": "示例内容",
  "teachingStyle": "示例内容",
  "coverAssetId": 1,
  "homeStudioId": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CoachMeDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.certified | boolean | certified 字段（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.displayName | String | displayName 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.teachingStyle | String | teachingStyle 字段（推断） |
  | data.certificationStatus | String | certificationStatus 字段（推断） |
  | data.homeStudioId | integer | homeStudioId 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.avgRating | number | avgRating 字段（推断） |
  | data.activeStudioIds | List<integer> | activeStudioIds 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "certified": true,
    "coachId": 1,
    "displayName": "TODO: 待补充",
    "intro": "TODO: 待补充",
    "teachingStyle": "TODO: 待补充",
    "certificationStatus": "pending",
    "homeStudioId": 1,
    "coverAssetId": 1,
    "avgRating": 99,
    "activeStudioIds": [
      1
    ]
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java:33；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询看板
- **路径**：`GET /api/h5/coach/dashboard`
- **描述**：查询看板接口（由 CoachOpsController.dashboard 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/coach/dashboard" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CoachDashboardDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.monthSessions | integer | monthSessions 字段（推断） |
  | data.monthWorkshopOrders | integer | monthWorkshopOrders 字段（推断） |
  | data.monthIncome | number | monthIncome 字段（推断） |
  | data.pendingReviewReplies | integer | pendingReviewReplies 字段（推断） |
  | data.avgRating | number | avgRating 字段（推断） |
  | data.ratingCount | integer | ratingCount 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "monthSessions": 1,
    "monthWorkshopOrders": 1,
    "monthIncome": 99,
    "pendingReviewReplies": 1,
    "avgRating": 99,
    "ratingCount": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java:38；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询课程
- **路径**：`GET /api/h5/coach/courses`
- **描述**：查询课程接口（由 CoachOpsController.courses 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/coach/courses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<CoachOpsService.CourseSummaryDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.courseName | String | courseName 字段（推断） |
  | data.difficultyLevel | String | difficultyLevel 字段（推断） |
  | data.priceAmount | java.math.number | priceAmount 字段（推断） |
  | data.durationMinutes | integer | durationMinutes 字段（推断） |
  | data.status | String | 状态（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "studioId": 1,
      "danceStyleId": 1,
      "courseName": "TODO: 待补充",
      "difficultyLevel": "TODO: 待补充",
      "priceAmount": 99,
      "durationMinutes": 1,
      "status": "pending"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、COURSE_NOT_FOUND、COURSE_OFFLINE、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/coachops/controller/CoachOpsController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 社区

### 创建
- **路径**：`POST /api/h5/community/posts`
- **描述**：创建接口（由 CommunityController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | postType | body | String | 否 | postType 字段（推断）；可选值：note / video / experience / practice | note |
  | contentText | body | String | 是 | 正文内容（推断）；校验：Size(min = 1, max = 5000) | TODO: 待补充 |
  | danceStyleId | body | integer | 否 | 舞种 ID（推断） | 1 |
  | relatedCourseId | body | integer | 否 | relatedCourseId 字段（推断） | 1 |
  | relatedWorkshopId | body | integer | 否 | relatedWorkshopId 字段（推断） | 1 |
  | cityId | body | integer | 否 | 城市 ID（推断） | 1 |
  | locationName | body | String | 否 | locationName 字段（推断）；校验：Size(max = 200) | TODO: 待补充 |
  | longitude | body | number | 否 | 经度（推断） | 99.00 |
  | latitude | body | number | 否 | 纬度（推断） | 99.00 |
  | visibility | body | String | 否 | 可见性（推断）；可选值：public / followers / private | public |
  | topicNames | body | List<String> | 否 | topicNames 字段（推断）；校验：Size(max = 5) | [] |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "postType": "note",
  "contentText": "示例内容",
  "danceStyleId": 1,
  "relatedCourseId": 1,
  "relatedWorkshopId": 1,
  "cityId": 1,
  "locationName": "示例内容",
  "longitude": 99,
  "latitude": 99,
  "visibility": "public",
  "topicNames": []
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PostDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.authorUserId | integer | authorUserId 字段（推断） |
  | data.postType | String | postType 字段（推断） |
  | data.contentText | String | 正文内容（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.relatedCourseId | integer | relatedCourseId 字段（推断） |
  | data.relatedWorkshopId | integer | relatedWorkshopId 字段（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.visibility | String | 可见性（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.publishedAt | OffsetDateTime | 发布时间，格式 ISO 8601（推断） |
  | data.topics | List<TopicDto> | topics 字段（推断） |
  | data.likeCount | integer | likeCount 字段（推断） |
  | data.commentCount | integer | commentCount 字段（推断） |
  | data.liked | boolean | 是否已点赞（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "authorUserId": 1,
    "postType": "note",
    "contentText": "TODO: 待补充",
    "danceStyleId": 1,
    "relatedCourseId": 1,
    "relatedWorkshopId": 1,
    "cityId": 1,
    "locationName": "TODO: 待补充",
    "longitude": 99,
    "latitude": 99,
    "visibility": "public",
    "postStatus": "pending",
    "publishedAt": "2026-05-23T10:00:00+08:00",
    "topics": [
      {
        "id": 1,
        "topicCode": "TODO: 待补充",
        "topicName": "TODO: 待补充",
        "postCount": 1,
        "hot": true
      }
    ],
    "likeCount": 1,
    "commentCount": 1,
    "liked": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:38；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除
- **路径**：`DELETE /api/h5/community/posts/{id}`
- **描述**：删除接口（由 CommunityController.delete 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/community/posts/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 详情查询
- **路径**：`GET /api/public/community/posts/{id}`
- **描述**：详情查询接口（由 CommunityController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/posts/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PostDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.authorUserId | integer | authorUserId 字段（推断） |
  | data.postType | String | postType 字段（推断） |
  | data.contentText | String | 正文内容（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.relatedCourseId | integer | relatedCourseId 字段（推断） |
  | data.relatedWorkshopId | integer | relatedWorkshopId 字段（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.visibility | String | 可见性（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.publishedAt | OffsetDateTime | 发布时间，格式 ISO 8601（推断） |
  | data.topics | List<TopicDto> | topics 字段（推断） |
  | data.likeCount | integer | likeCount 字段（推断） |
  | data.commentCount | integer | commentCount 字段（推断） |
  | data.liked | boolean | 是否已点赞（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "authorUserId": 1,
    "postType": "note",
    "contentText": "TODO: 待补充",
    "danceStyleId": 1,
    "relatedCourseId": 1,
    "relatedWorkshopId": 1,
    "cityId": 1,
    "locationName": "TODO: 待补充",
    "longitude": 99,
    "latitude": 99,
    "visibility": "public",
    "postStatus": "pending",
    "publishedAt": "2026-05-23T10:00:00+08:00",
    "topics": [
      {
        "id": 1,
        "topicCode": "TODO: 待补充",
        "topicName": "TODO: 待补充",
        "postCount": 1,
        "hot": true
      }
    ],
    "likeCount": 1,
    "commentCount": 1,
    "liked": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:49；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 信息流查询
- **路径**：`GET /api/public/community/feed`
- **描述**：信息流查询接口（由 CommunityController.feed 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | scope | query | String | 是 | scope 字段（推断） | TODO: 待补充 |
  | danceStyleId | query | integer | 否 | 舞种 ID（推断） | 1 |
  | topicId | query | integer | 否 | topicId 字段（推断） | 1 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/feed?scope=demo&danceStyleId=1&topicId=1&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PostListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<PostDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "authorUserId": 1,
        "postType": "note",
        "contentText": "TODO: 待补充",
        "danceStyleId": 1,
        "relatedCourseId": 1,
        "relatedWorkshopId": 1,
        "cityId": 1,
        "locationName": "TODO: 待补充",
        "longitude": 99,
        "latitude": 99,
        "visibility": "public",
        "postStatus": "pending",
        "publishedAt": "2026-05-23T10:00:00+08:00",
        "topics": [
          {
            "id": 1,
            "topicCode": "TODO: 待补充",
            "topicName": "TODO: 待补充",
            "postCount": 1,
            "hot": true
          }
        ],
        "likeCount": 1,
        "commentCount": 1,
        "liked": true
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:54；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 搜索
- **路径**：`GET /api/public/community/search`
- **描述**：搜索接口（由 CommunityController.search 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | q | query | String | 是 | 搜索关键词（推断） | hiphop |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/search?q=hiphop&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PostListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<PostDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "authorUserId": 1,
        "postType": "note",
        "contentText": "TODO: 待补充",
        "danceStyleId": 1,
        "relatedCourseId": 1,
        "relatedWorkshopId": 1,
        "cityId": 1,
        "locationName": "TODO: 待补充",
        "longitude": 99,
        "latitude": 99,
        "visibility": "public",
        "postStatus": "pending",
        "publishedAt": "2026-05-23T10:00:00+08:00",
        "topics": [
          {
            "id": 1,
            "topicCode": "TODO: 待补充",
            "topicName": "TODO: 待补充",
            "postCount": 1,
            "hot": true
          }
        ],
        "likeCount": 1,
        "commentCount": 1,
        "liked": true
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:66；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 点赞/取消点赞
- **路径**：`POST /api/h5/community/posts/{id}/like`
- **描述**：点赞/取消点赞接口（由 CommunityController.like 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/posts/1/like" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:77；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 发表评论
- **路径**：`POST /api/h5/community/posts/{id}/comments`
- **描述**：发表评论接口（由 CommunityController.createComment 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | commentText | body | String | 是 | 评论内容（推断）；校验：Size(min = 1, max = 1000) | TODO: 待补充 |
  | parentCommentId | body | integer | 否 | parentCommentId 字段（推断） | 1 |
  | replyToUserId | body | integer | 否 | replyToUserId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/posts/1/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "commentText": "示例内容",
  "parentCommentId": 1,
  "replyToUserId": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CommentDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.contentPostId | integer | contentPostId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.parentCommentId | integer | parentCommentId 字段（推断） |
  | data.replyToUserId | integer | replyToUserId 字段（推断） |
  | data.commentText | String | 评论内容（推断） |
  | data.commentStatus | String | commentStatus 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "contentPostId": 1,
    "userId": 1,
    "parentCommentId": 1,
    "replyToUserId": 1,
    "commentText": "TODO: 待补充",
    "commentStatus": "pending",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COMMENT_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COMMENT_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:84；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询评论列表
- **路径**：`GET /api/public/community/posts/{id}/comments`
- **描述**：查询评论列表接口（由 CommunityController.listComments 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/posts/1/comments" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<CommentDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.contentPostId | integer | contentPostId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.parentCommentId | integer | parentCommentId 字段（推断） |
  | data.replyToUserId | integer | replyToUserId 字段（推断） |
  | data.commentText | String | 评论内容（推断） |
  | data.commentStatus | String | commentStatus 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "contentPostId": 1,
      "userId": 1,
      "parentCommentId": 1,
      "replyToUserId": 1,
      "commentText": "TODO: 待补充",
      "commentStatus": "pending",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COMMENT_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COMMENT_NOT_FOUND、INVALID_ARGUMENT、POST_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:92；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除评论
- **路径**：`DELETE /api/h5/community/comments/{commentId}`
- **描述**：删除评论接口（由 CommunityController.deleteComment 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | commentId | path | integer | 是 | commentId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/community/comments/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COMMENT_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COMMENT_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:97；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询话题列表
- **路径**：`GET /api/public/community/topics`
- **描述**：查询话题列表接口（由 CommunityController.topics 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/topics" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<TopicDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.topicCode | String | topicCode 字段（推断） |
  | data.topicName | String | topicName 字段（推断） |
  | data.postCount | integer | postCount 字段（推断） |
  | data.hot | boolean | hot 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "topicCode": "TODO: 待补充",
      "topicName": "TODO: 待补充",
      "postCount": 1,
      "hot": true
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、TOPIC_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:105；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 按话题查询动态
- **路径**：`GET /api/public/community/topics/{name}/posts`
- **描述**：按话题查询动态接口（由 CommunityController.postsByTopic 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | name | path | String | 是 | name 字段（推断） | TODO: 待补充 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/community/topics/1/posts?page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PostListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<PostDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "authorUserId": 1,
        "postType": "note",
        "contentText": "TODO: 待补充",
        "danceStyleId": 1,
        "relatedCourseId": 1,
        "relatedWorkshopId": 1,
        "cityId": 1,
        "locationName": "TODO: 待补充",
        "longitude": 99,
        "latitude": 99,
        "visibility": "public",
        "postStatus": "pending",
        "publishedAt": "2026-05-23T10:00:00+08:00",
        "topics": [
          {
            "id": 1,
            "topicCode": "TODO: 待补充",
            "topicName": "TODO: 待补充",
            "postCount": 1,
            "hot": true
          }
        ],
        "likeCount": 1,
        "commentCount": 1,
        "liked": true
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、POST_NOT_FOUND、TOPIC_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:110；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 关注/取消关注
- **路径**：`POST /api/h5/community/follow/{userId}`
- **描述**：关注/取消关注接口（由 CommunityController.toggleFollow 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | userId | path | integer | 是 | 用户 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/follow/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、USER_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:121；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的关注
- **路径**：`GET /api/h5/community/follow/me`
- **描述**：查询我的关注接口（由 CommunityController.myFollowees 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/community/follow/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<Long> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    1
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:126；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 举报动态
- **路径**：`POST /api/h5/community/posts/{id}/report`
- **描述**：举报动态接口（由 CommunityController.reportPost 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | reasonCode | body | String | 是 | reasonCode 字段（推断）；可选值：spam / adult / violence / fraud / other | spam |
  | reasonDetail | body | String | 否 | reasonDetail 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/posts/1/report" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reasonCode": "spam",
  "reasonDetail": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、POST_NOT_FOUND、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:133；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 举报评论
- **路径**：`POST /api/h5/community/comments/{commentId}/report`
- **描述**：举报评论接口（由 CommunityController.reportComment 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | commentId | path | integer | 是 | commentId 字段（推断） | 1 |
  | reasonCode | body | String | 是 | reasonCode 字段（推断）；可选值：spam / adult / violence / fraud / other | spam |
  | reasonDetail | body | String | 否 | reasonDetail 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/community/comments/1/report" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reasonCode": "spam",
  "reasonDetail": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COMMENT_NOT_FOUND",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COMMENT_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、REPORT_DUPLICATED、REPORT_NOT_FOUND、REPORT_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/community/controller/CommunityController.java:141；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 收藏

### 切换状态
- **路径**：`POST /api/h5/favorites`
- **描述**：切换状态接口（由 FavoriteController.toggle 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | body | String | 是 | 目标类型（推断） | studio |
  | targetId | body | integer | 是 | 目标 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/favorites" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "targetType": "studio",
  "targetId": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/favorite/controller/FavoriteController.java:29；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 列表查询
- **路径**：`GET /api/h5/favorites`
- **描述**：列表查询接口（由 FavoriteController.list 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | query | String | 否 | 目标类型（推断） | studio |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/favorites?targetType=studio" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<FavoriteDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "targetType": "studio",
      "targetId": 1,
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/favorite/controller/FavoriteController.java:35；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 检查状态
- **路径**：`GET /api/h5/favorites/check`
- **描述**：检查状态接口（由 FavoriteController.check 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | query | String | 是 | 目标类型（推断） | studio |
  | targetId | query | integer | 是 | 目标 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/favorites/check?targetType=studio&targetId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/favorite/controller/FavoriteController.java:40；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 成长记录

### 创建打卡
- **路径**：`POST /api/h5/growth/checkins`
- **描述**：创建打卡接口（由 GrowthController.createCheckin 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | danceStyleId | body | integer | 否 | 舞种 ID（推断） | 1 |
  | studioId | body | integer | 否 | 舞室 ID（推断） | 1 |
  | courseScheduleId | body | integer | 否 | 课程排期 ID（推断） | 1 |
  | practicePostId | body | integer | 否 | practicePostId 字段（推断） | 1 |
  | durationMinutes | body | integer | 是 | durationMinutes 字段（推断）；校验：Min(1), Max(1440) | 1 |
  | feelingText | body | String | 否 | feelingText 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |
  | isPublic | body | Boolean | 否 | isPublic 字段（推断） | true |
  | checkinAt | body | OffsetDateTime | 否 | checkinAt 字段（推断） | 2026-05-23T10:00:00+08:00 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/growth/checkins" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "danceStyleId": 1,
  "studioId": 1,
  "courseScheduleId": 1,
  "practicePostId": 1,
  "durationMinutes": 1,
  "feelingText": "示例内容",
  "isPublic": true,
  "checkinAt": "2026-05-23T10:00:00+08:00"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | CheckinDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.durationMinutes | integer | durationMinutes 字段（推断） |
  | data.feelingText | String | feelingText 字段（推断） |
  | data.isPublic | Boolean | isPublic 字段（推断） |
  | data.checkinAt | OffsetDateTime | checkinAt 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "danceStyleId": 1,
    "studioId": 1,
    "courseScheduleId": 1,
    "practicePostId": 1,
    "durationMinutes": 1,
    "feelingText": "TODO: 待补充",
    "isPublic": true,
    "checkinAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CHECKIN_CODE_INVALID",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CHECKIN_CODE_INVALID、CHECKIN_NOT_FOUND、CHECKIN_TICKET_NOT_FOUND、CHECKIN_TOO_EARLY、CHECKIN_TOO_LATE、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:38；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询打卡列表
- **路径**：`GET /api/h5/growth/checkins`
- **描述**：查询打卡列表接口（由 GrowthController.listCheckins 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/checkins" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<CheckinDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.courseScheduleId | integer | 课程排期 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.durationMinutes | integer | durationMinutes 字段（推断） |
  | data.feelingText | String | feelingText 字段（推断） |
  | data.isPublic | Boolean | isPublic 字段（推断） |
  | data.checkinAt | OffsetDateTime | checkinAt 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "danceStyleId": 1,
      "studioId": 1,
      "courseScheduleId": 1,
      "practicePostId": 1,
      "durationMinutes": 1,
      "feelingText": "TODO: 待补充",
      "isPublic": true,
      "checkinAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CHECKIN_CODE_INVALID",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CHECKIN_CODE_INVALID、CHECKIN_NOT_FOUND、CHECKIN_TICKET_NOT_FOUND、CHECKIN_TOO_EARLY、CHECKIN_TOO_LATE、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除打卡
- **路径**：`DELETE /api/h5/growth/checkins/{id}`
- **描述**：删除打卡接口（由 GrowthController.deleteCheckin 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/growth/checkins/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CHECKIN_CODE_INVALID",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CHECKIN_CODE_INVALID、CHECKIN_NOT_FOUND、CHECKIN_TICKET_NOT_FOUND、CHECKIN_TOO_EARLY、CHECKIN_TOO_LATE、FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:48；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询统计
- **路径**：`GET /api/h5/growth/stats`
- **描述**：查询统计接口（由 GrowthController.stats 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/stats" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | GrowthStats | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.totalSessions | integer | totalSessions 字段（推断） |
  | data.totalMinutes | integer | totalMinutes 字段（推断） |
  | data.totalDays | integer | totalDays 字段（推断） |
  | data.styleCount | integer | styleCount 字段（推断） |
  | data.streakDays | integer | streakDays 字段（推断） |
  | data.lastCheckinAt | OffsetDateTime | lastCheckinAt 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "totalSessions": 1,
    "totalMinutes": 1,
    "totalDays": 1,
    "styleCount": 1,
    "streakDays": 1,
    "lastCheckinAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:54；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询时间线
- **路径**：`GET /api/h5/growth/timeline`
- **描述**：查询时间线接口（由 GrowthController.timeline 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/timeline" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<TimelineItem> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.type | String | type 字段（推断） |
  | data.refId | integer | refId 字段（推断） |
  | data.title | String | title 字段（推断） |
  | data.subtitle | String | subtitle 字段（推断） |
  | data.ts | OffsetDateTime | ts 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "type": "TODO: 待补充",
      "refId": 1,
      "title": "TODO: 待补充",
      "subtitle": "TODO: 待补充",
      "ts": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:59；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 更新当前目标
- **路径**：`PUT /api/h5/growth/goals/active`
- **描述**：更新当前目标接口（由 GrowthController.upsertGoal 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | goalPeriod | body | String | 否 | goalPeriod 字段（推断）；可选值：weekly / monthly | weekly |
  | targetMinutes | body | integer | 否 | targetMinutes 字段（推断）；校验：Min(0), Max(10000) | 1 |
  | targetTimes | body | integer | 否 | targetTimes 字段（推断）；校验：Min(0), Max(500) | 1 |
  | startDate | body | LocalDate | 是 | startDate 字段（推断） | 2026-05-23 |
  | endDate | body | LocalDate | 是 | endDate 字段（推断） | 2026-05-23 |

- **请求示例**

```bash
curl -X PUT "http://localhost:8080/api/h5/growth/goals/active" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "goalPeriod": "weekly",
  "targetMinutes": 1,
  "targetTimes": 1,
  "startDate": "2026-05-23",
  "endDate": "2026-05-23"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | GoalDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.goalPeriod | String | goalPeriod 字段（推断） |
  | data.targetMinutes | integer | targetMinutes 字段（推断） |
  | data.targetTimes | integer | targetTimes 字段（推断） |
  | data.currentMinutes | integer | currentMinutes 字段（推断） |
  | data.currentTimes | integer | currentTimes 字段（推断） |
  | data.startDate | LocalDate | startDate 字段（推断） |
  | data.endDate | LocalDate | endDate 字段（推断） |
  | data.goalStatus | String | goalStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "goalPeriod": "weekly",
    "targetMinutes": 1,
    "targetTimes": 1,
    "currentMinutes": 1,
    "currentTimes": 1,
    "startDate": "2026-05-23",
    "endDate": "2026-05-23",
    "goalStatus": "pending"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:64；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询当前目标
- **路径**：`GET /api/h5/growth/goals/active`
- **描述**：查询当前目标接口（由 GrowthController.activeGoal 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/goals/active" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | GoalDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.goalPeriod | String | goalPeriod 字段（推断） |
  | data.targetMinutes | integer | targetMinutes 字段（推断） |
  | data.targetTimes | integer | targetTimes 字段（推断） |
  | data.currentMinutes | integer | currentMinutes 字段（推断） |
  | data.currentTimes | integer | currentTimes 字段（推断） |
  | data.startDate | LocalDate | startDate 字段（推断） |
  | data.endDate | LocalDate | endDate 字段（推断） |
  | data.goalStatus | String | goalStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "goalPeriod": "weekly",
    "targetMinutes": 1,
    "targetTimes": 1,
    "currentMinutes": 1,
    "currentTimes": 1,
    "startDate": "2026-05-23",
    "endDate": "2026-05-23",
    "goalStatus": "pending"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:69；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建作品
- **路径**：`POST /api/h5/growth/works`
- **描述**：创建作品接口（由 GrowthController.createWork 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | danceStyleId | body | integer | 否 | 舞种 ID（推断） | 1 |
  | workTitle | body | String | 是 | workTitle 字段（推断）；校验：Size(max = 200) | TODO: 待补充 |
  | workDescription | body | String | 否 | workDescription 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |
  | coverAssetId | body | integer | 否 | coverAssetId 字段（推断） | 1 |
  | isPublic | body | Boolean | 否 | isPublic 字段（推断） | true |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/growth/works" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "danceStyleId": 1,
  "workTitle": "示例内容",
  "workDescription": "示例内容",
  "coverAssetId": 1,
  "isPublic": true
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workTitle | String | workTitle 字段（推断） |
  | data.workDescription | String | workDescription 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.isPublic | Boolean | isPublic 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "danceStyleId": 1,
    "workTitle": "TODO: 待补充",
    "workDescription": "TODO: 待补充",
    "coverAssetId": 1,
    "isPublic": true,
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:74；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询作品列表
- **路径**：`GET /api/h5/growth/works`
- **描述**：查询作品列表接口（由 GrowthController.listWorks 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/works" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<WorkDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workTitle | String | workTitle 字段（推断） |
  | data.workDescription | String | workDescription 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.isPublic | Boolean | isPublic 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "danceStyleId": 1,
      "workTitle": "TODO: 待补充",
      "workDescription": "TODO: 待补充",
      "coverAssetId": 1,
      "isPublic": true,
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:79；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除作品
- **路径**：`DELETE /api/h5/growth/works/{id}`
- **描述**：删除作品接口（由 GrowthController.deleteWork 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/growth/works/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:84；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询徽章
- **路径**：`GET /api/h5/growth/badges`
- **描述**：查询徽章接口（由 GrowthController.badges 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/growth/badges" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<BadgeDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.badgeId | integer | badgeId 字段（推断） |
  | data.sourceType | String | sourceType 字段（推断） |
  | data.sourceRefId | integer | sourceRefId 字段（推断） |
  | data.awardedAt | OffsetDateTime | awardedAt 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "badgeId": 1,
      "sourceType": "TODO: 待补充",
      "sourceRefId": 1,
      "awardedAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/growth/controller/GrowthController.java:90；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 认证与当前用户

### 发送短信验证码
- **路径**：`POST /api/auth/sms/send`
- **描述**：发送短信验证码接口（由 AuthController.sendSms 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | phone | body | String | 否 | 手机号（推断） | 13800138000 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/auth/sms/send" \
  -H "Content-Type: application/json" \
  -d '{
  "phone": 13800138000
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、SMS_COOLDOWN、SMS_EXPIRED、SMS_INVALID、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/iam/controller/AuthController.java:26；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 短信登录
- **路径**：`POST /api/auth/login`
- **描述**：短信登录接口（由 AuthController.login 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | phone | body | String | 否 | 手机号（推断） | 13800138000 |
  | code | body | String | 是 | 验证码或业务编码（推断）；校验：Size(min = 4, max = 6) | 123456 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
  "phone": 13800138000,
  "code": 123456
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | LoginResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.token | String | JWT 访问令牌（推断） |
  | data.user | UserSummary | user 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "token": "eyJhbGciOi...",
    "user": {
      "id": 1,
      "phone": 13800138000,
      "nickname": "TODO: 待补充",
      "avatar": "TODO: 待补充",
      "roles": [
        "TODO: 待补充"
      ]
    }
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/iam/controller/AuthController.java:32；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询当前用户
- **路径**：`GET /api/h5/me`
- **描述**：查询当前用户接口（由 MeController.me 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | UserSummary | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.phone | String | 手机号（推断） |
  | data.nickname | String | 昵称（推断） |
  | data.avatar | String | avatar 字段（推断） |
  | data.roles | List<String> | 角色列表（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "phone": 13800138000,
    "nickname": "TODO: 待补充",
    "avatar": "TODO: 待补充",
    "roles": [
      "TODO: 待补充"
    ]
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/iam/controller/MeController.java:27；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 商家管理

### 商家管理 invite
- **路径**：`POST /api/merchant/coach-relations`
- **描述**：商家管理 invite接口（由 CoachRelationController.invite 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | studioId | body | integer | 是 | 舞室 ID（推断） | 1 |
  | coachId | body | integer | 是 | 教练 ID（推断） | 1 |
  | relationType | body | String | 是 | relationType 字段（推断）；可选值：full_time / signed / independent | signed |
  | settlementMode | body | String | 否 | settlementMode 字段（推断）；可选值：ratio / fixed | ratio |
  | settlementRatio | body | number | 否 | settlementRatio 字段（推断） | 99.00 |
  | effectiveFrom | body | LocalDate | 否 | effectiveFrom 字段（推断） | 2026-05-23 |
  | effectiveTo | body | LocalDate | 否 | effectiveTo 字段（推断） | 2026-05-23 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/coach-relations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "studioId": 1,
  "coachId": 1,
  "relationType": "signed",
  "settlementMode": "ratio",
  "settlementRatio": 99,
  "effectiveFrom": "2026-05-23",
  "effectiveTo": "2026-05-23"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioCoachRelationDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.relationType | String | relationType 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.settlementMode | String | settlementMode 字段（推断） |
  | data.settlementRatio | number | settlementRatio 字段（推断） |
  | data.invitedByUserId | integer | invitedByUserId 字段（推断） |
  | data.approvedByUserId | integer | approvedByUserId 字段（推断） |
  | data.effectiveFrom | LocalDate | effectiveFrom 字段（推断） |
  | data.effectiveTo | LocalDate | effectiveTo 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "relationType": "signed",
    "relationStatus": "pending",
    "settlementMode": "ratio",
    "settlementRatio": 99,
    "invitedByUserId": 1,
    "approvedByUserId": 1,
    "effectiveFrom": "2026-05-23",
    "effectiveTo": "2026-05-23",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、RELATION_DUPLICATED、RELATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/CoachRelationController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 更新
- **路径**：`PUT /api/merchant/coach-relations/{id}`
- **描述**：更新接口（由 CoachRelationController.update 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | relationStatus | body | String | 否 | relationStatus 字段（推断）；可选值：pending / active / inactive / terminated | pending |
  | relationType | body | String | 否 | relationType 字段（推断）；可选值：full_time / signed / independent | signed |
  | settlementRatio | body | number | 否 | settlementRatio 字段（推断） | 99.00 |
  | effectiveTo | body | LocalDate | 否 | effectiveTo 字段（推断） | 2026-05-23 |

- **请求示例**

```bash
curl -X PUT "http://localhost:8080/api/merchant/coach-relations/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "relationStatus": "pending",
  "relationType": "signed",
  "settlementRatio": 99,
  "effectiveTo": "2026-05-23"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioCoachRelationDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.relationType | String | relationType 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.settlementMode | String | settlementMode 字段（推断） |
  | data.settlementRatio | number | settlementRatio 字段（推断） |
  | data.invitedByUserId | integer | invitedByUserId 字段（推断） |
  | data.approvedByUserId | integer | approvedByUserId 字段（推断） |
  | data.effectiveFrom | LocalDate | effectiveFrom 字段（推断） |
  | data.effectiveTo | LocalDate | effectiveTo 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "relationType": "signed",
    "relationStatus": "pending",
    "settlementMode": "ratio",
    "settlementRatio": 99,
    "invitedByUserId": 1,
    "approvedByUserId": 1,
    "effectiveFrom": "2026-05-23",
    "effectiveTo": "2026-05-23",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、RELATION_DUPLICATED、RELATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/CoachRelationController.java:36；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 商家管理 listByStudio
- **路径**：`GET /api/merchant/coach-relations`
- **描述**：商家管理 listByStudio接口（由 CoachRelationController.listByStudio 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | studioId | query | integer | 是 | 舞室 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/merchant/coach-relations?studioId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<StudioCoachRelationDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.relationType | String | relationType 字段（推断） |
  | data.relationStatus | String | relationStatus 字段（推断） |
  | data.settlementMode | String | settlementMode 字段（推断） |
  | data.settlementRatio | number | settlementRatio 字段（推断） |
  | data.invitedByUserId | integer | invitedByUserId 字段（推断） |
  | data.approvedByUserId | integer | approvedByUserId 字段（推断） |
  | data.effectiveFrom | LocalDate | effectiveFrom 字段（推断） |
  | data.effectiveTo | LocalDate | effectiveTo 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "studioId": 1,
      "coachId": 1,
      "relationType": "signed",
      "relationStatus": "pending",
      "settlementMode": "ratio",
      "settlementRatio": 99,
      "invitedByUserId": 1,
      "approvedByUserId": 1,
      "effectiveFrom": "2026-05-23",
      "effectiveTo": "2026-05-23",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "COACH_NOT_APPROVED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：COACH_NOT_APPROVED、COACH_NOT_FOUND、FORBIDDEN、INVALID_ARGUMENT、RELATION_DUPLICATED、RELATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/CoachRelationController.java:44；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 提交
- **路径**：`POST /api/h5/studio-claims`
- **描述**：提交接口（由 StudioClaimController.submit 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | studioId | body | integer | 是 | 舞室 ID（推断） | 1 |
  | claimType | body | String | 否 | claimType 字段（推断）；可选值：owner_claim / operator_claim | TODO: 待补充 |
  | businessLicenseAssetId | body | integer | 否 | businessLicenseAssetId 字段（推断） | 1 |
  | submittedRemark | body | String | 否 | submittedRemark 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/studio-claims" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "studioId": 1,
  "claimType": "示例内容",
  "businessLicenseAssetId": 1,
  "submittedRemark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioClaimDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.claimType | String | claimType 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.businessLicenseAssetId | integer | businessLicenseAssetId 字段（推断） |
  | data.submittedRemark | String | submittedRemark 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "applicantUserId": 1,
    "claimType": "TODO: 待补充",
    "claimStatus": "pending",
    "businessLicenseAssetId": 1,
    "submittedRemark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CLAIM_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CLAIM_DUPLICATED、CLAIM_NOT_FOUND、CLAIM_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/studio-claims/mine`
- **描述**：查询我的列表接口（由 StudioClaimController.mine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/studio-claims/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<StudioClaimDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.claimType | String | claimType 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.businessLicenseAssetId | integer | businessLicenseAssetId 字段（推断） |
  | data.submittedRemark | String | submittedRemark 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "studioId": 1,
      "applicantUserId": 1,
      "claimType": "TODO: 待补充",
      "claimStatus": "pending",
      "businessLicenseAssetId": 1,
      "submittedRemark": "TODO: 待补充",
      "reviewedByUserId": 1,
      "reviewedAt": "2026-05-23T10:00:00+08:00",
      "reviewRemark": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CLAIM_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CLAIM_DUPLICATED、CLAIM_NOT_FOUND、CLAIM_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java:36；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 按状态查询列表
- **路径**：`GET /api/admin/studio-claims`
- **描述**：按状态查询列表接口（由 StudioClaimController.listByStatus 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | status | query | String | 否 | 状态（推断） | pending |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/studio-claims?status=pending&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<StudioClaimDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.claimType | String | claimType 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.businessLicenseAssetId | integer | businessLicenseAssetId 字段（推断） |
  | data.submittedRemark | String | submittedRemark 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "studioId": 1,
        "applicantUserId": 1,
        "claimType": "TODO: 待补充",
        "claimStatus": "pending",
        "businessLicenseAssetId": 1,
        "submittedRemark": "TODO: 待补充",
        "reviewedByUserId": 1,
        "reviewedAt": "2026-05-23T10:00:00+08:00",
        "reviewRemark": "TODO: 待补充",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CLAIM_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CLAIM_DUPLICATED、CLAIM_NOT_FOUND、CLAIM_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java:41；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 审核通过
- **路径**：`POST /api/admin/studio-claims/{id}/approve`
- **描述**：审核通过接口（由 StudioClaimController.approve 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/studio-claims/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioClaimDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.claimType | String | claimType 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.businessLicenseAssetId | integer | businessLicenseAssetId 字段（推断） |
  | data.submittedRemark | String | submittedRemark 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "applicantUserId": 1,
    "claimType": "TODO: 待补充",
    "claimStatus": "pending",
    "businessLicenseAssetId": 1,
    "submittedRemark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CLAIM_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CLAIM_DUPLICATED、CLAIM_NOT_FOUND、CLAIM_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java:50；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/admin/studio-claims/{id}/reject`
- **描述**：拒绝接口（由 StudioClaimController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/studio-claims/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | StudioClaimDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.claimType | String | claimType 字段（推断） |
  | data.claimStatus | String | claimStatus 字段（推断） |
  | data.businessLicenseAssetId | integer | businessLicenseAssetId 字段（推断） |
  | data.submittedRemark | String | submittedRemark 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "applicantUserId": 1,
    "claimType": "TODO: 待补充",
    "claimStatus": "pending",
    "businessLicenseAssetId": 1,
    "submittedRemark": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CLAIM_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CLAIM_DUPLICATED、CLAIM_NOT_FOUND、CLAIM_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、STUDIO_INACTIVE、STUDIO_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/merchant/controller/StudioClaimController.java:58；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 消息通知

### 列表查询
- **路径**：`GET /api/h5/messages`
- **描述**：列表查询接口（由 NotificationController.list 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | category | query | String | 否 | 分类（推断） | TODO: 待补充 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/messages?category=demo&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | NotificationListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<NotificationDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |
  | data.unread | integer | unread 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "noticeType": "TODO: 待补充",
        "category": "TODO: 待补充",
        "title": "TODO: 待补充",
        "content": "TODO: 待补充",
        "targetType": "studio",
        "targetId": 1,
        "isRead": true,
        "readAt": "2026-05-23T10:00:00+08:00",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1,
    "unread": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、NOTIFICATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/message/controller/NotificationController.java:26；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 消息通知 markRead
- **路径**：`POST /api/h5/messages/{id}/read`
- **描述**：消息通知 markRead接口（由 NotificationController.markRead 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/messages/1/read" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、NOTIFICATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/message/controller/NotificationController.java:35；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 消息通知 markAllRead
- **路径**：`POST /api/h5/messages/read-all`
- **描述**：消息通知 markAllRead接口（由 NotificationController.markAllRead 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/messages/read-all" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、NOTIFICATION_NOT_FOUND、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/message/controller/NotificationController.java:41；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 约练

### 创建
- **路径**：`POST /api/h5/practices`
- **描述**：创建接口（由 PracticeController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | danceStyleId | body | integer | 是 | 舞种 ID（推断） | 1 |
  | cityId | body | integer | 是 | 城市 ID（推断） | 1 |
  | studioId | body | integer | 否 | 舞室 ID（推断） | 1 |
  | locationName | body | String | 是 | locationName 字段（推断）；校验：Size(max = 200) | TODO: 待补充 |
  | locationAddress | body | String | 否 | locationAddress 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |
  | longitude | body | number | 否 | 经度（推断） | 99.00 |
  | latitude | body | number | 否 | 纬度（推断） | 99.00 |
  | skillLevel | body | String | 否 | skillLevel 字段（推断） | TODO: 待补充 |
  | expectedPeopleMin | body | integer | 否 | expectedPeopleMin 字段（推断）；校验：Min(1), Max(50) | 1 |
  | expectedPeopleMax | body | integer | 否 | expectedPeopleMax 字段（推断）；校验：Min(1), Max(50) | 1 |
  | startAt | body | OffsetDateTime | 是 | 开始时间，格式 ISO 8601（推断） | 2026-05-23T10:00:00+08:00 |
  | endAt | body | OffsetDateTime | 是 | 结束时间，格式 ISO 8601（推断） | 2026-05-23T10:00:00+08:00 |
  | description | body | String | 否 | description 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practices" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "danceStyleId": 1,
  "cityId": 1,
  "studioId": 1,
  "locationName": "示例内容",
  "locationAddress": "示例内容",
  "longitude": 99,
  "latitude": 99,
  "skillLevel": "示例内容",
  "expectedPeopleMin": 1,
  "expectedPeopleMax": 1,
  "startAt": "2026-05-23T10:00:00+08:00",
  "endAt": "2026-05-23T10:00:00+08:00",
  "description": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PracticePostDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.creatorUserId | integer | creatorUserId 字段（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.locationAddress | String | locationAddress 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.skillLevel | String | skillLevel 字段（推断） |
  | data.expectedPeopleMin | integer | expectedPeopleMin 字段（推断） |
  | data.expectedPeopleMax | integer | expectedPeopleMax 字段（推断） |
  | data.currentPeopleCount | integer | currentPeopleCount 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.expiresAt | OffsetDateTime | expiresAt 字段（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "creatorUserId": 1,
    "danceStyleId": 1,
    "studioId": 1,
    "cityId": 1,
    "locationName": "TODO: 待补充",
    "locationAddress": "TODO: 待补充",
    "longitude": 99,
    "latitude": 99,
    "skillLevel": "TODO: 待补充",
    "expectedPeopleMin": 1,
    "expectedPeopleMax": 1,
    "currentPeopleCount": 1,
    "startAt": "2026-05-23T10:00:00+08:00",
    "endAt": "2026-05-23T10:00:00+08:00",
    "expiresAt": "2026-05-23T10:00:00+08:00",
    "postStatus": "pending",
    "description": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:32；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 约练 square
- **路径**：`GET /api/public/practices`
- **描述**：约练 square接口（由 PracticeController.square 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | cityId | query | integer | 否 | 城市 ID（推断） | 1 |
  | danceStyleId | query | integer | 否 | 舞种 ID（推断） | 1 |
  | skillLevel | query | String | 否 | skillLevel 字段（推断） | TODO: 待补充 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/practices?cityId=1&danceStyleId=1&skillLevel=demo&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PracticeListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<PracticePostDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "creatorUserId": 1,
        "danceStyleId": 1,
        "studioId": 1,
        "cityId": 1,
        "locationName": "TODO: 待补充",
        "locationAddress": "TODO: 待补充",
        "longitude": 99,
        "latitude": 99,
        "skillLevel": "TODO: 待补充",
        "expectedPeopleMin": 1,
        "expectedPeopleMax": 1,
        "currentPeopleCount": 1,
        "startAt": "2026-05-23T10:00:00+08:00",
        "endAt": "2026-05-23T10:00:00+08:00",
        "expiresAt": "2026-05-23T10:00:00+08:00",
        "postStatus": "pending",
        "description": "TODO: 待补充",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:37；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 详情查询
- **路径**：`GET /api/public/practices/{id}`
- **描述**：详情查询接口（由 PracticeController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/practices/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PracticePostDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.creatorUserId | integer | creatorUserId 字段（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.locationAddress | String | locationAddress 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.skillLevel | String | skillLevel 字段（推断） |
  | data.expectedPeopleMin | integer | expectedPeopleMin 字段（推断） |
  | data.expectedPeopleMax | integer | expectedPeopleMax 字段（推断） |
  | data.currentPeopleCount | integer | currentPeopleCount 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.expiresAt | OffsetDateTime | expiresAt 字段（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "creatorUserId": 1,
    "danceStyleId": 1,
    "studioId": 1,
    "cityId": 1,
    "locationName": "TODO: 待补充",
    "locationAddress": "TODO: 待补充",
    "longitude": 99,
    "latitude": 99,
    "skillLevel": "TODO: 待补充",
    "expectedPeopleMin": 1,
    "expectedPeopleMax": 1,
    "currentPeopleCount": 1,
    "startAt": "2026-05-23T10:00:00+08:00",
    "endAt": "2026-05-23T10:00:00+08:00",
    "expiresAt": "2026-05-23T10:00:00+08:00",
    "postStatus": "pending",
    "description": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:48；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 取消
- **路径**：`POST /api/h5/practices/{id}/cancel`
- **描述**：取消接口（由 PracticeController.cancel 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practices/1/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | PracticePostDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.creatorUserId | integer | creatorUserId 字段（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.locationAddress | String | locationAddress 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.skillLevel | String | skillLevel 字段（推断） |
  | data.expectedPeopleMin | integer | expectedPeopleMin 字段（推断） |
  | data.expectedPeopleMax | integer | expectedPeopleMax 字段（推断） |
  | data.currentPeopleCount | integer | currentPeopleCount 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.expiresAt | OffsetDateTime | expiresAt 字段（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "creatorUserId": 1,
    "danceStyleId": 1,
    "studioId": 1,
    "cityId": 1,
    "locationName": "TODO: 待补充",
    "locationAddress": "TODO: 待补充",
    "longitude": 99,
    "latitude": 99,
    "skillLevel": "TODO: 待补充",
    "expectedPeopleMin": 1,
    "expectedPeopleMax": 1,
    "currentPeopleCount": 1,
    "startAt": "2026-05-23T10:00:00+08:00",
    "endAt": "2026-05-23T10:00:00+08:00",
    "expiresAt": "2026-05-23T10:00:00+08:00",
    "postStatus": "pending",
    "description": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:53；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 申请加入
- **路径**：`POST /api/h5/practices/{id}/join`
- **描述**：申请加入接口（由 PracticeController.apply 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | message | body | String | 否 | message 字段（推断）；校验：Size(max = 500) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practices/1/join" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "message": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | JoinRequestDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "practicePostId": 1,
    "applicantUserId": 1,
    "joinStatus": "pending",
    "joinMessage": "TODO: 待补充",
    "actedByUserId": 1,
    "actedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、JOIN_DUPLICATED、JOIN_REQUEST_NOT_FOUND、JOIN_STATE_CONFLICT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:58；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 约练 requestsOfPost
- **路径**：`GET /api/h5/practices/{id}/requests`
- **描述**：约练 requestsOfPost接口（由 PracticeController.requestsOfPost 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/practices/1/requests" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<JoinRequestDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "practicePostId": 1,
      "applicantUserId": 1,
      "joinStatus": "pending",
      "joinMessage": "TODO: 待补充",
      "actedByUserId": 1,
      "actedAt": "2026-05-23T10:00:00+08:00",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:66；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 接受申请
- **路径**：`POST /api/h5/practice-requests/{requestId}/accept`
- **描述**：接受申请接口（由 PracticeController.accept 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | requestId | path | integer | 是 | requestId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practice-requests/1/accept" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | JoinRequestDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "practicePostId": 1,
    "applicantUserId": 1,
    "joinStatus": "pending",
    "joinMessage": "TODO: 待补充",
    "actedByUserId": 1,
    "actedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:71；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/h5/practice-requests/{requestId}/reject`
- **描述**：拒绝接口（由 PracticeController.reject 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | requestId | path | integer | 是 | requestId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practice-requests/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | JoinRequestDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "practicePostId": 1,
    "applicantUserId": 1,
    "joinStatus": "pending",
    "joinMessage": "TODO: 待补充",
    "actedByUserId": 1,
    "actedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:76；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 约练 cancelByApplicant
- **路径**：`POST /api/h5/practice-requests/{requestId}/cancel`
- **描述**：约练 cancelByApplicant接口（由 PracticeController.cancelByApplicant 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | requestId | path | integer | 是 | requestId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/practice-requests/1/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | JoinRequestDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "practicePostId": 1,
    "applicantUserId": 1,
    "joinStatus": "pending",
    "joinMessage": "TODO: 待补充",
    "actedByUserId": 1,
    "actedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:81；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的约练
- **路径**：`GET /api/h5/practices/mine`
- **描述**：查询我的约练接口（由 PracticeController.myPosts 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/practices/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<PracticePostDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.creatorUserId | integer | creatorUserId 字段（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.locationAddress | String | locationAddress 字段（推断） |
  | data.longitude | number | 经度（推断） |
  | data.latitude | number | 纬度（推断） |
  | data.skillLevel | String | skillLevel 字段（推断） |
  | data.expectedPeopleMin | integer | expectedPeopleMin 字段（推断） |
  | data.expectedPeopleMax | integer | expectedPeopleMax 字段（推断） |
  | data.currentPeopleCount | integer | currentPeopleCount 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.expiresAt | OffsetDateTime | expiresAt 字段（推断） |
  | data.postStatus | String | postStatus 字段（推断） |
  | data.description | String | description 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "creatorUserId": 1,
      "danceStyleId": 1,
      "studioId": 1,
      "cityId": 1,
      "locationName": "TODO: 待补充",
      "locationAddress": "TODO: 待补充",
      "longitude": 99,
      "latitude": 99,
      "skillLevel": "TODO: 待补充",
      "expectedPeopleMin": 1,
      "expectedPeopleMax": 1,
      "currentPeopleCount": 1,
      "startAt": "2026-05-23T10:00:00+08:00",
      "endAt": "2026-05-23T10:00:00+08:00",
      "expiresAt": "2026-05-23T10:00:00+08:00",
      "postStatus": "pending",
      "description": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:86；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 约练 myJoinRequests
- **路径**：`GET /api/h5/practice-requests/mine`
- **描述**：约练 myJoinRequests接口（由 PracticeController.myJoinRequests 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/practice-requests/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<JoinRequestDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.practicePostId | integer | practicePostId 字段（推断） |
  | data.applicantUserId | integer | applicantUserId 字段（推断） |
  | data.joinStatus | String | joinStatus 字段（推断） |
  | data.joinMessage | String | joinMessage 字段（推断） |
  | data.actedByUserId | integer | actedByUserId 字段（推断） |
  | data.actedAt | OffsetDateTime | actedAt 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "practicePostId": 1,
      "applicantUserId": 1,
      "joinStatus": "pending",
      "joinMessage": "TODO: 待补充",
      "actedByUserId": 1,
      "actedAt": "2026-05-23T10:00:00+08:00",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、PRACTICE_FULL、PRACTICE_NOT_FOUND、PRACTICE_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/practice/controller/PracticeController.java:91；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 用户资料

### 查询
- **路径**：`GET /api/h5/profile`
- **描述**：查询接口（由 ProfileController.get 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ProfileResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.userId | integer | 用户 ID（推断） |
  | data.nickname | String | 昵称（推断） |
  | data.avatarAssetId | integer | avatarAssetId 字段（推断） |
  | data.gender | String | 性别（推断） |
  | data.birthday | LocalDate | 生日，格式 ISO 8601 日期（推断） |
  | data.bio | String | 个人简介（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.currentLevel | String | currentLevel 字段（推断） |
  | data.learningGoal | String | learningGoal 字段（推断） |
  | data.styles | List<StylePreferenceDto> | styles 字段（推断） |
  | data.privacy | PrivacyDto | privacy 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "userId": 1,
    "nickname": "TODO: 待补充",
    "avatarAssetId": 1,
    "gender": "TODO: 待补充",
    "birthday": "2026-05-23",
    "bio": "TODO: 待补充",
    "cityId": 1,
    "currentLevel": "TODO: 待补充",
    "learningGoal": "TODO: 待补充",
    "styles": [
      {
        "danceStyleId": 1,
        "name": "TODO: 待补充",
        "skillLevel": "TODO: 待补充",
        "isPrimary": true
      }
    ],
    "privacy": {
      "profileVisibility": "TODO: 待补充",
      "growthVisibility": "TODO: 待补充",
      "practiceVisibility": "TODO: 待补充",
      "contentVisibility": "TODO: 待补充"
    }
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/profile/controller/ProfileController.java:25；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 更新
- **路径**：`PUT /api/h5/profile`
- **描述**：更新接口（由 ProfileController.update 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | nickname | body | String | 否 | 昵称（推断）；校验：Size(max = 100) | TODO: 待补充 |
  | avatarAssetId | body | integer | 否 | avatarAssetId 字段（推断） | 1 |
  | gender | body | String | 否 | 性别（推断） | TODO: 待补充 |
  | birthday | body | LocalDate | 否 | 生日，格式 ISO 8601 日期（推断） | 2026-05-23 |
  | bio | body | String | 否 | 个人简介（推断）；校验：Size(max = 1000) | TODO: 待补充 |
  | cityId | body | integer | 否 | 城市 ID（推断） | 1 |
  | currentLevel | body | String | 否 | currentLevel 字段（推断） | TODO: 待补充 |
  | learningGoal | body | String | 否 | learningGoal 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |
  | styles | body | List<StylePreferenceDto> | 否 | styles 字段（推断） | [] |
  | privacy | body | PrivacyDto | 否 | privacy 字段（推断） | TODO: 待补充 |

- **请求示例**

```bash
curl -X PUT "http://localhost:8080/api/h5/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "nickname": "示例内容",
  "avatarAssetId": 1,
  "gender": "示例内容",
  "birthday": "2026-05-23",
  "bio": "示例内容",
  "cityId": 1,
  "currentLevel": "示例内容",
  "learningGoal": "示例内容",
  "styles": [],
  "privacy": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ProfileResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.userId | integer | 用户 ID（推断） |
  | data.nickname | String | 昵称（推断） |
  | data.avatarAssetId | integer | avatarAssetId 字段（推断） |
  | data.gender | String | 性别（推断） |
  | data.birthday | LocalDate | 生日，格式 ISO 8601 日期（推断） |
  | data.bio | String | 个人简介（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.currentLevel | String | currentLevel 字段（推断） |
  | data.learningGoal | String | learningGoal 字段（推断） |
  | data.styles | List<StylePreferenceDto> | styles 字段（推断） |
  | data.privacy | PrivacyDto | privacy 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "userId": 1,
    "nickname": "TODO: 待补充",
    "avatarAssetId": 1,
    "gender": "TODO: 待补充",
    "birthday": "2026-05-23",
    "bio": "TODO: 待补充",
    "cityId": 1,
    "currentLevel": "TODO: 待补充",
    "learningGoal": "TODO: 待补充",
    "styles": [
      {
        "danceStyleId": 1,
        "name": "TODO: 待补充",
        "skillLevel": "TODO: 待补充",
        "isPrimary": true
      }
    ],
    "privacy": {
      "profileVisibility": "TODO: 待补充",
      "growthVisibility": "TODO: 待补充",
      "practiceVisibility": "TODO: 待补充",
      "contentVisibility": "TODO: 待补充"
    }
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/profile/controller/ProfileController.java:30；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 评价与申诉

### 创建
- **路径**：`POST /api/h5/review-appeals`
- **描述**：创建接口（由 ReviewAppealController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | reviewId | body | integer | 是 | reviewId 字段（推断） | 1 |
  | appealReason | body | String | 是 | appealReason 字段（推断）；校验：Size(min = 5, max = 2000) | TODO: 待补充 |
  | evidenceNote | body | String | 否 | evidenceNote 字段（推断）；校验：Size(max = 2000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/review-appeals" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reviewId": 1,
  "appealReason": "示例内容",
  "evidenceNote": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewAppealDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.appellantUserId | integer | appellantUserId 字段（推断） |
  | data.appealReason | String | appealReason 字段（推断） |
  | data.appealStatus | String | appealStatus 字段（推断） |
  | data.evidenceNote | String | evidenceNote 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reviewId": 1,
    "appellantUserId": 1,
    "appealReason": "TODO: 待补充",
    "appealStatus": "pending",
    "evidenceNote": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "APPEAL_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：APPEAL_DUPLICATED、APPEAL_NOT_FOUND、APPEAL_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/review-appeals/mine`
- **描述**：查询我的列表接口（由 ReviewAppealController.mine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/review-appeals/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<ReviewAppealDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.appellantUserId | integer | appellantUserId 字段（推断） |
  | data.appealReason | String | appealReason 字段（推断） |
  | data.appealStatus | String | appealStatus 字段（推断） |
  | data.evidenceNote | String | evidenceNote 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "reviewId": 1,
      "appellantUserId": 1,
      "appealReason": "TODO: 待补充",
      "appealStatus": "pending",
      "evidenceNote": "TODO: 待补充",
      "reviewedByUserId": 1,
      "reviewedAt": "2026-05-23T10:00:00+08:00",
      "reviewRemark": "TODO: 待补充",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "APPEAL_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：APPEAL_DUPLICATED、APPEAL_NOT_FOUND、APPEAL_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java:36；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 按状态查询列表
- **路径**：`GET /api/admin/review-appeals`
- **描述**：按状态查询列表接口（由 ReviewAppealController.listByStatus 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | status | query | String | 否 | 状态（推断） | pending |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/review-appeals?status=pending&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<ReviewAppealDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.appellantUserId | integer | appellantUserId 字段（推断） |
  | data.appealReason | String | appealReason 字段（推断） |
  | data.appealStatus | String | appealStatus 字段（推断） |
  | data.evidenceNote | String | evidenceNote 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "reviewId": 1,
        "appellantUserId": 1,
        "appealReason": "TODO: 待补充",
        "appealStatus": "pending",
        "evidenceNote": "TODO: 待补充",
        "reviewedByUserId": 1,
        "reviewedAt": "2026-05-23T10:00:00+08:00",
        "reviewRemark": "TODO: 待补充",
        "createdAt": "2026-05-23T10:00:00+08:00"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "APPEAL_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：APPEAL_DUPLICATED、APPEAL_NOT_FOUND、APPEAL_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java:41；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 审核通过
- **路径**：`POST /api/admin/review-appeals/{id}/approve`
- **描述**：审核通过接口（由 ReviewAppealController.approve 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/review-appeals/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewAppealDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.appellantUserId | integer | appellantUserId 字段（推断） |
  | data.appealReason | String | appealReason 字段（推断） |
  | data.appealStatus | String | appealStatus 字段（推断） |
  | data.evidenceNote | String | evidenceNote 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reviewId": 1,
    "appellantUserId": 1,
    "appealReason": "TODO: 待补充",
    "appealStatus": "pending",
    "evidenceNote": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "APPEAL_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：APPEAL_DUPLICATED、APPEAL_NOT_FOUND、APPEAL_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java:50；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/admin/review-appeals/{id}/reject`
- **描述**：拒绝接口（由 ReviewAppealController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | remark | body | String | 否 | 备注（推断）；校验：Size(max = 1000) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/review-appeals/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "remark": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewAppealDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.appellantUserId | integer | appellantUserId 字段（推断） |
  | data.appealReason | String | appealReason 字段（推断） |
  | data.appealStatus | String | appealStatus 字段（推断） |
  | data.evidenceNote | String | evidenceNote 字段（推断） |
  | data.reviewedByUserId | integer | reviewedByUserId 字段（推断） |
  | data.reviewedAt | OffsetDateTime | 审核时间，格式 ISO 8601（推断） |
  | data.reviewRemark | String | reviewRemark 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reviewId": 1,
    "appellantUserId": 1,
    "appealReason": "TODO: 待补充",
    "appealStatus": "pending",
    "evidenceNote": "TODO: 待补充",
    "reviewedByUserId": 1,
    "reviewedAt": "2026-05-23T10:00:00+08:00",
    "reviewRemark": "TODO: 待补充",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "APPEAL_DUPLICATED",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：APPEAL_DUPLICATED、APPEAL_NOT_FOUND、APPEAL_STATE_CONFLICT、FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewAppealController.java:58；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建
- **路径**：`POST /api/h5/reviews`
- **描述**：创建接口（由 ReviewController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | body | String | 是 | 目标类型（推断）；可选值：studio / course / coach | studio |
  | targetId | body | integer | 是 | 目标 ID（推断） | 1 |
  | overallScore | body | number | 是 | overallScore 字段（推断）；校验：DecimalMin("1.00"), DecimalMax("5.00") | 99.00 |
  | contentText | body | String | 否 | 正文内容（推断）；校验：Size(max = 5000) | TODO: 待补充 |
  | dimensions | body | List<DimensionScoreDto> | 是 | dimensions 字段（推断） | [] |
  | sourceType | body | String | 否 | sourceType 字段（推断）；可选值：trial / order / checkin | TODO: 待补充 |
  | sourceRefId | body | integer | 否 | sourceRefId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/reviews" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "targetType": "studio",
  "targetId": 1,
  "overallScore": 99,
  "contentText": "示例内容",
  "dimensions": [],
  "sourceType": "示例内容",
  "sourceRefId": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.overallScore | number | overallScore 字段（推断） |
  | data.contentText | String | 正文内容（推断） |
  | data.isVerified | Boolean | isVerified 字段（推断） |
  | data.verifiedSourceType | String | verifiedSourceType 字段（推断） |
  | data.weightFactor | number | weightFactor 字段（推断） |
  | data.reviewStatus | String | reviewStatus 字段（推断） |
  | data.riskLevel | integer | riskLevel 字段（推断） |
  | data.helpfulCount | integer | helpfulCount 字段（推断） |
  | data.isPinned | Boolean | isPinned 字段（推断） |
  | data.publishedAt | OffsetDateTime | 发布时间，格式 ISO 8601（推断） |
  | data.dimensions | List<DimensionScoreDto> | dimensions 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "targetType": "studio",
    "targetId": 1,
    "overallScore": 99,
    "contentText": "TODO: 待补充",
    "isVerified": true,
    "verifiedSourceType": "TODO: 待补充",
    "weightFactor": 99,
    "reviewStatus": "pending",
    "riskLevel": 1,
    "helpfulCount": 1,
    "isPinned": true,
    "publishedAt": "2026-05-23T10:00:00+08:00",
    "dimensions": [
      {
        "code": 123456,
        "name": "TODO: 待补充",
        "score": 1
      }
    ]
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewController.java:32；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除
- **路径**：`DELETE /api/h5/reviews/{id}`
- **描述**：删除接口（由 ReviewController.delete 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/reviews/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewController.java:37；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 列表查询
- **路径**：`GET /api/public/reviews`
- **描述**：列表查询接口（由 ReviewController.list 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | query | String | 是 | 目标类型（推断） | studio |
  | targetId | query | integer | 是 | 目标 ID（推断） | 1 |
  | sort | query | String | 否 | sort 字段（推断） | TODO: 待补充 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/reviews?targetType=studio&targetId=1&sort=demo&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<ReviewDto> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "userId": 1,
        "targetType": "studio",
        "targetId": 1,
        "overallScore": 99,
        "contentText": "TODO: 待补充",
        "isVerified": true,
        "verifiedSourceType": "TODO: 待补充",
        "weightFactor": 99,
        "reviewStatus": "pending",
        "riskLevel": 1,
        "helpfulCount": 1,
        "isPinned": true,
        "publishedAt": "2026-05-23T10:00:00+08:00",
        "dimensions": [
          {
            "code": 123456,
            "name": "TODO: 待补充",
            "score": 1
          }
        ]
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewController.java:43；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 评价汇总
- **路径**：`GET /api/public/reviews/summary`
- **描述**：评价汇总接口（由 ReviewController.summary 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | targetType | query | String | 是 | 目标类型（推断） | studio |
  | targetId | query | integer | 是 | 目标 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/reviews/summary?targetType=studio&targetId=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewSummary | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.targetType | String | 目标类型（推断） |
  | data.targetId | integer | 目标 ID（推断） |
  | data.count | integer | count 字段（推断） |
  | data.verifiedCount | integer | verifiedCount 字段（推断） |
  | data.weightedAvgScore | number | weightedAvgScore 字段（推断） |
  | data.dimensionAvg | Map<String, number> | dimensionAvg 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "targetType": "studio",
    "targetId": 1,
    "count": 1,
    "verifiedCount": 1,
    "weightedAvgScore": 99,
    "dimensionAvg": 99
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewController.java:54；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建
- **路径**：`POST /api/h5/review-replies`
- **描述**：创建接口（由 ReviewReplyController.create 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | reviewId | body | integer | 是 | reviewId 字段（推断） | 1 |
  | replyContent | body | String | 是 | replyContent 字段（推断）；校验：Size(min = 1, max = 1000) | TODO: 待补充 |
  | isOfficial | body | Boolean | 否 | isOfficial 字段（推断） | true |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/review-replies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reviewId": 1,
  "replyContent": "示例内容",
  "isOfficial": true
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | ReviewReplyDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.replierUserId | integer | replierUserId 字段（推断） |
  | data.replyContent | String | replyContent 字段（推断） |
  | data.isOfficial | Boolean | isOfficial 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "reviewId": 1,
    "replierUserId": 1,
    "replyContent": "TODO: 待补充",
    "isOfficial": true,
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPLY_NOT_FOUND、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewReplyController.java:31；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 删除
- **路径**：`DELETE /api/h5/review-replies/{id}`
- **描述**：删除接口（由 ReviewReplyController.delete 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X DELETE "http://localhost:8080/api/h5/review-replies/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Map<String, Object> | 业务数据 |
  | traceId | String | 链路追踪 ID |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "result": "TODO: 待补充"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPLY_NOT_FOUND、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewReplyController.java:36；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 评价与申诉 listByReview
- **路径**：`GET /api/public/review-replies`
- **描述**：评价与申诉 listByReview接口（由 ReviewReplyController.listByReview 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | reviewId | query | integer | 是 | reviewId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/review-replies?reviewId=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<ReviewReplyDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.replierUserId | integer | replierUserId 字段（推断） |
  | data.replyContent | String | replyContent 字段（推断） |
  | data.isOfficial | Boolean | isOfficial 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "reviewId": 1,
      "replierUserId": 1,
      "replyContent": "TODO: 待补充",
      "isOfficial": true,
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、REPLY_NOT_FOUND、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewReplyController.java:42；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/review-replies/mine`
- **描述**：查询我的列表接口（由 ReviewReplyController.mine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/review-replies/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<ReviewReplyDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.reviewId | integer | reviewId 字段（推断） |
  | data.replierUserId | integer | replierUserId 字段（推断） |
  | data.replyContent | String | replyContent 字段（推断） |
  | data.isOfficial | Boolean | isOfficial 字段（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "reviewId": 1,
      "replierUserId": 1,
      "replyContent": "TODO: 待补充",
      "isOfficial": true,
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、REPLY_NOT_FOUND、REVIEW_NOT_FOUND、REVIEW_STATE_CONFLICT、UNAUTHORIZED
- **备注**：来源 backend/src/main/java/com/bitdance/review/controller/ReviewReplyController.java:47；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## Workshop 活动

### 列表查询
- **路径**：`GET /api/admin/workshops`
- **描述**：列表查询接口（由 AdminWorkshopController.list 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | auditStatus | query | String | 否 | auditStatus 字段（推断） | pending |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/admin/workshops?auditStatus=pending&page=1&pageSize=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | Page<WorkshopAdminItem> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "studioId": 1,
        "coachId": 1,
        "cityId": 1,
        "workshopName": "TODO: 待补充",
        "priceAmount": 99,
        "signupDeadline": "2026-05-23T10:00:00+08:00",
        "auditStatus": "pending",
        "publishStatus": "pending"
      }
    ],
    "pageable": "TODO: 待补充",
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/AdminWorkshopController.java:25；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 审核通过
- **路径**：`POST /api/admin/workshops/{id}/approve`
- **描述**：审核通过接口（由 AdminWorkshopController.approve 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/workshops/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopAdminItem | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "workshopName": "TODO: 待补充",
    "priceAmount": 99,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "auditStatus": "pending",
    "publishStatus": "pending"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/AdminWorkshopController.java:34；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 拒绝
- **路径**：`POST /api/admin/workshops/{id}/reject`
- **描述**：拒绝接口（由 AdminWorkshopController.reject 定义）。
- **鉴权**：是（Bearer JWT，角色 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/admin/workshops/1/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopAdminItem | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "workshopName": "TODO: 待补充",
    "priceAmount": 99,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "auditStatus": "pending",
    "publishStatus": "pending"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/AdminWorkshopController.java:39；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 签到
- **路径**：`POST /api/merchant/workshop-orders/{id}/checkin`
- **描述**：签到接口（由 MerchantWorkshopCheckinController.checkin 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | code | body | String | 是 | 验证码或业务编码（推断） | 123456 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/workshop-orders/1/checkin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "code": 123456
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CHECKIN_CODE_INVALID",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CHECKIN_CODE_INVALID、CHECKIN_NOT_FOUND、CHECKIN_TICKET_NOT_FOUND、CHECKIN_TOO_EARLY、CHECKIN_TOO_LATE、FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopCheckinController.java:25；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建
- **路径**：`POST /api/merchant/workshops`
- **描述**：创建接口（由 MerchantWorkshopController.create 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | studioId | body | integer | 是 | 舞室 ID（推断） | 1 |
  | coachId | body | integer | 否 | 教练 ID（推断） | 1 |
  | cityId | body | integer | 是 | 城市 ID（推断） | 1 |
  | danceStyleId | body | integer | 否 | 舞种 ID（推断） | 1 |
  | workshopName | body | String | 是 | workshopName 字段（推断）；校验：Size(max = 200) | TODO: 待补充 |
  | coverAssetId | body | integer | 否 | coverAssetId 字段（推断） | 1 |
  | intro | body | String | 否 | intro 字段（推断）；校验：Size(max = 5000) | TODO: 待补充 |
  | address | body | String | 是 | address 字段（推断）；校验：Size(max = 1000) | TODO: 待补充 |
  | locationName | body | String | 是 | locationName 字段（推断）；校验：Size(max = 200) | TODO: 待补充 |
  | longitude | body | number | 否 | 经度（推断） | 99.00 |
  | latitude | body | number | 否 | 纬度（推断） | 99.00 |
  | priceAmount | body | number | 是 | priceAmount 字段（推断） | 99.00 |
  | minPeople | body | integer | 否 | minPeople 字段（推断）；校验：Min(1), Max(500) | 1 |
  | maxPeople | body | integer | 否 | maxPeople 字段（推断）；校验：Min(1), Max(500) | 1 |
  | signupDeadline | body | OffsetDateTime | 否 | 报名截止时间，格式 ISO 8601（推断） | 2026-05-23T10:00:00+08:00 |
  | sourceType | body | String | 否 | sourceType 字段（推断）；可选值：studio / coach | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/workshops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "studioId": 1,
  "coachId": 1,
  "cityId": 1,
  "danceStyleId": 1,
  "workshopName": "示例内容",
  "coverAssetId": 1,
  "intro": "示例内容",
  "address": "示例内容",
  "locationName": "示例内容",
  "longitude": 99,
  "latitude": 99,
  "priceAmount": 99,
  "minPeople": 1,
  "maxPeople": 1,
  "signupDeadline": "2026-05-23T10:00:00+08:00",
  "sourceType": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.address | String | address 字段（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.minPeople | integer | minPeople 字段（推断） |
  | data.maxPeople | integer | maxPeople 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.sessions | List<SessionDto> | sessions 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "danceStyleId": 1,
    "workshopName": "TODO: 待补充",
    "coverAssetId": 1,
    "intro": "TODO: 待补充",
    "address": "TODO: 待补充",
    "locationName": "TODO: 待补充",
    "priceAmount": 99,
    "minPeople": 1,
    "maxPeople": 1,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "publishStatus": "pending",
    "auditStatus": "pending",
    "sessions": [
      {
        "id": 1,
        "workshopId": 1,
        "sessionName": "TODO: 待补充",
        "startAt": "2026-05-23T10:00:00+08:00",
        "endAt": "2026-05-23T10:00:00+08:00",
        "capacity": 1,
        "soldCount": 1,
        "checkinCount": 1,
        "sessionStatus": "pending"
      }
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopController.java:27；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 上架
- **路径**：`POST /api/merchant/workshops/{id}/publish`
- **描述**：上架接口（由 MerchantWorkshopController.publish 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/workshops/1/publish" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.address | String | address 字段（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.minPeople | integer | minPeople 字段（推断） |
  | data.maxPeople | integer | maxPeople 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.sessions | List<SessionDto> | sessions 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "danceStyleId": 1,
    "workshopName": "TODO: 待补充",
    "coverAssetId": 1,
    "intro": "TODO: 待补充",
    "address": "TODO: 待补充",
    "locationName": "TODO: 待补充",
    "priceAmount": 99,
    "minPeople": 1,
    "maxPeople": 1,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "publishStatus": "pending",
    "auditStatus": "pending",
    "sessions": [
      {
        "id": 1,
        "workshopId": 1,
        "sessionName": "TODO: 待补充",
        "startAt": "2026-05-23T10:00:00+08:00",
        "endAt": "2026-05-23T10:00:00+08:00",
        "capacity": 1,
        "soldCount": 1,
        "checkinCount": 1,
        "sessionStatus": "pending"
      }
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopController.java:32；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 下架
- **路径**：`POST /api/merchant/workshops/{id}/offline`
- **描述**：下架接口（由 MerchantWorkshopController.offline 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/workshops/1/offline" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.address | String | address 字段（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.minPeople | integer | minPeople 字段（推断） |
  | data.maxPeople | integer | maxPeople 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.sessions | List<SessionDto> | sessions 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "danceStyleId": 1,
    "workshopName": "TODO: 待补充",
    "coverAssetId": 1,
    "intro": "TODO: 待补充",
    "address": "TODO: 待补充",
    "locationName": "TODO: 待补充",
    "priceAmount": 99,
    "minPeople": 1,
    "maxPeople": 1,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "publishStatus": "pending",
    "auditStatus": "pending",
    "sessions": [
      {
        "id": 1,
        "workshopId": 1,
        "sessionName": "TODO: 待补充",
        "startAt": "2026-05-23T10:00:00+08:00",
        "endAt": "2026-05-23T10:00:00+08:00",
        "capacity": 1,
        "soldCount": 1,
        "checkinCount": 1,
        "sessionStatus": "pending"
      }
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopController.java:37；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 新增场次
- **路径**：`POST /api/merchant/workshop-sessions`
- **描述**：新增场次接口（由 MerchantWorkshopController.addSession 定义）。
- **鉴权**：是（Bearer JWT，角色 STUDIO_ADMIN 或 PLATFORM_ADMIN）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | workshopId | body | integer | 是 | workshopId 字段（推断） | 1 |
  | sessionName | body | String | 否 | sessionName 字段（推断）；校验：Size(max = 100) | TODO: 待补充 |
  | startAt | body | OffsetDateTime | 是 | 开始时间，格式 ISO 8601（推断） | 2026-05-23T10:00:00+08:00 |
  | endAt | body | OffsetDateTime | 是 | 结束时间，格式 ISO 8601（推断） | 2026-05-23T10:00:00+08:00 |
  | capacity | body | integer | 是 | capacity 字段（推断）；校验：Min(1) | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/merchant/workshop-sessions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "workshopId": 1,
  "sessionName": "示例内容",
  "startAt": "2026-05-23T10:00:00+08:00",
  "endAt": "2026-05-23T10:00:00+08:00",
  "capacity": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | SessionDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.sessionName | String | sessionName 字段（推断） |
  | data.startAt | OffsetDateTime | 开始时间，格式 ISO 8601（推断） |
  | data.endAt | OffsetDateTime | 结束时间，格式 ISO 8601（推断） |
  | data.capacity | integer | capacity 字段（推断） |
  | data.soldCount | integer | soldCount 字段（推断） |
  | data.checkinCount | integer | checkinCount 字段（推断） |
  | data.sessionStatus | String | sessionStatus 字段（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "workshopId": 1,
    "sessionName": "TODO: 待补充",
    "startAt": "2026-05-23T10:00:00+08:00",
    "endAt": "2026-05-23T10:00:00+08:00",
    "capacity": 1,
    "soldCount": 1,
    "checkinCount": 1,
    "sessionStatus": "pending"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、SESSION_NOT_AVAILABLE、SESSION_NOT_FOUND、SESSION_STARTED、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/MerchantWorkshopController.java:42；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 列表查询
- **路径**：`GET /api/public/workshops`
- **描述**：列表查询接口（由 WorkshopController.list 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | cityId | query | integer | 否 | 城市 ID（推断） | 1 |
  | danceStyleId | query | integer | 否 | 舞种 ID（推断） | 1 |
  | page | query | integer | 是 | 页码，从 1 开始（推断） | 1 |
  | pageSize | query | integer | 是 | 每页数量（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/workshops?cityId=1&danceStyleId=1&page=1&pageSize=1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopListResponse | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.list | List<WorkshopBrief> | 数据列表（推断） |
  | data.page | integer | 页码，从 1 开始（推断） |
  | data.pageSize | integer | 每页数量（推断） |
  | data.total | integer | 总数（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "studioId": 1,
        "coachId": 1,
        "cityId": 1,
        "danceStyleId": 1,
        "workshopName": "TODO: 待补充",
        "coverAssetId": 1,
        "locationName": "TODO: 待补充",
        "priceAmount": 99,
        "signupDeadline": "2026-05-23T10:00:00+08:00",
        "publishStatus": "pending"
      }
    ],
    "page": 1,
    "pageSize": 1,
    "total": 1
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:35；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 详情查询
- **路径**：`GET /api/public/workshops/{id}`
- **描述**：详情查询接口（由 WorkshopController.detail 定义）。
- **鉴权**：否（匿名可访问）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/public/workshops/1" \
  -H "Content-Type: application/json"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | WorkshopDetail | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.studioId | integer | 舞室 ID（推断） |
  | data.coachId | integer | 教练 ID（推断） |
  | data.cityId | integer | 城市 ID（推断） |
  | data.danceStyleId | integer | 舞种 ID（推断） |
  | data.workshopName | String | workshopName 字段（推断） |
  | data.coverAssetId | integer | coverAssetId 字段（推断） |
  | data.intro | String | intro 字段（推断） |
  | data.address | String | address 字段（推断） |
  | data.locationName | String | locationName 字段（推断） |
  | data.priceAmount | number | priceAmount 字段（推断） |
  | data.minPeople | integer | minPeople 字段（推断） |
  | data.maxPeople | integer | maxPeople 字段（推断） |
  | data.signupDeadline | OffsetDateTime | 报名截止时间，格式 ISO 8601（推断） |
  | data.publishStatus | String | publishStatus 字段（推断） |
  | data.auditStatus | String | auditStatus 字段（推断） |
  | data.sessions | List<SessionDto> | sessions 字段（推断） |
  | data.favored | boolean | 是否已收藏（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "studioId": 1,
    "coachId": 1,
    "cityId": 1,
    "danceStyleId": 1,
    "workshopName": "TODO: 待补充",
    "coverAssetId": 1,
    "intro": "TODO: 待补充",
    "address": "TODO: 待补充",
    "locationName": "TODO: 待补充",
    "priceAmount": 99,
    "minPeople": 1,
    "maxPeople": 1,
    "signupDeadline": "2026-05-23T10:00:00+08:00",
    "publishStatus": "pending",
    "auditStatus": "pending",
    "sessions": [
      {
        "id": 1,
        "workshopId": 1,
        "sessionName": "TODO: 待补充",
        "startAt": "2026-05-23T10:00:00+08:00",
        "endAt": "2026-05-23T10:00:00+08:00",
        "capacity": 1,
        "soldCount": 1,
        "checkinCount": 1,
        "sessionStatus": "pending"
      }
    ],
    "favored": true
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：INVALID_ARGUMENT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:45；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 创建订单
- **路径**：`POST /api/h5/workshop-orders`
- **描述**：创建订单接口（由 WorkshopController.createOrder 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | workshopId | body | integer | 是 | workshopId 字段（推断） | 1 |
  | sessionId | body | integer | 是 | sessionId 字段（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/workshop-orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "workshopId": 1,
  "sessionId": 1
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:52；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 支付
- **路径**：`POST /api/h5/workshop-orders/{id}/pay`
- **描述**：支付接口（由 WorkshopController.pay 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/workshop-orders/1/pay" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:57；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 取消
- **路径**：`POST /api/h5/workshop-orders/{id}/cancel`
- **描述**：取消接口（由 WorkshopController.cancel 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/workshop-orders/1/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:62；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 退款
- **路径**：`POST /api/h5/workshop-orders/{id}/refund`
- **描述**：退款接口（由 WorkshopController.refund 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | reason | body | String | 否 | 原因（推断）；校验：Size(max = 500) | TODO: 待补充 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/workshop-orders/1/refund" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "reason": "示例内容"
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:67；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 查询我的列表
- **路径**：`GET /api/h5/workshop-orders/mine`
- **描述**：查询我的列表接口（由 WorkshopController.mine 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | TODO: 待补充 | 代码未声明显式请求参数 | TODO: 待补充 |

- **请求示例**

```bash
curl -X GET "http://localhost:8080/api/h5/workshop-orders/mine" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>"
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | List<OrderDto> | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": [
    {
      "id": 1,
      "orderNo": "TODO: 待补充",
      "workshopId": 1,
      "workshopSessionId": 1,
      "userId": 1,
      "amountPayable": 99,
      "amountPaid": 99,
      "orderStatus": "pending",
      "paymentTxnNo": "TODO: 待补充",
      "checkinCode": "TODO: 待补充",
      "paidAt": "2026-05-23T10:00:00+08:00",
      "canceledAt": "2026-05-23T10:00:00+08:00",
      "refundedAt": "2026-05-23T10:00:00+08:00",
      "createdAt": "2026-05-23T10:00:00+08:00"
    }
  ],
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "FORBIDDEN",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:75；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

### 签到
- **路径**：`POST /api/h5/workshop-orders/{id}/checkin`
- **描述**：签到接口（由 WorkshopController.checkin 定义）。
- **鉴权**：是（Bearer JWT，任意已登录用户）
- **请求参数**

  | 参数 | 位置 | 类型 | 必填 | 说明 | 示例 |
  |------|------|------|------|------|------|
  | id | path | integer | 是 | 主键 ID（推断） | 1 |
  | code | body | String | 是 | 验证码或业务编码（推断） | 123456 |

- **请求示例**

```bash
curl -X POST "http://localhost:8080/api/h5/workshop-orders/1/checkin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
  "code": 123456
}'
```

- **响应字段**

  | 字段 | 类型 | 说明 |
  |------|------|------|
  | code | String | 响应码，成功为 SUCCESS |
  | message | String | 响应消息 |
  | data | OrderDto | 业务数据 |
  | traceId | String | 链路追踪 ID |
  | data.id | integer | 主键 ID（推断） |
  | data.orderNo | String | 订单号（推断） |
  | data.workshopId | integer | workshopId 字段（推断） |
  | data.workshopSessionId | integer | workshopSessionId 字段（推断） |
  | data.userId | integer | 用户 ID（推断） |
  | data.amountPayable | number | 应付金额（推断） |
  | data.amountPaid | number | 实付金额（推断） |
  | data.orderStatus | String | 订单状态（推断） |
  | data.paymentTxnNo | String | 支付交易号（推断） |
  | data.checkinCode | String | 签到码（推断） |
  | data.paidAt | OffsetDateTime | 支付时间，格式 ISO 8601（推断） |
  | data.canceledAt | OffsetDateTime | 取消时间，格式 ISO 8601（推断） |
  | data.refundedAt | OffsetDateTime | 退款时间，格式 ISO 8601（推断） |
  | data.createdAt | OffsetDateTime | 创建时间，格式 ISO 8601（推断） |

- **响应示例**

成功：
```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "TODO: 待补充",
    "workshopId": 1,
    "workshopSessionId": 1,
    "userId": 1,
    "amountPayable": 99,
    "amountPaid": 99,
    "orderStatus": "pending",
    "paymentTxnNo": "TODO: 待补充",
    "checkinCode": "TODO: 待补充",
    "paidAt": "2026-05-23T10:00:00+08:00",
    "canceledAt": "2026-05-23T10:00:00+08:00",
    "refundedAt": "2026-05-23T10:00:00+08:00",
    "createdAt": "2026-05-23T10:00:00+08:00"
  },
  "traceId": "01HXEXAMPLETRACE"
}
```
失败：
```json
{
  "code": "CHECKIN_CODE_INVALID",
  "message": "TODO: 待补充",
  "data": null,
  "traceId": "01HXEXAMPLETRACE"
}
```

- **错误码**：CHECKIN_CODE_INVALID、CHECKIN_NOT_FOUND、CHECKIN_TICKET_NOT_FOUND、CHECKIN_TOO_EARLY、CHECKIN_TOO_LATE、FORBIDDEN、INVALID_ARGUMENT、ORDER_NOT_FOUND、ORDER_STATE_CONFLICT、UNAUTHORIZED、WORKSHOP_AUDIT_STATE_CONFLICT、WORKSHOP_FULL、WORKSHOP_NOT_APPROVED、WORKSHOP_NOT_FOUND、WORKSHOP_NOT_PUBLISHED、WORKSHOP_STATE_CONFLICT、WORK_NOT_FOUND
- **备注**：来源 backend/src/main/java/com/bitdance/workshop/controller/WorkshopController.java:80；限流/幂等/异步/废弃策略代码未体现，TODO: 待补充。

## 扫描覆盖率自检

- 共扫描到 119 个接口，已记录 119 个。
- 未能解析的接口列表：无（Spring MVC Controller 中识别到的 Mapping 均已记录）。
- 推断字段占比：1785/1785（约 100%；当前 DTO 未发现可直接用于字段说明的 Swagger 注解或 Javadoc，字段说明主要按字段名与校验注解推断并标注“（推断）”）。
- 扫描范围：`backend/src/main/java/com/bitdance/**/controller`、`dto`、`domain`、`service`、`common/exception`、`iam/security`、`application.yml`。
