# AI 智能题库系统

> 基于 Spring Boot 3 + MyBatis-Plus + Vue 3 的智能题库系统  
> 课程：软件生产实习 · 2025-2026 学年第二学期

## 一、项目简介

AI 智能题库系统支持教师录入题目、组卷、发布考试；学生在线答题、自动判分、AI 解析、AI 推同类题。系统涵盖完整业务流程：题目录入 → 试卷生成 → 考试发布 → 学生作答 → 自动判分 → 错题归集 → AI 推题。

### 业务工作流

```
[教师]
录入题目 → 知识点标签 → 创建试卷（手动/随机）→ 发布考试
   ↓
[学生]
参加考试 → 倒计时作答 → 断点续答 → 提交试卷
   ↓
[系统]
客观题自动判分 → 错题自动归集 → AI 推荐同类题
   ↓
[学生]
查看错题本 → 标记掌握 → 复习闭环
   ↓
[系统]
统计分析：成绩趋势、错题分布、知识点掌握度
```

**状态流转**：
- 题目：草稿 → 启用 → 归档
- 考试：DRAFT → PUBLISHED → ONGOING → FINISHED → ARCHIVED
- 答题记录：IN_PROGRESS → SUBMITTED → GRADED
- 错题：未掌握 → 复习中 → 已掌握

## 二、技术栈

### 后端
- **Java 17** + **Spring Boot 3.3.4**
- **MyBatis-Plus 3.5.7**（ORM + 分页 + 自动填充）
- **MySQL 8.0**（数据库）
- **Spring Security + JWT**（鉴权，三角色 RBAC）
- **Knife4j 4.5**（OpenAPI 3 文档）
- **EasyExcel 4.0**（题目 Excel 导入导出）
- **Hutool 5.8**（工具库）
- **通义千问 DashScope HTTP API**（AI 调用：生成解析、推同类题）

### 前端
- **Vue 3** + **Vite 5** + **Vue Router 4** + **Pinia 2**
- **Element Plus 2.8**（UI）
- **Axios**（HTTP 拦截器 + JWT）
- **ECharts 5**（统计图表）

## 三、目录结构

```
ai-question-bank/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/aiqb/
│   │   ├── AiQuestionBankApplication.java
│   │   ├── ai/                     # 通义千问服务
│   │   ├── common/                 # Result、全局异常
│   │   ├── config/                 # Security、CORS、Knife4j、MyBatis
│   │   ├── controller/             # Auth/Subject/Question/Paper/Exam/Wrong/Stats/Ai
│   │   ├── dto/                    # 请求 DTO
│   │   ├── entity/                 # 10 张表实体
│   │   ├── exception/
│   │   ├── mapper/                 # MyBatis Mapper
│   │   ├── security/               # JwtUtil、Filter
│   │   ├── service/                # 业务层
│   │   │   └── impl/
│   │   ├── util/
│   │   └── vo/                     # 响应 VO
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── mapper/                 # MyBatis XML
│   │   └── sql/schema.sql          # 建库脚本
│   └── pom.xml
├── frontend/                       # Vue3 前端
│   ├── src/
│   │   ├── api/                    # 5 个 API 模块
│   │   ├── views/                  # 10 个页面
│   │   ├── stores/                 # Pinia
│   │   ├── router/
│   │   ├── styles/
│   │   └── utils/
│   ├── package.json
│   └── vite.config.js
├── docs/                           # 实习报告
└── README.md
```

## 四、本地运行

### 1. 准备 MySQL 8.0
```bash
# 启动 MySQL（管理员模式）
net start MySQL80

# 创建数据库（密码填你自己的）
mysql -u root -p
> CREATE DATABASE ai_question_bank DEFAULT CHARACTER SET utf8mb4;
> exit

# 导入表结构
mysql -u root -p ai_question_bank < backend/src/main/resources/sql/schema.sql
```

### 2. 修改后端配置
编辑 `backend/src/main/resources/application-dev.yml`，把 `DB_PASSWORD` 改成你的 MySQL 密码。

### 3. 配置通义千问 API Key（可选）
- 申请：https://dashscope.aliyun.com/ → 创建 API Key
- 编辑 `backend/src/main/resources/application.yml`，把 `ai.dashscope.api-key` 改成你的 key
- **不配也能跑**，AI 接口会返回模拟回答

### 4. 启动后端
```bash
cd backend
mvn spring-boot:run
```
访问：
- 接口文档：http://localhost:8080/api/doc.html
- Swagger UI：http://localhost:8080/api/swagger-ui.html

### 5. 启动前端
```bash
cd frontend
npm install
npm run dev
```
访问：http://localhost:5173

### 6. 默认账号
| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| teacher1 | admin123 | 教师 |
| student1 | admin123 | 学生 |

## 五、API 接口（22 个）

| 模块 | 接口 |
|---|---|
| **认证** | `POST /api/auth/register` `POST /api/auth/login` `GET /api/auth/me` |
| **科目** | `GET /api/subjects` |
| **题目** | `GET/POST/PUT/DELETE /api/questions[/{id}]` `GET /api/questions/random` `POST /api/questions/import/{subjectId}` `GET /api/questions/template` `GET /api/questions/export` |
| **试卷** | `GET/POST/PUT/DELETE /api/papers[/{id}]` `POST /api/papers/auto-generate` |
| **考试** | `GET /api/exams` `POST /api/exams` `PUT /api/exams` `DELETE /api/exams/{id}` `PUT /api/exams/{id}/publish` `PUT /api/exams/{id}/archive` `GET /api/exams/available` `POST /api/exams/{id}/start` `POST /api/exams/save-answer` `POST /api/exams/submit` `GET /api/exams/my-records` |
| **错题本** | `GET /api/wrong-questions` `PUT /api/wrong-questions/{id}/mastered` `GET /api/wrong-questions/stats` |
| **统计** | `GET /api/stats/personal` |
| **AI** | `POST /api/ai/explain/{questionId}` `GET /api/ai/recommend` |

## 六、数据库表（10 张）

1. `users` — 用户（学生/教师/管理员）
2. `subjects` — 科目
3. `questions` — 题目
4. `papers` — 试卷
5. `paper_questions` — 试卷-题目关联
6. `exams` — 考试
7. `exam_records` — 考试记录
8. `answers` — 答题详情
9. `wrong_questions` — 错题本
10. `ai_logs` — AI 调用日志

## 七、首次使用流程

1. 打开 http://localhost:5173
2. 用 `admin` / `admin123` 登录
3. 进入"题库管理" → 上传 Excel 或新建题目
4. 进入"试卷管理" → 随机组卷
5. 进入"考试中心" → 新建考试（选刚组的卷）→ 发布
6. 退出登录，用 `student1` / `admin123` 登录
7. 进入"考试中心" → 开始考试 → 提交
8. 进入"错题本" → AI 推题 → 标记掌握
9. 进入"统计分析" → 看成绩趋势

## 八、Git 部署

```bash
# 初始化
git init
git add .
git commit -m "feat: 初始版本"
git remote add origin <你的远程仓库>
git push -u origin master
```

## License

MIT - 仅用于课程学习
