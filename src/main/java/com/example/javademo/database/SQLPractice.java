package com.example.javademo.database;

import java.sql.*;

/**
 * 手写SQL练习 — 后端面试最高频考点
 *
 * 【面试常问 — SELECT语句执行顺序】
 * FROM → JOIN → ON → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT
 *
 * 【面试常问 — JOIN类型】
 * - INNER JOIN: 取交集
 * - LEFT JOIN: 左表全部 + 右表匹配（无匹配填NULL）
 * - RIGHT JOIN: 右表全部 + 左表匹配
 * - 自连接: 同一张表自己join自己（组织架构、上下级）
 *
 * 【面试常问 — 索引】
 * - 主键索引: 唯一，非空，聚簇索引（数据和索引在一起）
 * - 唯一索引: 唯一，可为NULL
 * - 普通索引: 加速查询
 * - 联合索引: 多列组合，最左前缀原则
 * - B+Tree: 所有数据在叶子节点，叶子节点有双向链表，适合范围查询
 *
 * 【面试常问 — 事务ACID】
 * - 原子性(Atomicity): 要么全部成功，要么全部失败
 * - 一致性(Consistency): 事务前后数据状态一致
 * - 隔离性(Isolation): 并发事务互不干扰
 * - 持久性(Durability): 提交后永久保存
 *
 * 【隔离级别】 读未提交 → 读已提交(RC) → 可重复读(RR) → 串行化
 * 脏读 | 不可重复读 | 幻读 依次解决
 *
 * 【常见坑】
 * - WHERE 中函数/计算会导致索引失效（如 WHERE YEAR(date) = 2024）
 * - LIKE '%xxx' 前面有%不走索引
 * - 联合索引跳过第一列不走索引（违反最左前缀）
 * - NOT IN 遇到NULL整体返回空
 */
public class SQLPractice {

  private static final String JDBC_URL = "jdbc:mysql://localhost:3306/edu_platform_mysql_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  private static final String USER = "root";
  private static final String PASSWORD = "315689";

  public static void main(String[] args) {
    // initDatabase();

    // System.out.println("\n========== 1. 基本查询(WHERE/ORDER BY/LIMIT) ==========");
    // basicQuery();

    // System.out.println("\n========== 2. JOIN联表查询 ==========");
    // joinQuery();

    // System.out.println("\n========== 3. 聚合函数 + GROUP BY + HAVING ==========");
    // groupByQuery();

    System.out.println("\n========== 4. 子查询 ==========");
    subQuery();

    // System.out.println("\n========== 5. 索引 ==========");
    // indexDemo();

    // System.out.println("\n========== 6. 事务隔离级别 ==========");
    // isolationDemo();

    // System.out.println("\n========== 7. 常见SQL陷阱 ==========");
    // sqlTrapDemo();
  }

  static void initDatabase() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // 班级表
      stmt.execute("""
          CREATE TABLE class (
              id INT AUTO_INCREMENT PRIMARY KEY,
              name VARCHAR(20) NOT NULL
          )
          """);

      // 学生表
      stmt.execute("""
          CREATE TABLE student (
              id INT AUTO_INCREMENT PRIMARY KEY,
              name VARCHAR(50) NOT NULL,
              age INT,
              class_id INT
          )
          """);

      // 成绩表
      stmt.execute("""
          CREATE TABLE score (
              id INT AUTO_INCREMENT PRIMARY KEY,
              student_id INT,
              subject VARCHAR(20),
              score DECIMAL(5,1)
          )
          """);

      // 插入数据
      stmt.execute("INSERT INTO class VALUES (1, '一班'), (2, '二班'), (3, '三班')");
      stmt.execute("INSERT INTO student VALUES (1, '张三', 20, 1), (2, '李四', 22, 1), "
          + "(3, '王五', 21, 2), (4, '赵六', 23, 2), (5, '孙七', 19, NULL)");
      stmt.execute("INSERT INTO score VALUES "
          + "(1, 1, '语文', 85), (2, 1, '数学', 92), (3, 1, '英语', 78), "
          + "(4, 2, '语文', 90), (5, 2, '数学', 88), (6, 2, '英语', 95), "
          + "(7, 3, '语文', 72), (8, 3, '数学', 65), "
          + "(9, 4, '语文', 88), (10, 4, '数学', 91)");

      System.out.println("数据库初始化完成: 3张表 + 测试数据");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void basicQuery() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      System.out.println("【SELECT 执行顺序】FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY");

      // 查询id大于10的学生
      try (ResultSet rs = stmt.executeQuery(
          "SELECT name, id FROM students WHERE id > 10 ORDER BY id DESC")) {
        System.out.println("id>10的学生（降序）:");
        while (rs.next()) {
          System.out.println(" " + rs.getString("name") + " " + rs.getInt("id"));
        }
      }

      // LIMIT 分页（假设每页2条，第2页）
      System.out.println("\n分页查询（第3页，每页3条）:");
      try (ResultSet rs = stmt.executeQuery(
          "SELECT name, id FROM students ORDER BY id LIMIT 6,3")) {
        // MySQL用LIMIT 2,2，H2用LIMIT 2 OFFSET 2
        while (rs.next()) {
          System.out.println(" " + rs.getString("name") + rs.getString("id"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void joinQuery() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // INNER JOIN — 返回匹配的行
      System.out.println("INNER JOIN（课程+学生，返回所有选修了课程的学生（或是有人选的课））:");
      try (ResultSet rs = stmt.executeQuery("""
          SELECT s.name AS 学生, c.name AS 课程
          FROM courses c
          INNER JOIN enrollments e ON c.id = e.course_id
          INNER JOIN students s ON e.student_id = s.id
          ORDER bY 课程 DESC
          """)) {
        while (rs.next()) {
          System.out.println(" " + rs.getString("课程") + " → " + rs.getString("学生"));
        }
      }

      // LEFT JOIN — 左表全部返回
      System.out.println("\nLEFT 课程+学生，所有课程都出现）:");
      try (ResultSet rs = stmt.executeQuery("""
          SELECT s.name AS 学生, c.name AS 课程
          FROM courses c
          LEFT JOIN enrollments e ON c.id = e.course_id
          LEFT JOIN students s ON e.student_id = s.id
          ORDER bY 课程 DESC
          """)) {
        while (rs.next()) {
          String cname = rs.getString("课程");
          System.out.println(" " + (cname != null ? cname : "【NULL-无学生】") + " → "
              + rs.getString("学生"));
        }
      }

      // 三表联查
      System.out.println("\n三表联查（学生+课程+成绩）:");
      try (ResultSet rs = stmt.executeQuery("""
              SELECT c.name AS 课程, s.name AS 学生, e.score AS 成绩
              FROM courses c
              INNER JOIN enrollments e ON c.id = e.course_id
              INNER JOIN students s ON e.student_id = s.id
              ORDER bY 课程 DESC
          """)) {
        while (rs.next()) {
          System.out.printf(" %s | %s | %.1f%n",
              rs.getString("课程"), rs.getString("学生"),
              rs.getFloat("成绩"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void groupByQuery() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // 每个学生的平均分
      System.out.println("每个学生的平均分:");
      try (ResultSet rs = stmt.executeQuery("""
          SELECT s.name AS 姓名, AVG(e.score) AS 平均分, COUNT(*) as 科目数
          FROM students s
          LEFT JOIN enrollments e ON s.id = e.student_id
          GROUP BY s.id, s.name
          having AVG(e.score) >= 80
          ORDER BY 平均分 DESC;
          """)) {
        System.out.println("（HAVING过滤平均分>=80的）");
        while (rs.next()) {
          System.out.printf("  %s: 平均%.1f (%d门课)%n",
              rs.getString("姓名"), rs.getDouble("平均分"),
              rs.getInt("科目数"));
        }
      }

      // 每个班级的学生人数
      System.out.println("\n每个班级人数:");
      try (ResultSet rs = stmt.executeQuery("""
          select s.class_name as 班级, count(*) 班级人数
          from students s
          group by s.class_name
          """)) {
        while (rs.next()) {
          System.out.println("  " + rs.getString("班级") + ": "
              + rs.getInt("班级人数") + "人");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void subQuery() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // WHERE子查询 — 查询高于平均分的学生
      System.out.println("高于平均分的学生（WHERE子查询）:");
      try (ResultSet rs = stmt.executeQuery("""
          select s.name , AVG(e.score ) as 平均分
          from  students s 
          inner join enrollments e on s.id = e.student_id 
          group by s.id, s.name 
          having AVG(e.score) > (select AVG(score) from enrollments e2 )
          """)) {
        while (rs.next()) {
          System.out.printf("  %s: %.1f%n",
              rs.getString("name"), rs.getDouble("平均分"));
        }
      }

      // FROM子查询
      System.out.println("\n每个学生最高分科目（FROM子查询）:");
      try (ResultSet rs = stmt.executeQuery("""
          select s.name, T.max_sc
          from students s 
          inner join (
            select e.student_id, MAX(e.score ) as max_sc
            from enrollments e 
            group by student_id
          ) as T on s.id = T.student_id 
          """)) {
        while (rs.next()) {
          System.out.printf("  %s 最高分 %.1f分%n",
              rs.getString("name"), rs.getDouble("score"));
        }
      }

      // EXISTS 子查询 — 有成绩的学生
      System.out.println("\n有考试成绩的学生（EXISTS）:");
      try (ResultSet rs = stmt.executeQuery("""
          SELECT name FROM student s
          WHERE EXISTS (SELECT 1 FROM score sc WHERE sc.student_id = s.id)
          """)) {
        while (rs.next()) {
          System.out.println("  " + rs.getString("name"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void indexDemo() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // 创建索引
      stmt.execute("CREATE INDEX idx_student_name ON student(name)");
      stmt.execute("CREATE INDEX idx_score_student ON score(student_id)");
      // 联合索引
      stmt.execute("CREATE INDEX idx_score_stu_sub ON score(student_id, subject)");

      System.out.println("创建索引: idx_student_name, idx_score_student, idx_score_stu_sub(联合索引)");
      System.out.println("\n索引失效场景（面试高频！）：");
      System.out.println("  1. WHERE中使用函数: WHERE UPPER(name) = '张三' → 索引失效");
      System.out.println("  2. 前置模糊查询: WHERE name LIKE '%三' → 索引失效");
      System.out.println("  3. 隐式类型转换: WHERE phone = 13800138000 (phone是varchar) → 索引失效");
      System.out.println("  4. 联合索引不满足最左前缀: idx(a,b,c) 跳过a直接用b → 索引失效");
      System.out.println("  5. OR连接非索引列: WHERE a=1 OR b=2 (b无索引) → 索引失效");
      System.out.println("  6. NOT IN / != / <> → 可能不走索引");

      System.out.println("\n索引使用 EXPLAIN 分析（查看执行计划）:");
      try (ResultSet rs = stmt.executeQuery(
          "EXPLAIN SELECT * FROM student WHERE name = '张三'")) {
        while (rs.next()) {
          System.out.println("  " + rs.getString(1));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  static void isolationDemo() {
    System.out.println("事务隔离级别（从低到高）：");
    System.out.println("┌──────────────┬──────┬──────────┬──────┐");
    System.out.println("│ 隔离级别      │ 脏读 │ 不可重复读│ 幻读 │");
    System.out.println("├──────────────┼──────┼──────────┼──────┤");
    System.out.println("│ READ UNCOMMITTED│ ✓  │    ✓     │  ✓  │");
    System.out.println("│ READ COMMITTED  │ ✗  │    ✓     │  ✓  │");
    System.out.println("│ REPEATABLE READ │ ✗  │    ✗     │  ✓  │");
    System.out.println("│ SERIALIZABLE    │ ✗  │    ✗     │  ✗  │");
    System.out.println("└──────────────┴──────┴──────────┴──────┘");
    System.out.println("MySQL默认REPEATABLE READ，Oracle/PostgreSQL默认READ COMMITTED");

    System.out.println("\n脏读: 读到未提交事务的数据（可能回滚）");
    System.out.println("不可重复读: 同一事务两次读取结果不同（另一事务UPDATE）");
    System.out.println("幻读: 同一事务两次查询结果行数不同（另一事务INSERT/DELETE）");
  }

  static void sqlTrapDemo() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement()) {

      // 陷阱1：NOT IN + NULL
      System.out.println("陷阱1: NOT IN 遇到 NULL");
      try (ResultSet rs = stmt.executeQuery(
          "SELECT * FROM student WHERE class_id NOT IN (2, NULL)")) {
        System.out.println("  结果: " + (rs.next() ? "有数据" : "空！"));
        System.out.println("  原因: NOT IN中有NULL，整个条件变为NULL");
      }

      // 陷阱2：COUNT
      System.out.println("\n陷阱2: COUNT(*) vs COUNT(列名)");
      try (ResultSet rs = stmt.executeQuery(
          "SELECT COUNT(*) AS c1, COUNT(class_id) AS c2 FROM student")) {
        rs.next();
        System.out.println("  COUNT(*): " + rs.getInt("c1") + " (所有行)");
        System.out.println("  COUNT(class_id): " + rs.getInt("c2") + " (class_id非NULL的行，孙七的class_id是NULL)");
      }

      // 陷阱3：WHERE vs HAVING
      System.out.println("\n陷阱3: WHERE 不能跟聚合函数，HAVING 可以");
      System.out.println("  正确: SELECT ... WHERE score > 80"); // 过滤行
      System.out.println("  正确: SELECT ... HAVING AVG(score) > 80"); // 过滤分组
      System.out.println("  错误: SELECT ... WHERE AVG(score) > 80"); // WHERE不能跟聚合函数
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
