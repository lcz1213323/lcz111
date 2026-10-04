package com.erp.dao;

import com.erp.entity.Permission;
import com.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 权限数据访问层
 * 负责菜单动态加载、按钮权限判断、角色权限分配
 */
public class PermissionDao {

    /**
     * 根据用户ID查询其拥有的所有权限编码（用于按钮级权限判断）
     *
     * EXPLAIN分析:
     * SELECT DISTINCT p.permission_code FROM sys_permission p
     * INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
     * INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
     * WHERE ur.user_id = ?
     *
     * 优化前: 无此查询，权限判断在JSP中通过硬编码角色字符串实现
     * 优化后: 使用索引 sys_user_role(idx_user_id),
     *         sys_role_permission(uk_role_perm), sys_permission(PRIMARY)
     * 预计扫描行数 &lt; 50行，查询效率极高
     */
    public Set<String> findPermissionCodesByUserId(Long userId) {
        Set<String> codes = new HashSet<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT DISTINCT p.permission_code FROM sys_permission p " +
                    "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
                    "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
                    "WHERE ur.user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                codes.add(rs.getString("permission_code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return codes;
    }

    /**
     * 根据用户ID查询其拥有的菜单权限（menu_type=1），按sort_order排序
     * 用于动态生成侧边栏导航
     *
     * EXPLAIN分析:
     * SELECT DISTINCT p.* FROM sys_permission p
     * INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
     * INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
     * WHERE ur.user_id = ? AND p.menu_type = 1
     * ORDER BY p.sort_order
     *
     * 使用索引: sys_user_role(idx_user_id), sys_role_permission(uk_role_perm)
     *           sys_permission(PRIMARY, idx_menu_type)
     */
    public List<Permission> findMenusByUserId(Long userId) {
        List<Permission> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT DISTINCT p.id, p.permission_code, p.permission_name, " +
                    "p.url, p.parent_id, p.menu_type, p.icon, p.sort_order " +
                    "FROM sys_permission p " +
                    "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
                    "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
                    "WHERE ur.user_id = ? AND p.menu_type = 1 " +
                    "ORDER BY p.sort_order ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Permission p = new Permission();
                p.setId(rs.getLong("id"));
                p.setPermissionCode(rs.getString("permission_code"));
                p.setPermissionName(rs.getString("permission_name"));
                p.setUrl(rs.getString("url"));
                p.setParentId(rs.getLong("parent_id"));
                p.setMenuType(rs.getInt("menu_type"));
                p.setIcon(rs.getString("icon"));
                p.setSortOrder(rs.getInt("sort_order"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * 查询所有权限（管理端使用）
     */
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM sys_permission ORDER BY sort_order ASC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Permission p = new Permission();
                p.setId(rs.getLong("id"));
                p.setPermissionCode(rs.getString("permission_code"));
                p.setPermissionName(rs.getString("permission_name"));
                p.setUrl(rs.getString("url"));
                p.setParentId(rs.getLong("parent_id"));
                p.setMenuType(rs.getInt("menu_type"));
                p.setIcon(rs.getString("icon"));
                p.setSortOrder(rs.getInt("sort_order"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return list;
    }
}