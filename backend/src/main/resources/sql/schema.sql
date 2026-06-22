-- ============================================================
-- AI 智能题库系统 - 数据库建表脚本
-- 适用 MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

-- 1. 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(120) COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：ADMIN/TEACHER/STUDENT',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 科目表
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '科目名',
    `code` VARCHAR(20) NOT NULL UNIQUE COMMENT '科目代码',
    `description` VARCHAR(255) COMMENT '描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- 3. 题目表
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `subject_id` BIGINT NOT NULL COMMENT '科目ID',
    `type` VARCHAR(20) NOT NULL COMMENT '题型：SINGLE/MULTIPLE/JUDGE/ESSAY',
    `difficulty` INT NOT NULL DEFAULT 1 COMMENT '难度：1-5',
    `content` TEXT NOT NULL COMMENT '题干',
    `options` JSON COMMENT '选项（单选/多选）JSON 数组',
    `answer` VARCHAR(500) NOT NULL COMMENT '答案',
    `explanation` TEXT COMMENT '解析',
    `knowledge_point` VARCHAR(255) COMMENT '知识点/考点',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_subject` (`subject_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_knowledge` (`knowledge_point`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 4. 试卷表
DROP TABLE IF EXISTS `papers`;
CREATE TABLE `papers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
    `description` TEXT COMMENT '描述',
    `subject_id` BIGINT NOT NULL COMMENT '科目ID',
    `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
    `duration` INT NOT NULL DEFAULT 60 COMMENT '时长（分钟）',
    `created_by` BIGINT NOT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_subject` (`subject_id`),
    INDEX `idx_creator` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 5. 试卷-题目关联表
DROP TABLE IF EXISTS `paper_questions`;
CREATE TABLE `paper_questions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `paper_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `score` INT NOT NULL DEFAULT 0 COMMENT '该题分值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '题目顺序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
    INDEX `idx_paper` (`paper_id`),
    INDEX `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-题目关联';

-- 6. 考试表
DROP TABLE IF EXISTS `exams`;
CREATE TABLE `exams` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `paper_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `start_time` DATETIME COMMENT '开放时间',
    `end_time` DATETIME COMMENT '截止时间',
    `duration` INT NOT NULL DEFAULT 60 COMMENT '时长（分钟）',
    `max_attempts` INT NOT NULL DEFAULT 1 COMMENT '最大参考次数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/ONGOING/FINISHED/ARCHIVED',
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_paper` (`paper_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 7. 考试记录表
DROP TABLE IF EXISTS `exam_records`;
CREATE TABLE `exam_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `paper_id` BIGINT NOT NULL,
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `total_score` INT NOT NULL DEFAULT 0 COMMENT '总分',
    `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS/SUBMITTED/GRADED',
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `submit_time` DATETIME COMMENT '提交时间',
    `duration_used` INT COMMENT '实际用时（秒）',
    INDEX `idx_exam` (`exam_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录';

-- 8. 答题详情表
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `record_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `user_answer` VARCHAR(2000) COMMENT '用户答案',
    `is_correct` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否正确',
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `time_spent` INT COMMENT '用时（秒）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_record` (`record_id`),
    INDEX `idx_question` (`question_id`),
    INDEX `idx_correct` (`is_correct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情';

-- 9. 错题本
DROP TABLE IF EXISTS `wrong_questions`;
CREATE TABLE `wrong_questions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `wrong_count` INT NOT NULL DEFAULT 1 COMMENT '错误次数',
    `mastered` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已掌握',
    `last_wrong_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_mastered` (`mastered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

-- 10. AI 调用日志
DROP TABLE IF EXISTS `ai_logs`;
CREATE TABLE `ai_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '调用人',
    `feature` VARCHAR(50) NOT NULL COMMENT '功能：EXPLAIN/RECOMMEND/CLASSIFY',
    `input` TEXT COMMENT '输入',
    `output` TEXT COMMENT '输出',
    `tokens_used` INT COMMENT '消耗 tokens',
    `cost_ms` INT COMMENT '耗时（毫秒）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    `error_msg` TEXT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_feature` (`feature`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认科目
INSERT INTO `subjects` (`name`, `code`, `description`) VALUES
('Java 程序设计', 'JAVA', 'Java 基础到高级'),
('数据结构', 'DS', '数据结构与算法'),
('高等数学', 'MATH', '大学高等数学'),
('英语', 'ENGLISH', '大学英语'),
('计算机网络', 'NETWORK', '网络基础');

-- 默认管理员（密码 admin123，BCrypt 加密）
-- 生成方法：BCryptPasswordEncoder().encode("admin123")
INSERT INTO `users` (`username`, `password`, `nickname`, `role`, `email`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN', 'admin@aiqb.com'),
('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张老师', 'TEACHER', 'teacher1@aiqb.com'),
('student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李同学', 'STUDENT', 'student1@aiqb.com');

-- 示例题目
INSERT INTO `questions` (`subject_id`, `type`, `difficulty`, `content`, `options`, `answer`, `explanation`, `knowledge_point`, `created_by`) VALUES
(1, 'SINGLE', 1, 'Java 中 main 方法的返回类型是什么？', JSON_ARRAY('void', 'int', 'String', 'boolean'), 'A', 'main 方法必须是 public static void main(String[] args)。', 'Java 基础', 2),
(1, 'SINGLE', 2, '以下哪个不是 Java 的基本数据类型？', JSON_ARRAY('int', 'String', 'double', 'boolean'), 'B', 'String 是引用类型，不是基本数据类型。', '数据类型', 2),
(1, 'JUDGE', 1, 'Java 中 String 是不可变对象。', NULL, 'T', 'String 对象一旦创建不可修改，所有修改都会生成新对象。', 'String', 2),
(1, 'MULTIPLE', 3, '以下哪些是 Java 的集合类？', JSON_ARRAY('ArrayList', 'HashMap', 'HashSet', 'StringBuilder'), 'A,B,C', 'StringBuilder 不是集合类，是字符串处理工具。', '集合框架', 2);

-- 示例试卷
INSERT INTO `papers` (`title`, `description`, `subject_id`, `total_score`, `duration`, `created_by`) VALUES
('Java 基础测试（一）', '考察 Java 基础语法', 1, 100, 60, 2);

-- 试卷题目关联
INSERT INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`) VALUES
(1, 1, 30, 1),
(1, 2, 30, 2),
(1, 3, 20, 3),
(1, 4, 20, 4);
