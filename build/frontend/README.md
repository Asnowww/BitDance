# BitDance 用户/教练 H5 移动端

Vue 3 + Vite + TypeScript + Pinia + Vue Router (hash) + Vant 4。
小红书视觉风格主色 `#FF2442`，375 设计稿移动端比例，最大宽度 480px 居中。
先以浏览器 H5 形式上线，后续使用 Capacitor 打包为 Android APK。

## 启动

```bash
cd frontend
npm install
npm run dev
```

默认 `VITE_USE_MOCK=true`，无需后端即可联通；接口走前端内置 mock adapter，文件位于 `src/mock/`。

## 地图定位

发布动态的所在位置需要腾讯地图真实 POI 逆地理编码：

```bash
VITE_TENCENT_MAP_KEY=你的腾讯地图前端Key
VITE_TENCENT_MAP_REFERER=BitDance
```

有前端 Key 时，页面会优先用腾讯地图 JSAPI 根据浏览器经纬度反查具体店面/小区/餐厅名称。没有 Key 时，`VITE_USE_MOCK=true` 只能使用本地少量演示 POI，不能覆盖全国真实地点。

## 切换真后端

1. 修改 `.env.development`：`VITE_USE_MOCK=false`，并把 `VITE_API_BASE` 指向后端实际地址，例如 `http://localhost:8080/api`。
2. 确认所有用到的接口在后端已实现，否则页面会大面积报错。
3. 重启 `npm run dev`。

## 评价联调记录（2026-05-21）

负责人：王梓涵。
排期内容：`docs`，补充评价联调记录。

### 涉及前端页面

- `src/pages/publish/PublishReviewPage.vue`：评价发布与编辑入口，覆盖舞室、教练、课程三类评价对象。
- `src/pages/studio/StudioReviewsPage.vue`：评价列表、评分摘要、维度均分和排序切换。
- `src/components/StarRating.vue`：评价分数输入与只读展示。

### 真后端接口核对

| 场景 | 方法与路径 | 鉴权 | 核对重点 |
| --- | --- | --- | --- |
| 发布评价 | `POST /h5/reviews` | Bearer token | `targetType` 仅允许 `studio`、`coach`、`course`；维度分不能为空；返回 `reviewStatus`、`isVerified`、`weightFactor`。 |
| 删除评价 | `DELETE /h5/reviews/{id}` | Bearer token | 只能删除本人评价；成功返回 `{ deleted: true }`。 |
| 评价列表 | `GET /public/reviews` | 无需登录 | 参数包含 `targetType`、`targetId`、`sort`、`page`、`pageSize`；排序覆盖 `latest`、`helpful`、`verified`。 |
| 评价摘要 | `GET /public/reviews/summary` | 无需登录 | 返回 `count`、`verifiedCount`、`weightedAvgScore`、`dimensionAvg`，用于详情页评分摘要。 |

### 前端字段映射

- 舞室维度：`traffic`、`hygiene`、`venue`、`vibe`。
- 教练维度：`patience`、`correction`、`explanation`、`beginnerFriendly`。
- 课程维度：`difficulty`、`rhythm`、`intensity`、`gain`。
- 切真后端时，写接口走 `/h5/reviews`，读接口走 `/public/reviews` 与 `/public/reviews/summary`；不要把公开读接口和登录写接口混成同一个 `/reviews` 路径。

### 验收关注点

1. `VITE_USE_MOCK=false` 后，评价列表可按目标对象拉取并正确展示综合分、评价数量、维度均分。
2. 未登录用户可查看公开评价列表；发布、删除评价时必须携带登录态。
3. 新评价提交后，页面能根据后端返回的 `reviewStatus` 与 `isVerified` 展示验证状态。
4. 低分、重复评价、空维度分、非法 `targetType` 等异常场景，需要展示后端返回的错误提示。
5. 编辑评价入口属于前端保留能力；本轮真后端联调以发布、删除、列表、摘要四类接口为准。

## 路由模式

必须保持 hash 模式，否则后续 Capacitor 打包 APK 会失效。

## 打包 Android APK（Capacitor）

首次：

```bash
cd frontend
npm install                  # 第一次需要安装 @capacitor/cli @capacitor/core @capacitor/android
npm run build                # 产出 dist/
npm run cap:add:android      # 仅首次执行，创建 android/ 平台目录
npm run cap:sync             # 同步 web 资源到 Android 工程
npm run cap:open:android     # 打开 Android Studio 进行签名打包
```

后续每次只需 `npm run cap:sync`。`capacitor.config.ts` 已设置 appId `com.bitdance.app`、appName BitDance、主题色 `#FF2442`、SplashScreen、StatusBar。

注意：

- 路由必须保持 hash 模式
- `androidScheme: 'https'` 表示 Android WebView 通过 https 协议加载本地资源，避免 cookie / WebRTC 等被降级
- 如需后端联调，确保 `.env.production` 的 `VITE_API_BASE` 指向真实可访问的 https 后端

## 目录约定

- `src/pages` 路由级页面，按业务模块分子目录（home / studio / review / practice / growth / community / workshop / coach / user）
- `src/components` 通用组件
- `src/stores` Pinia stores
- `src/api` 按业务域拆分的接口模块
- `src/mock` mock adapter 与各模块 mock 数据
- `src/utils` request 封装、storage、格式化等
- `src/styles` 主题变量与全局样式
