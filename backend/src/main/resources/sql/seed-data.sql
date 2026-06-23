-- ============================================================
-- AI 智能题库系统 - 演示数据
-- 适用 MySQL 8.0+
-- 字符集 utf8mb4
-- ============================================================
-- 用法：mysql -u root -p ai_question_bank < seed-data.sql
-- 或者在 Navicat 里执行
-- ============================================================

-- 1. 演示用户（密码统一 admin123，BCrypt 加密）
-- 已存在则忽略
INSERT IGNORE INTO `users` (`username`, `password`, `nickname`, `role`, `email`) VALUES
('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN',   'admin@aiqb.com'),
('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张老师',     'TEACHER', 'teacher1@aiqb.com'),
('teacher2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王老师',     'TEACHER', 'teacher2@aiqb.com'),
('student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李同学',     'STUDENT', 'student1@aiqb.com'),
('student2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王同学',     'STUDENT', 'student2@aiqb.com'),
('student3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵同学',     'STUDENT', 'student3@aiqb.com');

-- 2. 扩展科目（如果已存在会自动跳过）
INSERT IGNORE INTO `subjects` (`name`, `code`, `description`) VALUES
('软件工程',     'SE',     '软件工程导论'),
('操作系统',     'OS',     '计算机操作系统'),
('人工智能导论', 'AI',     'AI 基础与机器学习');

-- ============================================================
-- 3. 题目（多科目 × 多题型 × 多难度 × 多知识点）
-- 已存在则跳过（按 content 判重）
-- ============================================================

-- ---- Java 程序设计（subject_id=1）----
INSERT IGNORE INTO `questions` (`subject_id`, `type`, `difficulty`, `content`, `options`, `answer`, `explanation`, `knowledge_point`, `score`, `created_by`) VALUES
-- 入门 1★
(1, 'SINGLE', 1, 'Java 中 main 方法的返回类型是什么？', JSON_ARRAY('void', 'int', 'String', 'boolean'), 'A', 'main 方法必须是 public static void main(String[] args)。', 'Java 基础', 2, 2),
(1, 'SINGLE', 1, '下列哪个不是 Java 的关键字？', JSON_ARRAY('class', 'interface', 'sizeof', 'static'), 'C', 'sizeof 不是 Java 关键字，Java 中没有 sizeof 运算符。', 'Java 基础', 2, 2),
(1, 'JUDGE', 1, 'Java 中 String 是不可变对象。', NULL, 'T', 'String 对象一旦创建就不可修改，所有"修改"都会生成新对象。', 'String', 2, 2),
(1, 'JUDGE', 1, 'Java 的源代码文件后缀是 .class。', NULL, 'F', 'Java 源代码后缀是 .java，编译后字节码才是 .class。', 'Java 基础', 2, 2),
(1, 'FILL', 1, 'Java 中定义常量的关键字是 _____。', NULL, 'final', 'final 修饰的变量只能赋值一次，即为常量。', 'Java 基础', 3, 2),

-- 简单 2★
(1, 'SINGLE', 2, '以下哪个不是 Java 的基本数据类型？', JSON_ARRAY('int', 'String', 'double', 'boolean'), 'B', 'String 是引用类型，不是基本数据类型。', '数据类型', 3, 2),
(1, 'SINGLE', 2, 'Java 中用于强制类型转换的运算符是？', JSON_ARRAY('+', '(type)', 'as', 'cast'), 'B', 'Java 用 (type) 强制类型转换。', '数据类型', 3, 2),
(1, 'SINGLE', 2, 'Java 中数组下标从几开始？', JSON_ARRAY('0', '1', '-1', '由程序员决定'), 'A', 'Java 数组下标从 0 开始。', '数组', 3, 2),
(1, 'MULTIPLE', 2, '以下哪些是 Java 的访问修饰符？', JSON_ARRAY('public', 'private', 'protected', 'default'), 'A,B,C,D', '四种访问修饰符：public、private、protected、默认（default）。', '面向对象', 4, 2),
(1, 'JUDGE', 2, 'Java 中所有类都隐式继承自 Object 类。', NULL, 'T', 'Java 中所有类都直接或间接继承自 java.lang.Object。', '面向对象', 2, 2),
(1, 'FILL', 2, 'Java 中创建对象的运算符是 _____。', NULL, 'new', '使用 new 关键字创建对象实例。', '面向对象', 3, 2),

-- 中等 3★
(1, 'SINGLE', 3, '以下哪些是 Java 的集合类？', JSON_ARRAY('ArrayList', 'HashMap', 'HashSet', 'StringBuilder'), 'A', 'ArrayList 是集合类；HashMap/HashSet 是 Map/Set；StringBuilder 不是。', '集合框架', 3, 2),
(1, 'SINGLE', 3, 'Java 中 HashMap 的默认初始容量是？', JSON_ARRAY('8', '16', '32', '64'), 'B', 'HashMap 默认初始容量是 16，负载因子 0.75。', '集合框架', 3, 2),
(1, 'SINGLE', 3, '以下哪个关键字用于抛出异常？', JSON_ARRAY('throw', 'throws', '两者都是', 'try'), 'C', 'throw 在方法体内主动抛出异常，throws 在方法签名声明可能抛出的异常。', '异常处理', 3, 2),
(1, 'MULTIPLE', 3, '关于 ArrayList，正确的有？', JSON_ARRAY('可以存储 null', '线程安全', '底层是数组', '查询快增删慢'), 'A,C,D', 'ArrayList 允许 null，非线程安全（Vector 才是），底层是 Object[]。', '集合框架', 4, 2),
(1, 'MULTIPLE', 3, '以下哪些是线程安全的集合？', JSON_ARRAY('Vector', 'HashMap', 'Hashtable', 'ConcurrentHashMap'), 'A,C,D', 'Vector、Hashtable、ConcurrentHashMap 是线程安全；HashMap 不是。', '多线程', 4, 2),
(1, 'JUDGE', 3, 'Java 中 StringBuilder 是线程安全的。', NULL, 'F', 'StringBuilder 非线程安全，StringBuffer 才是线程安全。', 'String', 2, 2),

-- 困难 4★
(1, 'SINGLE', 4, 'JVM 堆内存中，存放对象的区域是？', JSON_ARRAY('程序计数器', '虚拟机栈', '堆', '方法区'), 'C', 'JVM 堆用于存放对象实例，是 GC 管理的主要区域。', 'JVM', 4, 2),
(1, 'SINGLE', 4, 'Java 中 synchronized 关键字修饰静态方法时，锁的是？', JSON_ARRAY('调用对象', 'Class 对象', '当前线程', '不锁'), 'B', '修饰静态方法时，锁的是该类的 Class 对象。', '多线程', 4, 2),
(1, 'MULTIPLE', 4, 'GC Roots 包括以下哪些？', JSON_ARRAY('虚拟机栈引用的对象', '方法区静态属性引用的对象', '方法区常量引用的对象', '所有存活对象'), 'A,B,C', 'GC Roots 包括：栈帧中的引用、方法区静态/常量引用、被同步锁持有的对象等。', 'JVM', 5, 2),
(1, 'FILL', 4, 'Java 8 中函数式接口的注解是 _____。', NULL, '@FunctionalInterface', '@FunctionalInterface 标注函数式接口，编译器会校验。', 'Java 8 新特性', 4, 2),

-- 挑战 5★
(1, 'SINGLE', 5, '关于 Java 内存模型（JMM），错误的是？', JSON_ARRAY('volatile 保证可见性', 'synchronized 保证原子性', 'volatile 保证原子性', 'happens-before 是 JMM 的核心概念'), 'C', 'volatile 不保证原子性（仅保证可见性和禁止重排序），比如 i++ 不安全。', 'JVM', 5, 2),
(1, 'SINGLE', 5, 'ConcurrentHashMap 在 JDK 1.8 中的底层实现是？', JSON_ARRAY('分段锁 + 数组 + 链表', 'CAS + synchronized + 数组 + 链表 + 红黑树', '纯 HashTable', '纯红黑树'), 'B', 'JDK 1.8 抛弃分段锁，改为 CAS + synchronized + Node 数组 + 链表 + 红黑树。', '多线程', 5, 2),

-- ---- 数据结构（subject_id=2）----
(2, 'SINGLE', 1, '栈的特点是？', JSON_ARRAY('先进先出', '后进先出', '随机访问', '以上都不是'), 'B', '栈（Stack）遵循 LIFO（Last In First Out）。', '线性表', 2, 2),
(2, 'SINGLE', 1, '队列的特点是？', JSON_ARRAY('先进先出', '后进先出', '随机访问', '以上都不是'), 'A', '队列（Queue）遵循 FIFO（First In First Out）。', '线性表', 2, 2),
(2, 'SINGLE', 2, '二分查找要求序列是？', JSON_ARRAY('无序', '有序', '可以是链表', '以上都可以'), 'B', '二分查找要求序列有序（升序或降序），且最好是顺序存储。', '查找算法', 3, 2),
(2, 'SINGLE', 2, '链表相比数组的优势是？', JSON_ARRAY('随机访问快', '插入删除快', '内存连续', '占用空间小'), 'B', '链表插入删除只需修改指针，时间复杂度 O(1)；数组需要移动元素 O(n)。', '线性表', 3, 2),
(2, 'SINGLE', 3, '二叉树前序遍历的顺序是？', JSON_ARRAY('根 左 右', '左 根 右', '左 右 根', '右 左 根'), 'A', '前序遍历：根节点 → 左子树 → 右子树。', '树', 3, 2),
(2, 'SINGLE', 3, '哈希表查找的平均时间复杂度是？', JSON_ARRAY('O(1)', 'O(log n)', 'O(n)', 'O(n log n)'), 'A', '哈希表在不冲突情况下查找时间复杂度 O(1)。', '查找算法', 3, 2),
(2, 'SINGLE', 4, '红黑树是一种？', JSON_ARRAY('二叉搜索树', '平衡二叉树', '自平衡二叉搜索树', 'B 树'), 'C', '红黑树是自平衡的二叉搜索树，通过颜色约束保证近似平衡。', '树', 4, 2),
(2, 'MULTIPLE', 3, '以下哪些是稳定排序？', JSON_ARRAY('冒泡排序', '快速排序', '归并排序', '堆排序'), 'A,C', '冒泡排序、归并排序是稳定排序；快排、堆排是不稳定。', '排序算法', 4, 2),
(2, 'SINGLE', 5, 'Dijkstra 算法不适用以下哪种图？', JSON_ARRAY('无向图', '有向图', '含负权边的图', '连通图'), 'C', 'Dijkstra 不能处理负权边（应使用 Bellman-Ford 或 SPFA）。', '图算法', 5, 2),

-- ---- 操作系统（subject_id=6，扩展）----
(6, 'SINGLE', 2, '进程与线程的本质区别是？', JSON_ARRAY('进程是动态的，线程是静态的', '进程是资源分配单位，线程是 CPU 调度单位', '线程是资源分配单位，进程是 CPU 调度单位', '二者完全相同'), 'B', '进程拥有独立资源，线程共享进程资源；线程是 CPU 调度的基本单位。', '进程管理', 3, 2),
(6, 'SINGLE', 2, '死锁的四个必要条件不包括？', JSON_ARRAY('互斥', '请求和保持', '可抢占', '循环等待'), 'C', '死锁必要条件：互斥、请求保持、不可剥夺、循环等待。', '进程管理', 3, 2),
(6, 'SINGLE', 3, '虚拟内存的主要作用是？', JSON_ARRAY('扩大物理内存', '实现进程隔离', '加快 CPU 速度', '减少磁盘 IO'), 'A', '虚拟内存通过分页/分段让进程使用比物理内存更大的地址空间。', '内存管理', 3, 2),
(6, 'JUDGE', 2, '页式存储管理不会产生内部碎片。', NULL, 'F', '页式存储管理会产生内部碎片（最后一页未用完）。', '内存管理', 2, 2),
(6, 'SINGLE', 4, 'LRU 页面置换算法置换的是？', JSON_ARRAY('最久未使用的页', '最先进入内存的页', '最少使用的页', '随机页'), 'A', 'LRU（Least Recently Used）选择最久未被访问的页面置换。', '内存管理', 4, 2),

-- ---- 高等数学（subject_id=3）----
(3, 'SINGLE', 1, '函数 y = sin(x) 的导数是？', JSON_ARRAY('cos(x)', '-cos(x)', '-sin(x)', 'tan(x)'), 'A', '(sin x)\' = cos x。', '一元微分', 2, 2),
(3, 'SINGLE', 1, 'lim(x→0) sin(x)/x = ?', JSON_ARRAY('0', '1', '∞', '不存在'), 'B', '重要极限：lim(x→0) sin(x)/x = 1。', '极限', 2, 2),
(3, 'SINGLE', 2, '∫ 1/x dx = ?', JSON_ARRAY('ln|x| + C', '1/x² + C', 'x + C', '-1/x + C'), 'A', '∫ 1/x dx = ln|x| + C。', '一元积分', 3, 2),
(3, 'JUDGE', 1, '连续函数一定可导。', NULL, 'F', '可导必连续，连续不一定可导（如 y=|x| 在 0 点）。', '一元微分', 2, 2),
(3, 'SINGLE', 3, 'e^x 的麦克劳林展开是？', JSON_ARRAY('1 + x + x²/2! + ...', 'x + x³/3! + ...', '1 - x + x²/2! - ...', 'ln(1+x)'), 'A', 'e^x = Σ x^n/n! = 1 + x + x²/2! + ...', '无穷级数', 3, 2),

-- ---- 英语（subject_id=4）----
(4, 'SINGLE', 1, '"Hello" 的中文意思是？', JSON_ARRAY('再见', '你好', '谢谢', '对不起'), 'B', 'Hello = 你好。', '日常英语', 2, 2),
(4, 'SINGLE', 2, '"book" 的中文意思是？', JSON_ARRAY('笔', '书', '桌子', '椅子'), 'B', 'book = 书。', '词汇', 2, 2),
(4, 'SINGLE', 3, '"I am a student." 的正确翻译是？', JSON_ARRAY('我是一名老师', '我是一名学生', '我是一个医生', '我是一名工人'), 'B', 'I am a student = 我是一名学生。', '语法', 3, 2),

-- ---- 计算机网络（subject_id=5）----
(5, 'SINGLE', 1, 'HTTP 默认端口是？', JSON_ARRAY('21', '22', '80', '443'), 'C', 'HTTP 默认 80，HTTPS 默认 443。', '应用层', 2, 2),
(5, 'SINGLE', 2, 'TCP 三次握手中，第二次握手发送的标志位是？', JSON_ARRAY('SYN', 'ACK', 'SYN+ACK', 'FIN'), 'C', '第二次握手：服务器发送 SYN+ACK 回应客户端。', '传输层', 3, 2),
(5, 'SINGLE', 3, 'OSI 七层模型中，IP 协议位于？', JSON_ARRAY('应用层', '传输层', '网络层', '数据链路层'), 'C', 'IP 协议位于网络层。', '网络层', 3, 2),
(5, 'JUDGE', 2, 'TCP 是面向连接的可靠传输协议。', NULL, 'T', 'TCP 通过三次握手建立连接，提供可靠、有序的字节流传输。', '传输层', 2, 2);

-- ============================================================
-- 4. 试卷（3 套，覆盖不同规模）
-- ============================================================

-- 试卷 1：Java 基础测试（期中） - 已存在则跳过
INSERT IGNORE INTO `papers` (`id`, `title`, `description`, `subject_id`, `total_score`, `duration`, `created_by`) VALUES
(1, 'Java 基础测试（一）', '考察 Java 基础语法', 1, 100, 60, 2);

-- 试卷 2：Java 进阶模拟
INSERT IGNORE INTO `papers` (`title`, `description`, `subject_id`, `total_score`, `duration`, `created_by`) VALUES
('Java 进阶模拟卷', '面向对象 + 集合 + 异常 + 多线程综合', 1, 100, 90, 2),
('数据结构期末考试', '线性表 + 树 + 排序 + 查找综合', 2, 100, 120, 2),
('操作系统期中测验', '进程管理 + 内存管理', 6, 100, 60, 2),
('高等数学单元测试 1', '极限 + 微分 + 积分基础', 3, 100, 90, 2),
('Java 入门小测验', '5 道入门题', 1, 50, 30, 2);

-- ============================================================
-- 5. 试卷-题目关联
-- 论文查题时如果发现 paper_questions 已经有就跳过
-- ============================================================

-- 试卷 1 (Java 基础测试)
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`) VALUES
(1, 1, 30, 0), (1, 2, 30, 1), (1, 3, 20, 2), (1, 4, 20, 3);

-- 试卷 2 (Java 进阶模拟) - 选第 6~15 题
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`)
SELECT 2, q.id, 10, ROW_NUMBER() OVER (ORDER BY q.id) - 1
FROM questions q WHERE q.id BETWEEN 6 AND 15;

-- 试卷 3 (数据结构期末) - 第 23~32 题
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`)
SELECT 3, q.id, 10, ROW_NUMBER() OVER (ORDER BY q.id) - 1
FROM questions q WHERE q.id BETWEEN 23 AND 32;

-- 试卷 4 (操作系统期中) - 第 33~37 题
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`)
SELECT 4, q.id, 20, ROW_NUMBER() OVER (ORDER BY q.id) - 1
FROM questions q WHERE q.id BETWEEN 33 AND 37;

-- 试卷 5 (高数单元测试) - 第 38~42 题
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`)
SELECT 5, q.id, 20, ROW_NUMBER() OVER (ORDER BY q.id) - 1
FROM questions q WHERE q.id BETWEEN 38 AND 42;

-- 试卷 6 (Java 入门小测验) - 第 1~5 题
INSERT IGNORE INTO `paper_questions` (`paper_id`, `question_id`, `score`, `sort_order`)
SELECT 6, q.id, 10, ROW_NUMBER() OVER (ORDER BY q.id) - 1
FROM questions q WHERE q.id BETWEEN 1 AND 5;

-- ============================================================
-- 6. 考试（已发布 + 进行中 + 草稿 + 归档 各种状态）
-- ============================================================
INSERT IGNORE INTO `exams` (`title`, `paper_id`, `start_time`, `end_time`, `duration`, `max_attempts`, `status`, `created_by`) VALUES
('Java 基础期中考试', 1, '2026-06-15 09:00:00', '2026-07-30 23:59:59', 60, 3, 'PUBLISHED', 2),
('Java 进阶模拟测验', 2, '2026-06-20 10:00:00', '2026-08-15 23:59:59', 90, 2, 'PUBLISHED', 2),
('数据结构期末统考', 3, '2026-07-01 14:00:00', '2026-07-15 23:59:59', 120, 1, 'DRAFT', 2),
('操作系统期中测验', 4, '2026-06-10 09:00:00', '2026-06-30 23:59:59', 60, 1, 'ONGOING', 2),
('高数单元测试 1', 5, '2026-05-01 10:00:00', '2026-05-15 23:59:59', 90, 1, 'ARCHIVED', 2),
('Java 入门小测验', 6, '2026-06-25 09:00:00', '2026-08-30 23:59:59', 30, 5, 'PUBLISHED', 2);

-- ============================================================
-- 7. 历史考试记录 + 错题（多样化数据）
-- 让学生账户有丰富的统计数据
-- ============================================================

-- student1 完成的考试 1（得分 80，错题）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(1, 1, 4, 1, 80, 100, 'GRADED', '2026-06-15 09:05:00', '2026-06-15 09:55:00', 3000);
INSERT IGNORE INTO `answers` (`record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
(1, 1, 'A', 1, 30),
(1, 2, 'C', 0, 0),  -- 错
(1, 3, 'T', 1, 20),
(1, 4, 'F', 1, 20);

-- student1 完成的考试 2（得分 60，错题较多）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(2, 2, 4, 2, 60, 100, 'GRADED', '2026-06-20 10:10:00', '2026-06-20 11:30:00', 4800);
-- 简化：只标错题
INSERT IGNORE INTO `answers` (`record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
(2, 7, 'A', 1, 10),
(2, 8, 'A', 0, 0),  -- 错
(2, 9, 'A,C,D', 0, 0),  -- 错
(2, 10, 'A', 1, 10),
(2, 11, 'T', 1, 10),
(2, 12, 'new', 1, 10),
(2, 13, 'A', 1, 10),
(2, 14, 'D', 0, 0),  -- 错
(2, 15, 'A,B,C', 1, 10),
(2, 16, 'F', 1, 10);

-- student1 完成的考试 4（操作系统）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(3, 4, 4, 4, 60, 100, 'GRADED', '2026-06-10 09:00:00', '2026-06-10 09:55:00', 3300);
INSERT IGNORE INTO `answers` (`record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
(3, 33, 'B', 1, 20),
(3, 34, 'C', 1, 20),
(3, 35, 'A', 0, 0),  -- 错
(3, 36, 'T', 1, 20),
(3, 37, 'A', 0, 0);  -- 错

-- student1 完成的考试 5（高数）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(4, 5, 4, 5, 80, 100, 'GRADED', '2026-05-05 10:00:00', '2026-05-05 11:25:00', 5100);
INSERT IGNORE INTO `answers` (`record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
(4, 38, 'A', 1, 20),
(4, 39, 'B', 1, 20),
(4, 40, 'A', 1, 20),
(4, 41, 'F', 0, 0),  -- 错
(4, 42, 'A', 1, 20);

-- student1 进行中（断点续答测试用）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`) VALUES
(5, 1, 4, 1, 0, 100, 'IN_PROGRESS', '2026-06-22 18:00:00');
INSERT IGNORE INTO `answers` (`record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
(5, 1, 'A', 0, 0),
(5, 2, 'B', 0, 0);

-- student2 的考试记录（不同的学生验证隔离）
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(6, 1, 5, 1, 60, 100, 'GRADED', '2026-06-15 09:10:00', '2026-06-15 10:00:00', 3000),
(7, 2, 5, 2, 80, 100, 'GRADED', '2026-06-20 10:00:00', '2026-06-20 11:20:00', 4800),
(8, 4, 5, 4, 80, 100, 'GRADED', '2026-06-10 09:00:00', '2026-06-10 09:50:00', 3000);

-- student3
INSERT IGNORE INTO `exam_records` (`id`, `exam_id`, `user_id`, `paper_id`, `score`, `total_score`, `status`, `start_time`, `submit_time`, `duration_used`) VALUES
(9, 1, 6, 1, 100, 100, 'GRADED', '2026-06-15 09:00:00', '2026-06-15 09:45:00', 2700),
(10, 2, 6, 2, 90, 100, 'GRADED', '2026-06-20 10:00:00', '2026-06-20 11:15:00', 4500),
(11, 4, 6, 4, 100, 100, 'GRADED', '2026-06-10 09:00:00', '2026-06-10 09:40:00', 2400),
(12, 5, 6, 5, 100, 100, 'GRADED', '2026-05-05 10:00:00', '2026-05-05 11:00:00', 3600);

-- ============================================================
-- 8. 错题（自动从考试错题入库）
-- ============================================================
INSERT IGNORE INTO `wrong_questions` (`user_id`, `question_id`, `wrong_count`, `mastered`) VALUES
-- student1
(4, 2, 1, 0),  -- main 返回类型
(4, 8, 1, 0),  -- 强制类型转换
(4, 9, 1, 0),  -- 多选访问修饰符
(4, 14, 1, 0), -- ArrayList
(4, 35, 1, 0), -- 虚拟内存
(4, 37, 1, 0), -- LRU
(4, 41, 1, 1), -- 高数连续可导（已掌握）
-- student2
(5, 8, 1, 0),
(5, 14, 1, 0),
(5, 33, 1, 0);

-- ============================================================
-- 9. AI 调用日志（演示用，体现 AI 模块活跃）
-- ============================================================
INSERT IGNORE INTO `ai_logs` (`user_id`, `feature`, `input`, `output`, `tokens_used`, `cost_ms`, `status`) VALUES
(4, 'EXPLAIN',   '解释题目：Java 中 HashMap 的默认初始容量是？', 'HashMap 默认初始容量为 16，负载因子 0.75。...', 120, 2300, 'SUCCESS'),
(4, 'RECOMMEND', '推荐同类题：基于错题 8',  '推荐 3 道集合类相关题目...', 380, 5100, 'SUCCESS'),
(5, 'EXPLAIN',   '解释题目：LRU 页面置换算法', 'LRU 选择最久未被访问的页面...', 150, 2800, 'SUCCESS'),
(2, 'EXPLAIN',   '解释题目：JMM volatile', 'volatile 不保证原子性...', 220, 3500, 'FAILED');

-- ============================================================
-- 完成
-- ============================================================
SELECT '=========================================' AS '';
SELECT '演示数据导入完成' AS '消息';
SELECT CONCAT('用户数: ', COUNT(*)) AS '统计' FROM users;
SELECT CONCAT('科目数: ', COUNT(*)) AS '统计' FROM subjects;
SELECT CONCAT('题目数: ', COUNT(*)) AS '统计' FROM questions;
SELECT CONCAT('试卷数: ', COUNT(*)) AS '统计' FROM papers;
SELECT CONCAT('考试数: ', COUNT(*)) AS '统计' FROM exams;
SELECT CONCAT('考试记录: ', COUNT(*)) AS '统计' FROM exam_records;
SELECT CONCAT('错题数: ', COUNT(*)) AS '统计' FROM wrong_questions;
