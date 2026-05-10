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

## 切换真后端

1. 修改 `.env.development`：`VITE_USE_MOCK=false`，并把 `VITE_API_BASE` 指向后端实际地址，例如 `http://localhost:8080/api`。
2. 确认所有用到的接口在后端已实现，否则页面会大面积报错。
3. 重启 `npm run dev`。

## 路由模式

必须保持 hash 模式，否则后续 Capacitor 打包 APK 会失效。

## 目录约定

- `src/pages` 路由级页面，按业务模块分子目录（home / studio / review / practice / growth / community / workshop / coach / user）
- `src/components` 通用组件
- `src/stores` Pinia stores
- `src/api` 按业务域拆分的接口模块
- `src/mock` mock adapter 与各模块 mock 数据
- `src/utils` request 封装、storage、格式化等
- `src/styles` 主题变量与全局样式
