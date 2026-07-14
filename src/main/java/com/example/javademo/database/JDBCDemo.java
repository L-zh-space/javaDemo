package com.example.javademo.database;

import java.sql.*;

/**
 * 原生JDBC操作 — 不用框架，手写数据库连接和CRUD
 *
 * 【面试常问】
 * - JDBC连接步骤：加载驱动 → 获取连接 → 创建Statement → 执行SQL → 处理结果 → 关闭资源
 * - Statement vs PreparedStatement：后者防SQL注入、预编译性能好
 * - 事务：connection.setAutoCommit(false) → 执行SQL → commit/rollback
 * - 连接池：复用连接，避免频繁创建销毁（Druid/HikariCP）
 *
 * 【常见坑】
 * - 资源必须在finally中关闭（或用try-with-resources）
 * - 忘记关闭连接导致连接池泄漏
 * - ResultSet 不能重复遍历
 * - 拼接SQL有注入风险 → 必须用PreparedStatement
 */
public class JDBCDemo {

    // H2内存数据库 — 无需安装，进程结束数据消失
    private static final String JDBC_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        System.out.println("========== 1. JDBC连接步骤 ==========");
        loadDriver();

        System.out.println("\n========== 2. 建表 ==========");
        createTable();

        System.out.println("\n========== 3. CRUD操作 ==========");
        insertData();
        queryData();
        updateData();
        deleteData();

        System.out.println("\n========== 4. PreparedStatement 防SQL注入 ==========");
        sqlInjectionDemo();

        System.out.println("\n========== 5. 事务 ==========");
        transactionDemo();

        System.out.println("\n========== 6. 批量操作 ==========");
        batchDemo();
    }

    // 步骤1：加载驱动（JDBC4+ 自动加载，这行可省略但面试要会讲）
    static void loadDriver() {
        try {
            // H2的驱动类
            Class.forName("org.h2.Driver");
            System.out.println("1. 驱动加载成功: org.h2.Driver");
            System.out.println("2. 通过 DriverManager.getConnection() 获取连接");
            System.out.println("3. 创建 Statement / PreparedStatement");
            System.out.println("4. 执行 executeQuery() / executeUpdate()");
            System.out.println("5. 处理 ResultSet");
            System.out.println("6. 关闭连接（try-with-resources自动管理）");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS student (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    age INT NOT NULL,
                    score DECIMAL(5,2) DEFAULT 0,
                    class_name VARCHAR(20)
                )
                """;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("student 表创建成功（id, name, age, score, class_name）");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void insertData() {
        String sql = "INSERT INTO student (name, age, score, class_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 添加第1条
            pstmt.setString(1, "张三");
            pstmt.setInt(2, 20);
            pstmt.setDouble(3, 85.5);
            pstmt.setString(4, "一班");
            pstmt.executeUpdate();

            // 添加第2条
            pstmt.setString(1, "李四");
            pstmt.setInt(2, 22);
            pstmt.setDouble(3, 92.0);
            pstmt.setString(4, "一班");
            pstmt.executeUpdate();

            // 添加第3条
            pstmt.setString(1, "王五");
            pstmt.setInt(2, 21);
            pstmt.setDouble(3, 76.5);
            pstmt.setString(4, "二班");
            pstmt.executeUpdate();

            // 添加第4条
            pstmt.setString(1, "赵六");
            pstmt.setInt(2, 23);
            pstmt.setDouble(3, 88.0);
            pstmt.setString(4, "二班");
            pstmt.executeUpdate();

            System.out.println("插入4条数据成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void queryData() {
        String sql = "SELECT * FROM student ORDER BY score DESC";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("学生列表（按成绩降序）：");
            System.out.println("ID\t姓名\t年龄\t成绩\t班级");
            System.out.println("────────────────────────────────");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double score = rs.getDouble("score");
                String className = rs.getString("class_name");
                System.out.printf("%d\t%s\t%d\t%.1f\t%s%n", id, name, age, score, className);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void updateData() {
        String sql = "UPDATE student SET score = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, 95.0);
            pstmt.setString(2, "张三");
            int rows = pstmt.executeUpdate();
            System.out.println("更新了 " + rows + " 行（张三成绩改为95）");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void deleteData() {
        String sql = "DELETE FROM student WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "赵六");
            int rows = pstmt.executeUpdate();
            System.out.println("删除了 " + rows + " 行（删除赵六）");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==== SQL注入演示 ====
    static void sqlInjectionDemo() {
        // 模拟用户输入的恶意参数
        String evilInput = "张三' OR '1'='1";  // 经典SQL注入

        // ❌ 危险做法：拼接SQL（同学！你面试时千万不要这么写！）
        System.out.println("恶意输入: " + evilInput);
        String dangerousSql = "SELECT * FROM student WHERE name = '" + evilInput + "'";
        System.out.println("拼接后的SQL: " + dangerousSql);
        System.out.println("→ WHERE条件恒为真，会查出所有数据！");

        // ✅ 安全做法：PreparedStatement 参数化查询
        String safeSql = "SELECT * FROM student WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(safeSql)) {
            pstmt.setString(1, evilInput);  // 参数会被自动转义
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = rs.next();
                System.out.println("PreparedStatement 查询结果数: " + (found ? "0（安全，注入失败）" : "0（安全）"));
                System.out.println("原因: ? 占位符的值被当作数据而非SQL语句执行");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==== 事务演示 ====
    static void transactionDemo() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            // 关闭自动提交（默认每条SQL自动提交）
            conn.setAutoCommit(false);

            // 操作1：扣张三10分
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE student SET score = score - 10 WHERE name = ?")) {
                pstmt.setString(1, "张三");
                pstmt.executeUpdate();
                System.out.println("事务-操作1: 张三扣10分");
            }

            // 操作2：（模拟异常情况）给不存在的同学加分
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE student SET score = score + 10 WHERE name = ?")) {
                pstmt.setString(1, "不存在的同学");
                pstmt.executeUpdate();
                System.out.println("事务-操作2: 加分（影响0行但不算错误）");
            }

            // 全部成功 → 提交
            conn.commit();
            System.out.println("事务提交成功，所有操作生效");

            // 查看结果
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT name, score FROM student WHERE name = '张三'");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("张三当前成绩: " + rs.getDouble("score"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("发生异常，事务回滚！");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void batchDemo() {
        String sql = "INSERT INTO student (name, age, score, class_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < 5; i++) {
                pstmt.setString(1, "批量学生" + i);
                pstmt.setInt(2, 20 + i);
                pstmt.setDouble(3, 70 + i * 5);
                pstmt.setString(4, "三班");
                pstmt.addBatch();  // 加入批次
            }
            int[] results = pstmt.executeBatch();  // 一次性发送
            System.out.println("批量插入5条数据，影响行数: " + results.length);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
