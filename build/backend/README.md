# BitDance 后端（Spring Boot 3 + Java 21）

## 当前进度（W5 收官）

后端 15 段功能点 BE-001 ~ BE-015 全部交付，`mvn test` 233+ 全绿。详见仓库根目录 `开发历史记录.md`。

模块清单：
- common：统一响应、异常、TraceId、JPA Auditing、CORS、Cache（Spring Cache 抽象）、OpenAPI 分组
- iam：手机验证码登录、JWT、RBAC、CurrentUser
- profile：M3 资料 / 偏好 / 隐私
- message：M3 通知中心
- catalog：M1 Studio / Coach / Course / Schedule（含 fn_haversine_km 附近搜索）
- favorite：多态收藏
- booking：M1 Trial Booking（含商家侧 confirm/reject/attend/no_show）
- review：M2 维度评价 + 雷达图 + 风控权重 + Reply + Appeal
- practice：M4 约练发布 / 广场 / 申请接受拒绝 + 过期定时
- buddy：M4 互评 + 搭子关系沉淀
- growth：M5 打卡 / 统计 / 时间线 / 目标 / 作品 / 徽章读取
- community：M6 动态 / 评论 / 点赞 / 话题 / 关注 / 举报 / 搜索
- workshop：M6 Workshop 全流程（用户侧 + 商家创建侧 + 商家核销 + PaymentGateway 抽象）
- coachops：教练运营侧 + 资质审核
- merchant：Studio Claim + Coach Relation + MerchantAccessGuard
- admin：Report 工作台 + Audit Log 查询
- audit：@AuditAction AOP + AuditLogService + REQUIRES_NEW 独立事务
- badge：BadgeRule 接口 + 5 条内置规则 + BadgeRuleEngine
- workshop/job：CloseUnpaidWorkshopOrderJob 定时关单

## 本地启动

依赖：JDK 21、Maven 3.9+、PostgreSQL（云库或本地）、Redis（本地 6379 可选）。

```bash
cd backend
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# 填入数据库凭证与 JWT secret
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

服务监听 `8080`，API 前缀 `/api`。OpenAPI UI：<http://localhost:8080/api/swagger-ui.html>，按 `public / auth / h5 / merchant / admin` 五组切换。

## 测试

```bash
mvn test
```

H2 内存库 + Mockito 切片测试，不依赖 PostgreSQL/Redis。CI 在 GitHub Actions 双 job（backend / frontend）自动跑。

## 部署（Docker 单机演示）

仓库根目录 `docker-compose.yml` 起 Redis + backend + frontend 三个容器。

```bash
# 必须 env：BITDANCE_JWT_SECRET、SPRING_DATASOURCE_PASSWORD
# 可选 env：FRONTEND_ORIGIN、BITDANCE_SMS_MOCK、BITDANCE_PAYMENT_PROVIDER
export BITDANCE_JWT_SECRET=$(openssl rand -hex 32)
export SPRING_DATASOURCE_PASSWORD=...

docker compose up -d --build
# backend → http://localhost:8080/api/actuator/health
# frontend → http://localhost:8081
```

`application-prod.yml` 全部敏感字段走 env：`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`、`SPRING_REDIS_HOST/PORT`、`BITDANCE_JWT_SECRET`、`BITDANCE_PAYMENT_PROVIDER`、`FRONTEND_ORIGIN`。

## 数据库

```bash
psql -f bitdance_postgresql_schema.sql -U BitDance -d bitdance
```

默认 schema `bitdance`，所有 JPA 实体在 `application.yml` 内配置 `default_schema: bitdance`。

## API 前缀

| 前缀 | 用途 | 权限 |
| --- | --- | --- |
| `/api/public/**` | 公开页 | 匿名可访问 |
| `/api/auth/**` | 登录 / 验证码 | 匿名可访问 |
| `/api/h5/**` | 用户/教练 H5 端 | 任意已登录角色 |
| `/api/merchant/**` | 舞室管理员后台 | `STUDIO_ADMIN` 或 `PLATFORM_ADMIN` |
| `/api/admin/**` | 平台管理员后台 | `PLATFORM_ADMIN` |
| `/api/callback/**` | 第三方回调 | 签名校验 |

## 凭证

不要把 `application-local.yml`、`.env`、JWT secret、SMS / 支付 key 提交到仓库。`.gitignore` 已覆盖。`application-prod.yml` 只声明字段名，值从 env 注入。
