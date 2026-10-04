package com.erp.servlet;

import com.erp.dao.PermissionDao;
import com.erp.dao.UserDao;
import com.erp.entity.Permission;
import com.erp.entity.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 登录Servlet
 * 登录成功后加载用户权限信息到Session，支持RBAC动态菜单
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        UserDao dao = new UserDao();
        User user = dao.login(username, password);

        if (user != null) {
            // 登录成功，把用户信息存入 Session
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", user);

            // 【新增】加载用户权限编码集合到Session（用于按钮级权限判断）
            PermissionDao permDao = new PermissionDao();
            Set<String> permissions = permDao.findPermissionCodesByUserId(user.getId());
            session.setAttribute("_USER_PERMISSIONS", permissions);

            // 【新增】加载用户菜单列表到Session（用于动态侧边栏导航）
            List<Permission> menus = permDao.findMenusByUserId(user.getId());
            session.setAttribute("userMenus", menus);

            // 根据角色跳转到默认页面
            if ("ADMIN".equals(user.getRoleCode())) {
                resp.sendRedirect(req.getContextPath() + "/product");
            } else if ("PURCHASER".equals(user.getRoleCode())) {
                resp.sendRedirect(req.getContextPath() + "/purchase_request");
            } else {
                // WAREHOUSE（库管员）只有「采购管理」菜单权限，跳到采购订单页
                resp.sendRedirect(req.getContextPath() + "/purchase_order");
            }
        } else {
            // 登录失败，跳回登录页并带上错误信息
            req.setAttribute("msg", "用户名或密码错误！");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}