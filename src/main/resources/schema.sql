-- ============================================
-- H2 数据库初始化脚本
-- 用于 SQLPractice.java 中的手写SQL练习
-- ============================================

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL COMMENT '班级名称'
);

-- 学生表（外键关联班级）
CREATE TABLE IF NOT EXISTS student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    age INT COMMENT '年龄',
    class_id INT COMMENT '所属班级',
    FOREIGN KEY (class_id) REFERENCES class(id)
);

-- 成绩表（外键关联学生）
CREATE TABLE IF NOT EXISTS score (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL COMMENT '学生ID',
    subject VARCHAR(20) NOT NULL COMMENT '科目',
    score DECIMAL(5,1) COMMENT '成绩',
    FOREIGN KEY (student_id) REFERENCES student(id)
);

-- 索引（面试常考点）
CREATE INDEX IF NOT EXISTS idx_student_name ON student(name);
CREATE INDEX IF NOT EXISTS idx_score_student ON score(student_id);
-- 联合索引 — 遵循最左前缀原则
CREATE INDEX IF NOT EXISTS idx_score_student_subject ON score(student_id, subject);

-- 测试数据
INSERT INTO class VALUES (1, '一班'), (2, '二班'), (3, '三班');

INSERT INTO student VALUES
(1, '张三', 20, 1),
(2, '李四', 22, 1),
(3, '王五', 21, 2),
(4, '赵六', 23, 2),
(5, '孙七', 19, NULL);  -- 孙七没有班级，用于LEFT JOIN演示

INSERT INTO score VALUES
(1, 1, '语文', 85),
(2, 1, '数学', 92),
(3, 1, '英语', 78),
(4, 2, '语文', 90),
(5, 2, '数学', 88),
(6, 2, '英语', 95),
(7, 3, '语文', 72),
(8, 3, '数学', 65),
(9, 4, '语文', 88),
(10, 4, '数学', 91);
