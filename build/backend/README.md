# BitDance 后端（Spring Boot 3 + Java 21）

## 当前进度（W1 起步）

- 工程骨架、统一响应 `{ code, message, data, traceId }`、全局异常、TraceId Filter、JPA Auditing、CORS、SpringDoc OpenAPI
- M3 IAM 起步：手机验证码发送 + 登录、JWT 签发、JwtAuthFilter、AppUser / UserRoleBinding 实体与 Repository
- `/auth/sms/send`、`/auth/login`、`/h5/me` 可调通
- 单元/MockMvc 测试覆盖 AuthController 正反例

后续按 `BE-002 ~ BE-015` 推进（见仓库根目录 `开发历史记录.md` 与 SKILL）。

## 启动

依赖：JDK 21、Maven 3.9+、PostgreSQL（云库或本地）、Redis（本地 6379）。

```bash
cd backend
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# 在 application-local.yml 内填入数据库凭证与 JWT secret
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

服务监听 `8080`，API 前缀 `/api`。

OpenAPI UI：<http://localhost:8080/api/swagger-ui.html>

## 数据库

初始化执行仓库根目录的 `bitdance_postgresql_schema.sql`。默认 schema `bitdance`，所有 JPA 实体在 `application.yml` 内配置 `default_schema: bitdance`。

## 测试

```bash
mvn test
```

测试使用 H2 内存库 + Mockito，不依赖真实 PostgreSQL/Redis（Redis 依赖项目通过 mock service 跳过）。

## API 前缀

| 前缀 | 用途 |
| --- | --- |
| `/api/public/**` | 公开页 |
| `/api/auth/**` | 登录 / 验证码 |
| `/api/h5/**` | 用户/教练 H5 端 |
| `/api/merchant/**` | 舞室管理员后台 |
| `/api/admin/**` | 平台管理员后台 |
| `/api/callback/**` | 第三方回调 |

## 凭证

不要把 `application-local.yml`、`.env`、JWT secret 提交到仓库。`.gitignore` 已覆盖。
