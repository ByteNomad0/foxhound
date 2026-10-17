# 408 计算机科学学习平台

一个面向 408 计算机学科（数据结构、计算机组成原理、操作系统、计算机网络）考研复习的前后端分离学习平台。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios |
| 后端 | Spring Boot 3.x + JDK 17 + Spring Security + JWT |
| 数据层 | MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| AI 助手 | 兼容 OpenAI 接口的任意大模型（DeepSeek / 通义千问 / OpenAI 等），密钥后期自行配置 |

## 功能清单

- ✅ 用户登录 / 注册 / 个人资料 / 修改密码
- ✅ 课程系统（后台可随时增删课程，前端自动展示）
- ✅ 知识点系统（层级结构、详情讲解、重点 / 难点标注）
- ✅ 题库系统（单选题 / 多选题 / 判断题）
- ✅ 答题系统（提交答案、自动判分、查看解析）
- ✅ 学习记录（答题历史、正确率、学习进度）
- ✅ AI 学习助手（API 密钥后期输入、薄弱点分析、知识点答疑）
- ✅ 后台管理（用户 / 课程 / 知识点 / 题目 / AI 配置 / 数据看板）

## 目录结构

```
kaoyan408/
├── database/          # 建表脚本 + 示例数据
├── backend/           # Spring Boot 后端
├── frontend/          # Vue3 前端
└── README.md
```

## 快速启动

### 1. 初始化数据库

```bash
mysql --default-character-set=utf8mb4 -uroot -p < database/schema.sql
mysql --default-character-set=utf8mb4 -uroot -p < database/data.sql
```

> ⚠️ Windows 下务必加 `--default-character-set=utf8mb4`，否则示例数据中的中文会乱码报错。
>
> 默认数据库连接：`localhost:3306/kaoyan408`，用户 `root`，密码已在 `application.yml` 中配置。
> 如与你的环境不一致，请修改 `backend/src/main/resources/application.yml`。

### 2. 启动后端（需 JDK 17+，已用 JDK 25 实测通过）

本机已备好 Maven（含阿里云镜像 `settings.xml`）：

```bash
cd backend
C:\Users\talos\.maven\apache-maven-3.9.9\bin\mvn spring-boot:run
```

也可以用 IntelliJ IDEA 内置 Maven，或先打包再运行：

```bash
C:\Users\talos\.maven\apache-maven-3.9.9\bin\mvn -DskipTests package
java -jar target/kaoyan408-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8080`。

### 3. 启动前端（需 Node.js 18+）

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理转发 `/api` 到后端。

## 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 普通用户 | student | student123 |

## AI 助手配置

登录管理员账号 → 后台管理 → AI 配置，填入：

- **Base URL**：兼容 OpenAI 的接口地址，例如
  - DeepSeek：`https://api.deepseek.com/v1`
  - 通义千问（DashScope 兼容模式）：`https://dashscope.aliyuncs.com/compatible-mode/v1`
  - OpenAI：`https://api.openai.com/v1`
- **API Key**：你的密钥（保存后数据库中脱敏存储，仅展示后 4 位）
- **模型名**：例如 `deepseek-chat` / `qwen-plus` / `gpt-4o-mini`

配置并启用后，普通用户在「AI 助手」页面即可进行知识点答疑与薄弱点分析。

## 后续添加课程

进入后台管理 → 课程管理 → 新增课程；再在知识点管理中为该课程添加知识点；题库中录入题目并关联课程 / 知识点。前端首页与课程详情会自动展示。

## 常见问题

- **端口占用**：修改 `application.yml` 的 `server.port`，并同步修改 `frontend/vite.config.js` 的代理 target。
- **JDK 版本**：必须使用 JDK 17 或更高版本。
