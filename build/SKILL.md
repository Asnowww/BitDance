---
name: bitdance-dev
description: BitDance 舞室点评/约练平台的统一开发流程。当用户在本仓库内提出任何与 BitDance 项目相关的开发、调试、需求实现、功能补全、代码评审、测试或部署任务时触发；也在用户提到"开发一个需求点"、"循环开发"、"按规范开发"、"orchestrator"、"派活"、"分配开发"、"按 BitDance 流程"等用语时触发。本 skill 固化了项目技术栈、云数据库信息、参考文档位置、多 Agent 编排式开发军团（orchestrator-worker）协作模式、循环 vs 单点两种开发节奏，以及开发历史记录的撰写规范。
---

# BitDance 开发协作 Skill

## 0. 触发后的第一动作（必读）

每次进入本 skill 时，必须按下列顺序执行，不得跳过：

1. 读取 `开发历史记录.md` 的"当前上下文快照"和最近 3 条开发日志，建立项目世界模型。如果文件不存在，按本文档第 8 节的模板创建。
2. 读取 `BitDance_architecture_mvp.md`、`BitDance_后端代码规范文档.md`、`bitdance_postgresql_schema.sql`（如存在），作为架构、编码、数据约束。
3. 判断用户意图：
   - 若用户给出**单个功能点**（"实现登录页"、"加一个评论接口"），进入**单点模式**。
   - 若用户明确说"循环开发"、"持续推进"、"跑完所有需求"、"自动开发"，进入**循环模式**。
   - 若意图不明，主动确认一次再开工。
4. 进入第 5 节的 orchestrator-worker 多 agent 流程。

## 1. 项目定位

BitDance 是面向舞蹈学习者的垂直平台，围绕「找舞室 → 选课程 → 看真实评价 → 约搭子 → 记录成长」构建闭环。它不是泛社区也不是普通点评工具，而是把分散在地图、点评、短视频、私聊里的舞蹈学习路径整合成一条连续体验。

MVP 优先实现：附近舞室搜索、舞种与课程筛选、结构化评价（老师耐心 / 零基础友好 / 节奏 / 强度 / 环境）、约搭子、成长打卡。Workshop、商家端、风控折叠为二期。

## 2. 技术栈（已定）

### 前端
- Vue 3 + Vite 5 + TypeScript
- Pinia + Vue Router（hash 模式，便于后续打包）
- Vant 4 + 自定义组件，**小红书视觉风格**（主色 `#FF2442`，瀑布流首页、沉浸式详情页、底部 TabBar 中央凸起发布按钮）
- 移动端比例开发：375 设计稿 + `postcss-px-to-viewport`，最大宽度 480px 居中
- axios 封装 + 自实现 mock adapter（开发期 `USE_MOCK = true`，无需后端即可联通）
- 先纯 H5；后续用 Capacitor 打包 APK，路由保持 hash 模式即可
- 前端代码位于 `frontend/`

### 后端
- Spring Boot 3.x + **Java 21**
- 数据库 PostgreSQL（云数据库，见第 3 节）
- 编码、分层、命名以 `BitDance_后端代码规范文档.md` 为准
- 表结构以 `bitdance_postgresql_schema.sql` 为准（默认 schema：`bitdance`）
- 后端代码位于 `backend/`（暂未创建）

### 跨端
- 接口契约：RESTful，统一返回 `{ code, message, data }`，code 0 / 200 表示成功
- 鉴权：Bearer token（前端已写入 axios 拦截器）
- 联调切换：前端 `src/utils/request.ts` 把 `USE_MOCK` 改为 `false`，并在 `.env.development` 配 `VITE_API_BASE`

## 3. 云数据库

| 项 | 值 |
| --- | --- |
| 类型 | PostgreSQL |
| 主机 | `98.142.241.155` |
| 端口 | `5432` |
| 数据库 | `bitdance` |
| 用户名 | `BitDance` |
| 密码 | `shixun123` |
| 默认 schema | `bitdance` |

JDBC URL：`jdbc:postgresql://98.142.241.155:5432/bitdance?currentSchema=bitdance`

注意：密码以明文写在文档里仅因为是实训项目；切勿把这套凭证用于公网暴露的服务，也不要 push 到公开仓库。生产部署时换走环境变量。

## 4. 参考文档（位于仓库内）

- `BitDance_architecture_mvp.md` —— MVP 架构、模块边界、领域模型
- `BitDance_后端代码规范文档.md` —— 包结构、命名、异常、日志、事务、Controller/Service/Mapper 分层
- `bitdance_postgresql_schema.sql` —— 表结构与初始数据
- `第五组开题立项书-舞蹈学习与约练平台.docx` / `开题立项书-BitDance.pdf` —— 产品需求与功能边界
- `frontend/README.md` —— 前端启动与切真后端步骤

允许在开发中发现不合理之处时**修改这些文档**，但必须：在被改文档的开头加"修订记录"小节，写明修改日期、修改人、修改条目、修改原因。同时把这条改动记入 `开发历史记录.md` 的"决策记录"区块。

## 5. Orchestrator-Worker 多 Agent 开发军团

### 5.1 总体形态

中心化 orchestrator-worker 模式。所有 agent 共享同一份代码仓库与文件系统。Orchestrator 是唯一持有全局状态的 agent，其他 agent 各司其职，完成后汇报给 orchestrator。

```
                ┌──────────────────┐
                │   Orchestrator   │  全局状态、拆解、调度、验收
                └─┬───────────┬────┘
        派活      │           │     汇报
   ┌──────────┬──┘           └──┬──────────┬──────────┐
   ▼          ▼                  ▼          ▼          ▼
Architect  Developer-FE  Developer-BE   Reviewer    Tester
 (契约)     (前端)        (后端)         (评审)     (测试)
```

### 5.2 各 Agent 职责

**Orchestrator Agent**
- 读取 `开发历史记录.md` + 需求文档，把用户需求拆成可独立验收的功能点列表
- 为每个功能点维护一份"工单"：`{ id, title, 描述, 验收标准, 依赖, 当前状态, 负责 agent, 关联文件 }`
- 派活、跟踪进度、整合产出、决定下一步
- 对每个功能点最终拍板"通过"或"打回"
- 唯一可写 `开发历史记录.md` 的 agent

**Architect Agent**
- 在每个功能点开发前先输出：技术方案、模块划分、API 契约（路径 / 方法 / 请求 / 响应）、数据模型变更（表结构 / 索引 / 迁移 SQL）、前后端字段约定
- 输出形式必须能直接被 Developer 当作输入约束消费
- 不写实现代码，只写设计文档片段（追加到 `开发历史记录.md` 的接口与数据契约区块）

**Developer Agent**（可分裂为 Frontend / Backend 两个实例）
- Frontend Developer：在 `frontend/` 内按 Vue3 + Vite + TS + Vant + 小红书风格规范实现页面与接口对接，复用已有 `components/`
- Backend Developer：在 `backend/` 内按 `BitDance_后端代码规范文档.md` 实现 Controller / Service / Repository，按 schema 写实体与 SQL
- 两个实例共享提示词模板但身份不同，必须严格遵循 Architect 的契约
- 写完后自测能编译、能跑、关键路径手工验证一次，再交给 Reviewer

**Reviewer Agent**
- 静态检查：编译错误、TS 类型错误、空指针风险、未处理异常、SQL 注入、N+1 查询
- 风格检查：是否符合后端代码规范文档与前端项目既有风格（命名、分层、注释、组件粒度）
- 设计检查：是否偏离 Architect 给的契约，是否引入了禁区清单里的反模式
- 输出形式：分级问题列表（Blocker / Major / Minor），Blocker 与 Major 必须 Developer 修完后再过一次 Reviewer

**Tester Agent**
- 后端：JUnit 5 单元测试 + MockMvc / RestAssured 接口测试，覆盖正常路径 + 至少 2 个异常路径
- 前端：关键交互手工脚本（步骤 + 预期），有条件加 Vitest 组件测试
- 输出形式：测试用例 + 执行结果。失败时给 Developer 复现步骤
- 不通过则打回 Developer

### 5.3 流程闭环

```
Orchestrator 拆解
   → Architect 出契约
   → Developer-FE / Developer-BE 并行实现
   → Reviewer 审查（不通过则回 Developer）
   → Tester 测试（不通过则回 Developer）
   → Orchestrator 验收（不通过则定位环节回退）
   → 通过 → 写入开发历史记录.md → 进入下一个功能点（循环模式）或终止（单点模式）
```

只有当 Reviewer 全部通过且 Tester 全部通过且 Orchestrator 验收通过时，才能认为该需求点开发完毕。任何一环未通过，Orchestrator 指定回退到对应环节。

### 5.4 单点模式 vs 循环模式

**单点模式（默认）**
用户只指定一个功能点。orchestrator 按 5.3 流程跑完该功能点，写完日志后**停止**，向用户汇报。

**循环模式**
仅当用户明确表达"循环开发"、"自动跑完"、"持续开发到完成为止"时进入。流程：
```
while 还有未完成的需求点:
    取下一个优先级最高的功能点
    走 5.3 完整闭环
    通过后 → 更新开发历史记录.md → 进入下一轮
退出条件：
  - 所有已知功能点完成
  - 出现需要用户决策的阻塞（比如未定义的产品规则、缺少凭证）
  - 用户中断
```
进入循环模式时 Orchestrator 必须先输出本轮要开发的功能点清单（带优先级），用户默认确认后才开始跑。

### 5.5 Agent 间共享约束

- 所有 agent 共享同一份代码仓库；任何文件改动直接落盘。
- Architect 的契约一旦写入 `开发历史记录.md` 的接口与数据契约区块，即成为后续 agent 的硬约束。
- Developer 之间发生冲突（如前端要的字段后端没出）由 Orchestrator 协调，不私下改契约。
- 任何 agent 发现禁区清单里的风险都要立即上报 Orchestrator。

## 6. 提示词模板（供 Orchestrator 调用 Task 工具时复用）

调用任何 worker agent 时，prompt 必须包含：

1. 你的身份（Architect / Developer-FE / Developer-BE / Reviewer / Tester）和职责边界
2. 当前功能点工单（id、标题、验收标准）
3. 上游产出（如 Developer 拿到 Architect 的契约）
4. 仓库内必读文件列表（架构文档、规范文档、schema、开发历史记录）
5. 输出格式要求（结构化、能被下游或 Orchestrator 直接消费）
6. 完成后必须返回的状态字段：`status (done|blocked|need-info)`、`产出文件列表`、`遗留问题`

## 7. 约定与规范汇总

- 提交信息：`type(scope): subject`，type ∈ feat/fix/refactor/docs/test/chore
- 分支：`feature/<功能点 id>`、`fix/<bug>`
- 前端命名：组件 PascalCase，文件 PascalCase.vue，store / composable camelCase
- 后端命名：包小写、类 PascalCase、方法 camelCase、常量大写下划线
- 不允许 push 数据库密码到公网仓库；本仓库为私有实训仓库故第 3 节明文记录
- 任何破坏接口契约的改动必须在 `开发历史记录.md` 的决策记录里登记

## 8. 开发历史记录撰写规范

每次开发开始前先读 `开发历史记录.md` 的"当前上下文快照"与最近若干条开发日志；开发结束前必须补完本次日志条目。文档与代码同入版本控制，路径为仓库根目录的 `开发历史记录.md`，过长时归档到 `docs/history/`。

文档区块顺序：项目全局坐标系、当前上下文快照、约定与规范、禁区与陷阱清单、环境与依赖、接口与数据契约、开发日志（时间倒序）、决策记录、变更日志。

写作风格：精确细致，段落式表述而非碎片化要点，不使用引号，不使用奇怪的比喻；同模块下的功能点用分段叙述。涉及目录路径时不暴露私有路径。

每条开发日志包含：日期与开发者标识；目标与背景动机；改动文件范围与核心逻辑变化；遇到的关键问题与解决路径；为什么选择该方案而非备选；遗留待办；关联 commit / PR / issue。动机与方案选择是最容易遗漏但最有价值的内容，必须写。

### 初始化模板

若仓库内尚不存在 `开发历史记录.md`，按下列模板创建：

```markdown
# 开发历史记录

## 项目概览

- 项目名称：BitDance
- 一句话定位：面向舞蹈学习者的找舞室、选课程、约搭子、记录成长一站式平台
- 当前版本/里程碑：MVP
- 主要负责人：第五组

### 技术栈

- 前端：Vue 3 + Vite + TypeScript + Pinia + Vant，小红书视觉风格，移动端 H5
- 后端：Spring Boot 3 + Java 21
- 数据库：PostgreSQL（云库 98.142.241.155:5432，schema bitdance）
- 关键依赖：axios、postcss-px-to-viewport-8-plugin、Capacitor（后续打包 APK）

### 架构与目录

frontend/ 前端工程，backend/ 后端工程，docs/ 设计与历史归档。详见 BitDance_architecture_mvp.md。

## 当前上下文快照

最后更新：YYYY-MM-DD

- 项目阶段：
- 当前焦点：
- 最近成功运行状态：
- 未完成工作：

## 约定与规范

见 SKILL bitdance-dev 第 7 节。

## 禁区与陷阱

- 不要修改 bitdance_postgresql_schema.sql 中已使用表的字段类型，只允许新增字段或新增表
- 前端路由必须保持 hash 模式，否则后续 APK 打包会失效
- USE_MOCK 切换前必须确认所有接口在真后端已实现，否则页面会大面积报错
- 云数据库凭证不得 push 到公开仓库

## 环境与依赖

### 本地搭建

前端：cd frontend && npm i && npm run dev
后端：见 backend/README.md（待补）

### 环境变量

| 变量名 | 用途 | 示例值 |
| --- | --- | --- |
| VITE_API_BASE | 前端真后端地址 | http://localhost:8080/api |
| SPRING_DATASOURCE_URL | 后端数据库连接 | jdbc:postgresql://98.142.241.155:5432/bitdance?currentSchema=bitdance |
| SPRING_DATASOURCE_USERNAME | 数据库用户 | BitDance |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | （占位，见 SKILL 第 3 节） |

### 数据初始化

执行 bitdance_postgresql_schema.sql 即可。

### 构建与部署

待补。

## 接口与数据契约

### 主要 API

由 Architect Agent 在每个功能点开发前补全。

### 数据库表结构

以 bitdance_postgresql_schema.sql 为准。

### 模块间通信

前后端通过 RESTful + Bearer token，统一返回 { code, message, data }。

## 开发日志

### YYYY-MM-DD ｜ 开发者

- 目标与背景：
- 改动范围：
- 关键问题与解决：
- 方案选择理由：
- 待办与遗留：
- 关联：

## 决策记录

### YYYY-MM-DD：决策标题

- 决策内容：
- 上下文与权衡：
- 影响范围：
- 回滚条件：

## 变更日志

### v0.1.0 ｜ YYYY-MM-DD

- 前端 H5 脚手架与小红书风格 MVP 页面
```

## 9. 本 Skill 的成功判据

每次开发结束时，必须满足：

1. 代码已落盘，能编译、能跑
2. Reviewer 与 Tester 各自通过
3. Orchestrator 已在 `开发历史记录.md` 追加本轮日志，包含动机与方案选择
4. 若改动了契约或参考文档，决策记录与文档头部修订记录同步
5. 单点模式：返回结论；循环模式：进入下一轮或在阻塞处停下并明确告诉用户为何停止
