package com.erp.util;

import com.erp.dao.PermissionDao;
import com.erp.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限判断工具类
 * 提供 Session 级别的权限缓存，避免每次请求都查询数据库
 */
public class PermissionUtil {

    private static final String PERM_SESSION_KEY = "_USER_PERMISSIONS";

    /**
     * 从Session获取用户权限码集合（首次加载后缓存）
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getUserPermissions(HttpSession session) {
        if (session == null) return Collections.emptySet();
        Set<String> permissions = (Set<String>) session.getAttribute(PERM_SESSION_KEY);
        if (permissions == null) {
            User user = (User) session.getAttribute("currentUser");
            if (user != null) {
                PermissionDao dao = new PermissionDao();
                permissions = dao.findPermissionCodesByUserId(user.getId());
                session.setAttribute(PERM_SESSION_KEY, permissions);
            } else {
                permissions = Collections.emptySet();
            }
        }
        return permissions != null ? permissions : Collections.emptySet();
    }

    /**
     * 检查用户是否拥有指定权限编码
     */
    public static boolean hasPermission(HttpSession session, String permissionCode) {
        Set<String> permissions = getUserPermissions(session);
        return permissions.contains(permissionCode);
    }

    /**
     * 检查用户是否拥有任一权限编码（满足一个即可）
     */
    public static boolean hasAnyPermission(HttpSession session, String... permissionCodes) {
        Set<String> permissions = getUserPermissions(session);
        for (String code : permissionCodes) {
            if (permissions.contains(code)) return true;
        }
        return false;
    }

    /**
     * 根据请求的Servlet路径判断是否有权访问（匹配权限表中的url字段）
     * 返回true表示有权访问或无需检查
     */
    public static boolean checkUrlPermission(String servletPath, Set<String> permissions) {
        // 无权限数据时不拦截（兼容权限表为空的情况）
        if (permissions == null || permissions.isEmpty()) return true;

        // URL到菜单权限编码的映射
        if (servletPath.startsWith("/product") && !permissions.contains("menu:product")) {
            return false;
        }
        if (servletPath.startsWith("/purchase_order") && !permissions.contains("menu:purchase")) {
            return false;
        }
        if (servletPath.startsWith("/purchase_request") && !permissions.contains("menu:request")) {
            return false;
        }
        return true;
    }

    /**
     * 刷新权限缓存（角色变更后调用）
     */
    public static void refreshPermissions(HttpSession session) {
        if (session != null) {
            session.removeAttribute(PERM_SESSION_KEY);
        }
    }
}