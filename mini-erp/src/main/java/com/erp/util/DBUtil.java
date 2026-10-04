package com.erp.util;

import java.sql.*;

/**
 * JDBC 工具类 - 管理 mini_erp 数据库的连接与资源释放
 */
public class DBUtil {

    // 数据库连接 URL，已带上常用参数
    private static final String URL = "jdbc:mysql://localhost:3306/mini_erp"
            + "?useUnicode=true"
            + "&characterEncoding=utf-8"
            + "&useSSL=false"
            + "&serverTimezone=Asia/Shanghai"
            + "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    // ⚠️ 千万注意：把下面的 YOUR_PASSWORD 换成你真实的 MySQL 密码！！！
    private static final String PASSWORD = "lczimxy233";

    // 静态代码块：加载 MySQL 驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC 驱动加载失败: " + e.getMessage());
        }
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 安静关闭 JDBC 资源
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}