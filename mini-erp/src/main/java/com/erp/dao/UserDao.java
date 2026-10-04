package com.erp.dao;

import com.erp.entity.User;
import com.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    // 根据用户名和密码查询用户，并连带查出角色信息
    public User login(String username, String password) {
        User user = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 多表联查：用户表 关联 用户角色关联表 关联 角色表
            String sql = "SELECT u.id, u.username, u.real_name, u.status, r.role_code, r.role_name " +
                    "FROM sys_user u " +
                    "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
                    "LEFT JOIN sys_role r ON ur.role_id = r.id " +
                    "WHERE u.username = ? AND u.password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setRealName(rs.getString("real_name"));
                user.setStatus(rs.getInt("status"));
                user.setRoleCode(rs.getString("role_code"));
                user.setRoleName(rs.getString("role_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return user;
    }
}