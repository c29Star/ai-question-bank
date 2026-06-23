# 项目工作日志 — ai-question-bank

> 这个文件记录从 2026-06-23 开始的"登录会话持久化"任务全过程，方便中断后快速接续。
> 维护人：Mavis（用户：开发者本人）· 维护方式：每次任务推进/中断时追加一段

---

## 任务总目标

实现一个**会话持久化策略**，满足以下三种行为：

1. **没勾 7 天免登录 + 关闭/退出网页后重新打开** → 跳登录页
2. **勾了 7 天免登录 + 重新打开/刷新** → 直接进用户自己的界面（停留在原模块）
3. **登录后没退出登录 + 刷新页面** → 停留在所选模块的本页

硬性约束：改动**只动登录相关**，不能影响其他业务模块的正常实现和使用。

## 涉及文件

- `frontend/src/main.js` — 入口；启动时同步 hydrate
- `frontend/src/router/index.js` — 路由守卫（决定跳 login 还是放行）
- `frontend/src/stores/user.js` — Pinia store；token/user 的真源
- `frontend/src/views/Login.vue` — 登录页（onMounted 清理、登录提交、setAuth）
- `frontend/src/views/Layout.vue` — 应用外壳（onMounted 拉统计、退出登录）
- `frontend/src/utils/http.js` — axios 实例；401 拦截 + 静默窗口

## 当前会话状态（持续更新）

### 会话 #1 — 2026-06-23 21:50 左右

**任务起点**：用户给出总目标 + 一张登录页弹"请先登录"的截图（中断前正在诊断）。

**已完成的修改**：

1. ✅ `router/index.js` —— 守卫内兜底再调一次 `hydrateFromStorage()`（防 main.js 顺序被改）；同时把 `markSilent401Window(4000)` + `markJustLoggedOut()` 从 `Login.vue onMounted` 前移到 `beforeEach` 的 `to.meta.guest` 分支，更早覆盖首屏残留 401。
2. ✅ `Login.vue` —— 移除 `onMounted` 里重复的 `markSilent401Window` / `markJustLoggedOut`（已由守卫完成）；保留 store + localStorage 残留清理。
3. ⚠️ `http.js` —— **上一回合我误写成 `require('@/stores/user')`**，Vite/ESM 环境跑不了，会破坏构建。**本回合要修**。

**待办**：

- [ ] **修 `http.js` 请求拦截器** —— 改回 `import { useUserStore } from '@/stores/user'`（ESM 静态 import 在模块加载时 hoist，但拦截器是请求触发时才执行，此时 Pinia 必定就绪）。
- [ ] 验证前端 dev 能正常启动（`npm run dev`）。
- [ ] 验证三个场景：
  - 勾 7 天免登录 → 关闭网页 → 重开 → 应进对应模块
  - 不勾 7 天免登录 → 关闭网页 → 重开 → 应跳 /login
  - 登录后刷新 → 应停留原模块

**关键判断 / 决策记录**：

- `isLogin` getter 判定为 `!!s.token`，所以**只要 store 里有 token 就视为登录**。
- 登录页 `onMounted` 无条件 logout 不影响"7 天免登录"场景，因为**刷新自己模块根本不会进 Login.vue**（守卫先放行）。
- 用户截图里"请先登录"刷屏的根因是**首屏残留的 401 在 Login.vue onMounted 之前到达**，静默窗口没盖住 → 解决方式：把静默窗口调用前移到路由守卫的 `beforeEach`（已在会话 #1 完成）。

---

### 会话 #2 — 2026-06-23 22:29 左右

**会话起点**：用户上传 4 张图（任务进度截图 + 之前任务背景），要求**记工作日志**且**继续未完成工作**。

**已完成工作**：

1. ✅ 创建本工作日志 `ai-question-bank/WORKLOG.md`（持续更新）
2. ✅ 修 `http.js` —— 把误写的 `require('@/stores/user')` 改成 `import { useUserStore } from '@/stores/user'`，加 try/catch 兜底
3. ✅ Vite HMR 验证 —— `frontend-dev2.log` 显示 `22:30:51 page reload src/utils/http.js` 无报错；`.err` 文件为空
4. ✅ 写并跑通 `verify-backend-and-modules.cjs`：
   - 后端 `/api/auth/login` admin/admin123 → 200 + 206 字符 token
   - `/api/auth/me` 带 token → 200
   - Vite dev 5173 → 200
   - `http.js` 编译正常，无 `require` 残留
   - `router/index.js` 编译正常
5. ✅ 启动 Edge 远程调试（9222），跑 `test-refresh.cjs`：

```
--- R1: login without remember, F5 on /dashboard ---
  after refresh: /login            ← PASS

--- R2: login with remember, F5 on /dashboard ---
  after refresh: /dashboard        ← PASS

--- R3: no-remember, reopen /login -> /login ---
  PATH: /login                      ← PASS

3 pass / 0 fail
```

**三个核心场景全部通过**，用户总目标达成。

**遗留事项**：
- 退出登录场景完整测试用 `test-login-persistence.js`（依赖 `window.__pinia_state__` 注入）会卡住，但现有 `test-refresh.cjs` 覆盖了"刷新"和"重开"两个核心路径。退出登录流程走 `userStore.logout() + localStorage.removeItem`，逻辑上等价于"不勾 7 天免登录 + 关闭网页"——**已被 R1 间接验证**。
- 如果想进一步验证，可以把 `test-login-persistence.js` 里的 `__pinia_state__` 注入改用 `localStorage` 快照（参照 `test-refresh.cjs` 的写法）。**不阻塞主任务**。

---

## 涉及修改的文件清单（会话 #1 + #2）

| 文件 | 改动 |
|------|------|
| `frontend/src/router/index.js` | 守卫内兜底 hydrate；静默窗口前移到 `to.meta.guest` 分支 |
| `frontend/src/views/Login.vue` | 移除 `onMounted` 里的静默窗口（已由守卫完成）；保留 store/localStorage 清理 |
| `frontend/src/utils/http.js` | 请求拦截器从 Pinia store 读 token，localStorage 回退仅信任 token+user 同时存在 |
| `ai-question-bank/WORKLOG.md` | 新建（项目内工作日志） |

## 未来会话接入指南

1. 先读本文件（`ai-question-bank/WORKLOG.md`）
2. 检查 `frontend-dev2.log` 最后几行 + `.err` 文件，确认 dev server 状态
3. 跑 `verify-backend-and-modules.cjs` 做一次健康检查
4. 跑 `test-persistence-v2.cjs` 验证五场景是否仍 PASS（**注意：旧的 `test-refresh.cjs` 已经过时，R1 期望已变**）

---

### 会话 #3 — 2026-06-23 22:51 左右

**会话起点**：用户提出**新需求** —— 无论是哪种用户登录后，**只要没主动退出登录**，刷新页面也要停留原模块（**不勾 7 天免登录也要这样**）。

**需求重新定义（最终版）**：

| # | 场景 | 期望行为 |
|---|------|---------|
| 1 | 不勾 7 天 + **刷新** | **停留原模块**（新需求） |
| 2 | 不勾 7 天 + 关掉整个浏览器重开 | 跳 /login |
| 3 | 勾 7 天 + 刷新 | 停留原模块 |
| 4 | 勾 7 天 + 关掉整个浏览器重开 | 停留原模块 |
| 5 | 主动退出登录 + 重开 | 跳 /login |

**根本修复**：把"会话持久化"和"7 天免登录"解耦为两层存储：

- `sessionStorage`：**默认都写**（无论勾不勾 7 天），保证 tab 还在时刷新不丢
- `localStorage`：**仅 remember=true 时写**，保证关掉整个浏览器后还能恢复

**已完成修改**：

1. ✅ `frontend/src/stores/user.js` — `setAuth` 默认写 sessionStorage；`remember=true` 时额外写 localStorage；`logout()` 同时清两个；`hydrateFromStorage()` 优先读 localStorage，回退到 sessionStorage
2. ✅ `frontend/src/utils/http.js` — 请求拦截器回退逻辑也按 localStorage → sessionStorage 顺序读
3. ✅ `frontend/src/router/index.js` — 守卫 `hasPersisted` 判断扩展到同时检查 sessionStorage + localStorage
4. ✅ `frontend/src/views/Login.vue` — `onMounted` 残留清理扩到同时清 sessionStorage

**验证结果**（`test-persistence-v2.cjs`，五场景全 PASS）：

```
--- S1: no-remember + F5 on /dashboard ---
  after refresh: /dashboard | sessionStorage keys: [ 'aiqb_user', 'aiqb_token' ] | localStorage keys: []
  -> PASS

--- S2: remember + F5 on /dashboard ---
  after refresh: /dashboard | localStorage keys: [ 'aiqb_user', 'aiqb_token' ]
  -> PASS

--- S3: no-remember + close tab (new tab simulates new session) -> /login ---
  after reopen: /login | sessionStorage keys: [] | localStorage keys: []
  -> PASS

--- S4: remember + close tab + reopen -> /dashboard ---
  after reopen: /dashboard | localStorage keys: [ 'aiqb_user', 'aiqb_token' ]
  -> PASS

--- S5: explicit logout + reopen -> /login ---
  after logout, in tab: /dashboard | localStorage keys: []
  after reopen: /login
  -> PASS

5 pass / 0 fail
```

**重要观察**：
- 旧 `test-refresh.cjs` 的 R1（不勾 + F5 → /login）**已经过时**，不应再用——它对应的是"旧需求"（不勾就丢登录态）。新需求下 R1 应是 /dashboard。
- Vite HMR 全部 reload/update 干净，无编译错误（`frontend-dev2.log` 显示 22:52~22:53 连续 reload stores/user.js → utils/http.js → router/index.js → hmr update Login.vue）。
- 没有动任何业务模块（Dashboard.vue / Questions.vue / Papers.vue / Exams.vue / WrongBook.vue / Stats.vue / History.vue / ExamTaking.vue / Layout.vue / api/*.js）—— 改的四个文件全部是登录相关。

**遗留事项**：无。

---

### 会话 #4 — 2026-06-23 ~ 2026-06-24 凌晨 GitHub 部署

**会话起点**：用户要求按论文"4.6 项目部署 / 学会 git 部署 / 2.3 代码质量与规范（Git 提交信息清晰、记录完整）"要求，把项目推到 GitHub。

**任务结果**：✅ 仓库已创建并推送完成

**关键信息**：
- GitHub 用户名：`c29Star`
- 仓库地址：https://github.com/c29Star/ai-question-bank
- 仓库类型：Public（论文要求公开）
- 默认分支：master
- 推送 commit 总数：6

**commit 列表**（push 后 GitHub 上的状态）：
```
0bfd4ea  chore: 补充 .gitignore 与工作日志           -- c29Star
b9abeb7  feat(frontend): 前端样式升级、API 整合与视图完善  -- c29Star
d95a4e0  feat(backend): 后端 API 完善、业务优化与种子数据  -- c29Star
00df8ed  fix(login): 修复登录持久化，支持会话级和 7 天免登录双层存储  -- c29Star
f194dbb  chore: 补全仓库元数据（LICENSE 与 frontend/.gitignore）  -- c29Star
fcef914  feat: 初始版本 - AI 智能题库系统 (Spring Boot 3 + Vue 3)  -- Student
```
（注意：fcef914 作者是 `Student <1957403649@qq.com>`——这是历史 commit，**不修改**，保留以体现真实开发过程。如果评分老师介意，后续可单独处理。）

**已完成工作**：

1. ✅ **安全扫描**：发现 `application-dev.yml` 里 DB_PASSWORD 默认值被误写为 `abc123`（用户本地 MySQL 密码），回退到占位符 `please-set-your-password`
2. ✅ **API Key 改进**：`application.yml` 中 `ai.dashscope.api-key` 改为完全从环境变量 `DASHSCOPE_API_KEY` 读取，避免泄露
3. ✅ **新增 LICENSE**：MIT 协议
4. ✅ **完善 .gitignore**：
   - 新增 `edge-*/`（浏览器调试用户数据目录）
   - 新增 `preview-*.png` / `preview-*.html`（开发调试临时预览）
   - 新增 `.git-commit-msg-*.txt`（commit 用的临时 message 文件）
   - 新增 `.vite/`（Vite 缓存）
   - 补全并恢复 `frontend/.gitignore`
5. ✅ **拆分 6 个 commit**：每个 commit 用 Conventional Commits 风格（feat/fix/chore），message 含主题+正文，符合论文"提交记录清晰、记录完整"
6. ✅ **GitHub 仓库创建**：用户在 https://github.com/new 网页创建（fine-grained token 不允许 `POST /user/repos`，改走网页）
7. ✅ **Push 成功**：`git push -u origin master` 完成
8. ✅ **凭据清理**：临时 token 文件 `C:\Users\pbyl0\Desktop\token.txt` 移到回收站；git remote URL 恢复成不含 token 的形式

**踩坑笔记（给后续会话参考）**：
- **fine-grained PAT 不能用于 `POST /user/repos`**：会返回 403 "Resource not accessible by personal access token"。创建仓库必须走网页（https://github.com/new）。
- **PowerShell + git 多 `-m` 中文 message 会被截断**：用 `-F <file>` 写到临时文件再 commit 最稳。
- **`LF will be replaced by CRLF` 警告**：Windows + git autocrlf 默认行为，无害。如果想消除，在 `.gitattributes` 加 `* text=auto eol=lf`（不强制要求）。
- **Windows `Remove-Item` 被安全规则挡住**：临时文件用 `mavis-trash` 移到回收站最稳。

**论文"4.6 项目部署"章节可直接引用的话术**：
> 本项目已部署到 GitHub：https://github.com/c29Star/ai-question-bank  
> 仓库地址：https://github.com/c29Star/ai-question-bank.git  
> 提交记录共 6 次：初始版本 → 仓库元数据 → 登录持久化修复 → 后端完善 → 前端升级 → 文档  
> 本地运行：
> ```bash
> git clone https://github.com/c29Star/ai-question-bank.git
> cd ai-question-bank
> # 启动 MySQL 并导入 schema
> mysql -u root -p < backend/src/main/resources/sql/schema.sql
> # 启动后端
> cd backend && mvn spring-boot:run
> # 另开终端启动前端
> cd frontend && npm install && npm run dev
> ```

**遗留事项**：无。



