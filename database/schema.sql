-- 408 计算机科学学习平台 数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS kaoyan408 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE kaoyan408;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ----------------------------
-- 课程表
-- ----------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    code        VARCHAR(50)  DEFAULT NULL COMMENT '课程代码',
    subject     VARCHAR(50)  DEFAULT NULL COMMENT '所属学科分类',
    description TEXT         COMMENT '课程描述',
    cover       VARCHAR(255) DEFAULT NULL COMMENT '封面图地址',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='课程表';

-- ----------------------------
-- 知识点表（parent_id = 0 表示顶层章节，可多级）
-- ----------------------------
DROP TABLE IF EXISTS knowledge_point;
CREATE TABLE knowledge_point (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id        BIGINT       NOT NULL COMMENT '所属课程',
    parent_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '父知识点ID，0为顶层',
    title            VARCHAR(200) NOT NULL COMMENT '知识点标题',
    content          LONGTEXT     COMMENT '详细讲解',
    key_point        TEXT         COMMENT '重点',
    difficult_point  TEXT         COMMENT '难点',
    sort             INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course (course_id),
    KEY idx_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='知识点表';

-- ----------------------------
-- 题目表
-- type: SINGLE 单选 / MULTI 多选 / JUDGE 判断
-- options: JSON 数组 [{"key":"A","content":"选项内容"}]
-- answer:  JSON 数组，如 ["A"]、["A","C"]、["T"]、["F"]
-- ----------------------------
DROP TABLE IF EXISTS question;
CREATE TABLE question (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id           BIGINT       NOT NULL COMMENT '所属课程',
    knowledge_point_id  BIGINT       DEFAULT NULL COMMENT '关联知识点',
    type                VARCHAR(20)  NOT NULL COMMENT '题型：SINGLE/MULTI/JUDGE',
    content             TEXT         NOT NULL COMMENT '题干',
    options             TEXT         COMMENT '选项JSON',
    answer              VARCHAR(255) NOT NULL COMMENT '正确答案JSON数组',
    analysis            TEXT         COMMENT '解析',
    difficulty          INT          NOT NULL DEFAULT 1 COMMENT '难度1-5',
    status              TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course (course_id),
    KEY idx_kp (knowledge_point_id),
    KEY idx_type (type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='题目表';

-- ----------------------------
-- 答题记录表
-- ----------------------------
DROP TABLE IF EXISTS answer_record;
CREATE TABLE answer_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT       NOT NULL COMMENT '用户',
    question_id  BIGINT       NOT NULL COMMENT '题目',
    user_answer  VARCHAR(255) NOT NULL COMMENT '用户答案JSON数组',
    is_correct   TINYINT      NOT NULL COMMENT '是否正确：1正确 0错误',
    submitted_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_question (question_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='答题记录表';

-- ----------------------------
-- 学习进度表（用户学习过的知识点）
-- ----------------------------
DROP TABLE IF EXISTS study_record;
CREATE TABLE study_record (
    id                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id            BIGINT   NOT NULL COMMENT '用户',
    knowledge_point_id BIGINT   NOT NULL COMMENT '知识点',
    studied            TINYINT  NOT NULL DEFAULT 1 COMMENT '是否已学',
    last_studied_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后学习时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_kp (user_id, knowledge_point_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='学习进度表';

-- ----------------------------
-- AI 配置表（单例，后台维护）
-- ----------------------------
DROP TABLE IF EXISTS ai_config;
CREATE TABLE ai_config (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    provider      VARCHAR(50)  NOT NULL DEFAULT 'openai' COMMENT '提供商',
    base_url      VARCHAR(255) DEFAULT NULL COMMENT '接口地址',
    api_key       VARCHAR(255) DEFAULT NULL COMMENT 'API密钥',
    model         VARCHAR(100) DEFAULT NULL COMMENT '模型名',
    system_prompt TEXT         COMMENT '系统提示词',
    enabled       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否启用',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI配置表';

-- ----------------------------
-- AI 对话记录表
-- ----------------------------
DROP TABLE IF EXISTS ai_chat_record;
CREATE TABLE ai_chat_record (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT   NOT NULL COMMENT '用户',
    role       VARCHAR(20)  NOT NULL COMMENT '角色：user/assistant',
    content    TEXT         COMMENT '内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI对话记录表';
