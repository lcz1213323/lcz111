package com.erp.util;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try {
            // 尝试获取连接
            Connection conn = DBUtil.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ 数据库连接成功！");
                // 用完后关闭连接
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("❌ 数据库连接失败！");
            // 打印详细的错误信息，方便我们排查
            e.printStackTrace();
        }
    }
}