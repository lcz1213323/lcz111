package com.erp.filter;

import com.erp.entity.User;
import com.erp.util.PermissionUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

/**
 * 认证与授权拦截器
 * 1. 放行登录页面和登录接口
 * 2. 放行静态资源（CSS/JS/图片）
 * 3. 检查 Session 中的用户信息（认证）
 * 4. 检查 URL 级权限（授权）—— 基于RBAC权限码
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        String uri = httpReq.getRequestURI();
        String contextPath = httpReq.getContextPath();

        // 1. 放行静态资源
        if (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".gif") || uri.endsWith(".ico")) {
            chain.doFilter(req, resp);
            return;
        }

        // 2. 放行登录页面和登录接口（如果不放行，用户永远无法登录）
        if (uri.endsWith("/login.jsp") || uri.endsWith("/login")) {
            chain.doFilter(req, resp);
            return;
        }

        // 3. 检查 Session 中是否有用户信息（认证检查）
        HttpSession session = httpReq.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            // 未登录，强制跳转登录页
            httpResp.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        // 4. URL级权限检查（授权检查 —— RBAC核心）
        User user = (User) session.getAttribute("currentUser");
        Set<String> permissions = PermissionUtil.getUserPermissions(session);
        String servletPath = httpReq.getServletPath();

        // 管理员跳过URL权限检查（拥有所有权限）
        if (!"ADMIN".equals(user.getRoleCode())) {
            if (!PermissionUtil.checkUrlPermission(servletPath, permissions)) {
                // 无权限访问，返回403错误。
                // 注意：sendError 的 reason 短语会进入 HTTP 状态行，只能是 ASCII，
                // 中文会被 Jetty 用 ISO-8859-1 编码成乱码，故这里手动输出 UTF-8 页面。
                httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResp.setCharacterEncoding("UTF-8");
                httpResp.setContentType("text/html;charset=UTF-8");
                httpResp.getWriter().write("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                        + "<body><h3>HTTP ERROR 403</h3><p>您没有权限访问此资源</p></body></html>");
                return;
            }
        }

        // 认证和授权都通过，放行
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {}
}