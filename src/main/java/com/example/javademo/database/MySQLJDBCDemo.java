package com.example.javademo.database;

import java.sql.*;

/**
 * MySQL 8.0 以上版本的数据库连接有所不同：
 * 
 * 1、MySQL 8.0 以上版本驱动包版本 mysql-connector-java-8.0.16.jar。
 * 
 * 2、com.mysql.jdbc.Driver 更换为 com.mysql.cj.jdbc.Driver。
 * 
 * 3、MySQL 8.0 以上版本不需要建立 SSL 连接的，需要显示关闭。
 * 
 * 4、allowPublicKeyRetrieval=true 允许客户端从服务器获取公钥。
 * 
 * 5、最后还需要设置 CST。
 * 
 * MySQLJDBCDemo
 */

public class MySQLJDBCDemo {

  // 驱动名
  static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  // 数据库URL（IP地址/数据库名/连接配置）
  static final String DB_URL = "jdbc:mysql://localhost:3306/edu_platform_mysql_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  // 数据库的用户名与密码
  static final String USER = "root";
  static final String PASS = "315689";

  public static void main(String[] args) {
    System.out.println("========== 1. 加载驱动 ==========");
    loadDriver();
    System.out.println("\n========== CRUD操作 ==========");
    queryData();

  }

  static void loadDriver() {
    // 加载驱动
    // 使用 try- catch 捕捉错误并避免进程中断
    try {
      Class.forName(JDBC_DRIVER);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  static void queryData() {
    try {
      System.out.println("========== 2. 获取链接，创建数据库操作对象 ==========");
      Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
      Statement stat = conn.createStatement();
      System.out.println("========== 3. 编写sql ==========");
      String sql = "SELECT * FROM students ORDER BY stu_no DESC";
      System.out.println("========== 4. 使用stat执行sql ==========");
      stat.executeQuery(sql);
      System.out.println("========== 4. 处理stat中的结果 ==========");
      ResultSet res = stat.getResultSet();
      while (res.next()) {
        System.out.println(res.getInt("id"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
