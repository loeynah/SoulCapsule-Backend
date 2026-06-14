# SoulCapsule · Backend 后端服务

> 心灵胶囊 RESTful API 服务 — 用户认证、心情 CRUD、AI 聊天、解压锦囊、图片上传。

Android 客户端文档请参阅：[SoulCapsule/README.md](../SoulCapsule/README.md)

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库设计](#数据库设计)
- [API 接口文档](#api-接口文档)
- [配置说明](#配置说明)
- [架构说明](#架构说明)
- [常见问题](#常见问题)

---

## 项目简介

**SoulCapsule Backend** 是基于 Spring Boot 3 的情绪树洞后端，为 Android 客户端提供：

- 用户注册 / 登录（BCrypt）
- 心情记录增删改查与模糊搜索
- 智谱 AI 聊天（人设「小旅」）
- 发布心情时自动生成 AI 治愈回复
- 基于用户近期数据的解压小锦囊
- 图片本地存储与静态资源访问

默认端口：**8080**

---

## 功能特性

### 用户模块 (`UserController`)
- 注册：用户名唯一性校验 + BCrypt 加密
- 登录：密码校验，返回 `userId` 与 `username`

### 心情模块 (`MoodController`)
- 新增：映射 moodType → score，写入 tags / imageUrl，**AI 生成 aiFeedback**
- 更新：按 id 更新 score、emotions、content、tags、imageUrl
- 删除：按 id 删除
- 列表：按 userId 倒序，可选 keyword 模糊搜索 content

### AI 模块
- **ChatService**：glm-4-flash，读取最近 6 条聊天上下文
- **AiService.generateMoodFeedback**：发布心情时 15 字内治愈回复
- **AiService.getDecompressionTips**：读取最近 5 条心情 + 10 条聊天，返回 2~3 条 JSON 建议

### 文件模块 (`FileUploadController`)
- UUID 文件名存于 `uploads/`
- 静态映射 `/uploads/**`
- 单文件最大 10MB，支持 jpg/png/gif/webp

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 17+ |
| 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8 |
| AI | 智谱 Open API（glm-4-flash） |
| 安全 | spring-security-crypto（BCrypt） |
| 工具 | Lombok、Hutool 5.8 |

---

## 项目结构

```
SoulCapsule_Backend/
├── pom.xml
├── uploads/                          # 运行时自动创建，图片存储目录
└── src/main/
    ├── java/com/finalwork/soulcapsule/
    │   ├── SoulCapsuleApplication.java
    │   ├── controller/
    │   │   ├── UserController.java       # /api/user
    │   │   ├── MoodController.java       # /api/mood
    │   │   ├── ChatController.java       # /api/chat
    │   │   ├── AiController.java         # /api/ai
    │   │   └── FileUploadController.java # /api/upload
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── MoodService.java
    │   │   ├── ChatService.java
    │   │   ├── ChatMessageService.java
    │   │   ├── AiService.java
    │   │   └── FileStorageService.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── MoodRecord.java
    │   │   └── ChatMessage.java
    │   ├── mapper/
    │   │   ├── UserMapper.java
    │   │   ├── MoodRecordMapper.java
    │   │   └── ChatMessageMapper.java
    │   ├── dto/
    │   │   ├── UserRequest.java
    │   │   ├── LoginResponse.java
    │   │   ├── MoodRequest.java
    │   │   ├── MoodResponse.java
    │   │   ├── ChatRequest.java
    │   │   └── ChatResponse.java
    │   ├── config/
    │   │   ├── WebConfig.java            # 静态资源映射
    │   │   ├── PasswordEncoderConfig.java
    │   │   └── ZhipuProperties.java
    │   ├── common/
    │   │   └── ApiResult.java            # 统一响应 {code, message, data}
    │   └── exception/
    │       └── GlobalExceptionHandler.java
    └── resources/
        └── application.yml
```

---

## 环境要求

| 工具 | 版本 |
|------|------|
| JDK | **17 或以上**（推荐 21） |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| 智谱 AI | 需申请 API Key |

---

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS soulcapsule_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE soulcapsule_db;

CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(64)  NOT NULL,
  `password`    VARCHAR(128) NOT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `mood_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `score`       INT          DEFAULT NULL COMMENT '1~5',
  `tags`        VARCHAR(512) DEFAULT NULL COMMENT '逗号分隔标签',
  `emotions`    VARCHAR(32)  DEFAULT NULL,
  `content`     TEXT         DEFAULT NULL,
  `image_url`   VARCHAR(512) DEFAULT NULL,
  `ai_feedback` VARCHAR(256) DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `role`        VARCHAR(16)  NOT NULL COMMENT 'user / assistant',
  `content`     TEXT         NOT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/soulcapsule_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

zhipu:
  api:
    key: "your_zhipu_api_key"
```

### 3. 启动服务

```bash
cd SoulCapsule_Backend
mvn spring-boot:run
```

Windows PowerShell（指定 JDK 21 示例）：

```powershell
$env:JAVA_HOME="E:\JDK21\jdk-21.0.10"
mvn spring-boot:run
```

启动成功后访问：**http://localhost:8080**

### 4. 接口冒烟测试

```bash
# 注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"test\",\"password\":\"123456\"}"

# 登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"test\",\"password\":\"123456\"}"
```

---

## 数据库设计

### ER 关系

```
user (1) ──────< (N) mood_record
user (1) ──────< (N) chat_message
```

### moodType → score 映射

| emotions | score |
|----------|-------|
| 很好 | 5 |
| 好 | 4 |
| 一般 | 3 |
| 不好 | 2 |
| 很不好 | 1 |

### MoodRecord 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 所属用户 |
| score | INT | 心情分数 1~5 |
| tags | VARCHAR | 归因/情绪标签，逗号分隔 |
| emotions | VARCHAR | 心情类型文本 |
| content | TEXT | 日记正文 |
| image_url | VARCHAR | 配图 URL |
| ai_feedback | VARCHAR | AI 治愈回复 |
| create_time | DATETIME | 创建时间 |

---

## API 接口文档

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

失败：`code = 500`，`message` 为错误描述。

---

### 用户 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 注册 |
| POST | `/api/user/login` | 登录 |

**注册 / 登录请求体：**

```json
{ "username": "demo", "password": "123456" }
```

**登录响应 data：**

```json
{ "userId": 1, "username": "demo" }
```

---

### 心情 `/api/mood`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/mood/add` | 新增（自动生成 aiFeedback） |
| POST | `/api/mood/update` | 更新（需传 id） |
| DELETE | `/api/mood/delete/{id}` | 删除 |
| GET | `/api/mood/list?userId=1&keyword=` | 列表，keyword 可选 |

**MoodRequest 示例：**

```json
{
  "id": 1,
  "userId": 1,
  "moodType": "很好",
  "content": "今天完成了项目答辩！",
  "tags": "学习,开心,有成就感",
  "imageUrl": "http://localhost:8080/uploads/abc.jpg"
}
```

---

### 聊天 `/api/chat`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat/send` | 发送消息，持久化 user + assistant |

```json
{ "userId": 1, "message": "最近有点焦虑" }
```

**响应 data：**

```json
{ "reply": "愿意和我说说发生了什么吗？" }
```

---

### AI 锦囊 `/api/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/tips?userId=1` | 2~3 条个性化解压建议 |

**响应 data 示例：**

```json
["去楼下走十分钟", "今晚早点休息吧"]
```

无历史或 AI 失败时返回默认：`["去吹吹晚风吧", "喝一杯热牛奶"]`

---

### 文件 `/api/upload`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload` | 上传图片 |

- Content-Type: `multipart/form-data`
- 字段名: `file`
- 返回 data: 完整 URL，如 `http://localhost:8080/uploads/uuid.jpg`

静态访问：`GET /uploads/{filename}`

---

## 配置说明

### application.yml

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `spring.datasource.url` | MySQL 连接 | localhost:3306/soulcapsule_db |
| `spring.datasource.username` | 数据库用户 | root |
| `spring.datasource.password` | 数据库密码 | — |
| `spring.servlet.multipart.max-file-size` | 上传大小上限 | 10MB |
| `zhipu.api.key` | 智谱 API Key | — |

### MyBatis-Plus

- 驼峰映射：`map-underscore-to-camel-case: true`
- 主键策略：自增 `auto`

---

## 架构说明

```
HTTP Request
     ↓
Controller（参数校验、路由）
     ↓
Service（业务逻辑）
     ├── Mapper → MySQL
     ├── ChatService / AiService → 智谱 GLM-4-Flash
     └── FileStorageService → uploads/
     ↓
ApiResult<T> 统一 JSON 响应
```

### AI 调用说明

| 场景 | 服务 | 上下文 |
|------|------|--------|
| 聊天 | ChatService | 最近 6 条 chat_message |
| 心情回复 | AiService.generateMoodFeedback | 当前日记 content |
| 解压锦囊 | AiService.getDecompressionTips | 最近 5 条 mood + 10 条 chat |

---

## 常见问题

**启动报 JDK 版本错误？**  
Spring Boot 3 需要 JDK 17+。设置 `JAVA_HOME` 后重试。

**数据库连接失败？**  
确认 MySQL 已启动、库已创建、用户名密码正确。

**AI 接口 401 / 超时？**  
检查 `zhipu.api.key`；无 Key 时聊天报错，锦囊与心情回复降级为默认文案。

**图片 404？**  
确认 `uploads/` 目录存在；`WebConfig` 已将 `/uploads/**` 映射到本地目录。

**跨域问题？**  
当前为 Android 直连，无浏览器 CORS 限制；若扩展 Web 端需额外配置 CORS。

---

## 许可证

本项目为课程 / 毕业设计用途。如需开源发布，请根据实际情况补充 License 条款。

---

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [智谱 AI](https://open.bigmodel.cn/)

---

<p align="center"><strong>SoulCapsule Backend · 为每一种情绪提供可靠的存储与理解</strong></p>
