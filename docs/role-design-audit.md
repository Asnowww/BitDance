# 用户角色设计审计报告

**项目**：BitDance 舞蹈社区平台  
**审计日期**：2026-06-11  
**审计范围**：`build/backend/` · `build/frontend/` · `bitdance_postgresql_schema.sql`

---

## 一、设计规范 vs 现状对比

| 角色（规范） | 代码中对应值 | 状态 |
|---|---|---|
| 游客 | —（无枚举值） | ❌ 未实现，以未认证访问代替 |
| 普通用户 | `USER` | ✅ 已实现 |
| 舞室管理员 | `STUDIO_ADMIN` | ✅ 已实现 |
| 教练 | `COACH` | ✅ 已实现 |
| 平台管理员 | `PLATFORM_ADMIN` | ✅ 已实现 |

---

## 二、后端实现分析

### 2.1 角色存储模型

**实体**：[UserRoleBinding.java](build/backend/src/main/java/com/bitdance/iam/domain/UserRoleBinding.java)

```java
// 第 22 行
@Column(name = "role", nullable = false, length = 32)
private String role; // USER / COACH / STUDIO_ADMIN / PLATFORM_ADMIN
```

当前代码用一个裸字符串列 `role` 直接存储角色名，可容纳的值为：`USER`、`COACH`、`STUDIO_ADMIN`、`PLATFORM_ADMIN`。字段上没有 CHECK 约束，任何字符串均可写入。

**Schema 设计**（[bitdance_postgresql_schema.sql:303](build/backend/bitdance_postgresql_schema.sql#L303)）则定义了完整的关系型 RBAC 三表结构：

```
sys_role            ← 角色主数据（含 role_scope / status）
sys_permission      ← 权限主数据（resource_code + action_code）
sys_role_permission ← 角色-权限多对多
user_role_binding   ← 绑定表，role_id FK → sys_role.id
                       含 effective_from/to、source_type、granted_by_user_id
```

> **结论**：代码实现与 Schema 设计不一致。Schema 预留的精细权限体系（`sys_permission`、`sys_role_permission`）在代码层**完全未使用**，`user_role_binding` 表的实际 DDL 与 Schema 定义的列也不同（缺少 `role_id` FK、`effective_from`、`is_primary` 等字段）。

---

### 2.2 角色授予流程

| 触发时机 | 授予角色 | 代码位置 |
|---|---|---|
| 手机号/微信注册 | `USER` | [AuthService.java:119](build/backend/src/main/java/com/bitdance/iam/service/AuthService.java#L119) |
| 教练认证申请审批通过 | `COACH` | CoachCertificationService（约 103–111 行） |
| 舞室认领审批通过 | `STUDIO_ADMIN` | StudioClaimService（约 81–90 行） |
| 平台管理员 | 无自动流程（需手动写库） | — |

---

### 2.3 认证与鉴权机制

**JWT 解析**（[JwtAuthFilter.java:43-49](build/backend/src/main/java/com/bitdance/iam/jwt/JwtAuthFilter.java#L43)）

- 从 JWT claims 中取 `roles` 列表，转为 Spring Security Authority：`ROLE_USER`、`ROLE_COACH` 等。
- 无 Token 时不报错，直接放行，由下游路由规则决定是否拦截（匿名访问）。

**路由级鉴权**（[SecurityConfig.java:46-49](build/backend/src/main/java/com/bitdance/iam/security/SecurityConfig.java#L46)）

```java
.requestMatchers("/h5/**").authenticated()
.requestMatchers("/merchant/**").hasAnyRole("STUDIO_ADMIN", "PLATFORM_ADMIN")
.requestMatchers("/admin/**").hasRole("PLATFORM_ADMIN")
.anyRequest().authenticated()
```

**资源级守卫**（[MerchantAccessGuard.java:32-48](build/backend/src/main/java/com/bitdance/merchant/service/MerchantAccessGuard.java#L32)）

- `requireStudioOwnership(userId, studioId)`：平台管理员可越权，其余人必须是该舞室的已认领管理员。
- 通过查库实现，非 Spring Security 注解。

**方法级鉴权**：全库搜索结果显示，仅 `SecurityConfig` 中有 `hasRole`/`hasAnyRole` 调用，**无任何 `@PreAuthorize` 注解**用于 Controller 方法级保护。

---

## 三、前端实现分析

### 3.1 角色状态管理

**文件**：[stores/user.ts](build/frontend/src/stores/user.ts)

```typescript
// 第 59–62 行
const roleSet = computed(() => new Set(profile.value?.roles ?? []));
const isCoach       = computed(() => roleSet.value.has('COACH'));
const isStudioAdmin = computed(() => roleSet.value.has('STUDIO_ADMIN'));
const isPlatformAdmin = computed(() => roleSet.value.has('PLATFORM_ADMIN'));
```

角色从登录响应写入 `localStorage`（key: `bitdance_profile`），无二次服务端校验。

另有 `activeRole: 'user' | 'coach'` 的前端切换（[user.ts:64](build/frontend/src/stores/user.ts#L64)），但仅影响 UI 展示，不影响实际权限。

### 3.2 路由守卫

**文件**：[router/index.ts:368-376](build/frontend/src/router/index.ts#L368)

```typescript
router.beforeEach((to) => {
  if (to.meta?.requiresAuth && !getToken()) {
    return { path: '/login', ... };
  }
  ...
});
```

路由守卫**只校验是否已登录**，不校验角色。以下专属角色路由缺少角色保护：

| 路由 | 应要求角色 | 当前状态 |
|---|---|---|
| `/coach/dashboard` | `COACH` | 仅需登录 ❌ |
| `/coach/workshop-create` | `COACH` | 仅需登录 ❌ |
| `/coach/orders` | `COACH` | 仅需登录 ❌ |
| `/coach/appeal` | `COACH` | 仅需登录 ❌ |
| `/coach/replies` | `COACH` | 仅需登录 ❌ |
| `/admin/reports` | `PLATFORM_ADMIN` | 仅需登录 ❌ |

普通用户知晓 URL 后可直接进入这些页面；虽然后端 API 会返回 403，但前端体验存在漏洞，且页面本身可能渲染出敏感 UI 骨架。

### 3.3 角色驱动的 UI 渲染

[MePage.vue](build/frontend/src/pages/user/MePage.vue) 通过 `isCoach` / `isStudioAdmin` / `isPlatformAdmin` 计算属性控制工作台入口的显示——这是正确做法，但仅限于入口隐藏，不构成访问控制。

---

## 四、问题汇总

### P0 — 安全缺口

| # | 问题 | 影响 |
|---|---|---|
| 1 | **前端路由无角色守卫**，教练/管理员专属路由仅凭登录即可访问 | 普通用户可直达教练看板、举报后台等页面 |
| 2 | **`user_role_binding.role` 无枚举约束**（数据库层无 CHECK，Java 层无枚举类型） | 写入非法角色值不会被拦截，可能导致权限逃逸 |

### P1 — 规范缺口

| # | 问题 | 影响 |
|---|---|---|
| 3 | **游客角色未实现**，当前以"未认证匿名访问"代替 | 无法区分"主动游客"与"未认证用户"，游客限制逻辑（禁止评价/收藏/约练）依赖前端隐藏，后端无对应拦截 |
| 4 | **平台管理员无授予流程**，需直接操作数据库 | 运营初期可手动绕过，缺乏审计日志 |

### P2 — 架构不一致

| # | 问题 | 影响 |
|---|---|---|
| 5 | **Schema 与代码的 `user_role_binding` 表定义不匹配**：Schema 定义了 `role_id FK`、`effective_from`、`is_primary` 等字段，代码实体只有 `role` 字符串列 | 实际建表时若按 Schema 执行会报错；精细化权限体系形同虚设 |
| 6 | **`sys_role` / `sys_permission` / `sys_role_permission` 三表从未使用** | Schema 中预留的 RBAC 能力无法发挥作用 |
| 7 | **Controller 层无 `@PreAuthorize` 注解**，所有方法级权限校验散落在各 Service / Guard 的手动 `if` 判断中 | 权限逻辑分散，缺乏统一可见的权限地图 |

### P3 — 细节隐患

| # | 问题 | 影响 |
|---|---|---|
| 8 | 前端角色数组来自 `localStorage`，无过期刷新机制；角色被后台变更后，前端缓存不会自动失效 | 被吊销教练资格的用户前端仍显示教练入口，直至重新登录 |
| 9 | `activeRole` 切换（`user` ↔ `coach`）与真实角色集合解耦，未来若后端需区分"以教练身份操作"语义，需重新设计 | 当前可接受，但扩展成本高 |

---

## 五、优化建议

### 短期（本迭代可做）

**① 前端路由增加角色守卫**

在 `router/index.ts` 的 `beforeEach` 中扩展：

```typescript
// 在 router/index.ts 的路由 meta 中增加 requiredRole 字段
meta: { title: '经营看板', requiresAuth: true, requiredRole: 'COACH' }

// 在 beforeEach 中增加
const user = useUserStore();
if (to.meta?.requiredRole) {
  const required = to.meta.requiredRole as string;
  if (!user.roleSet.has(required)) {
    return { path: '/403' }; // 或跳回首页
  }
}
```

涉及文件：[router/index.ts](build/frontend/src/router/index.ts)

**② 后端角色值改用枚举**

在 Java 层定义 `RoleCode` 枚举，`UserRoleBinding.setRole()` 改为接受枚举，消灭裸字符串。同时在数据库的 `role` 列增加 `CHECK (role IN ('USER','COACH','STUDIO_ADMIN','PLATFORM_ADMIN'))` 约束。

**③ 游客限制后端兜底**

对评价、收藏、约练等写接口，若已配置为 `.anyRequest().authenticated()`，则匿名访问已被拦截。需确认以下端点确实在 `authenticated()` 范围内而非误归入 `/public/**`：
- `POST /h5/reviews`（写评价）
- `POST /h5/favorites`（收藏）
- `POST /h5/bookings`（试听预约）

**④ 平台管理员授予接口**

增加一个 `/admin/users/{userId}/roles` 接口（仅 `PLATFORM_ADMIN` 可调），避免直接操作数据库，同时写入操作日志。

### 中期（下一迭代）

**⑤ 对齐 Schema 的 `user_role_binding` 表**

若计划使用 Schema 中的完整结构（含 `effective_from/to`、`granted_by_user_id`），需同步修改：
- `UserRoleBinding.java` 实体增加对应字段
- `UserRoleBindingRepository` 增加按有效期查询方法
- `JwtService` 在签发 Token 时过滤已过期绑定

若不需要时效性管理，则应从 Schema 中移除多余字段，保持一致。

**⑥ Controller 统一 `@PreAuthorize` 注解**

开启 `@EnableMethodSecurity`，在各 Controller 方法上加注解替代手动判断：

```java
@PreAuthorize("hasRole('COACH')")
@PostMapping("/coach/workshops")
public ResponseEntity<?> createWorkshop(...) { ... }
```

**⑦ 前端角色缓存刷新**

在 `refreshProfile()` 时同步刷新角色数组（目前只刷新 nickname/avatar），确保后台角色变更后客户端感知：

```typescript
// stores/user.ts refreshProfile() 中
updateProfile({
  ...
  roles: data.roles  // 需后端在 /profile 响应中返回最新 roles
});
```

---

## 六、文件速查索引

| 文件 | 关键内容 |
|---|---|
| [UserRoleBinding.java](build/backend/src/main/java/com/bitdance/iam/domain/UserRoleBinding.java) | 角色实体，第 22 行角色字符串列 |
| [SecurityConfig.java](build/backend/src/main/java/com/bitdance/iam/security/SecurityConfig.java) | 路由级鉴权规则，第 46–48 行 |
| [JwtAuthFilter.java](build/backend/src/main/java/com/bitdance/iam/jwt/JwtAuthFilter.java) | JWT 解析与 Authority 构建，第 43–49 行 |
| [AuthService.java](build/backend/src/main/java/com/bitdance/iam/service/AuthService.java) | 注册时绑定 USER 角色，第 119 行 |
| [MerchantAccessGuard.java](build/backend/src/main/java/com/bitdance/merchant/service/MerchantAccessGuard.java) | 舞室所有权守卫，第 32–48 行 |
| [bitdance_postgresql_schema.sql](build/backend/bitdance_postgresql_schema.sql) | RBAC 三表定义（第 42–87 行），user_role_binding（第 303–323 行） |
| [stores/user.ts](build/frontend/src/stores/user.ts) | 前端角色计算属性，第 59–62 行 |
| [router/index.ts](build/frontend/src/router/index.ts) | 路由守卫，第 368–376 行；教练/管理员路由第 317–351 行 |
| [MePage.vue](build/frontend/src/pages/user/MePage.vue) | 角色驱动的工作台入口渲染 |
