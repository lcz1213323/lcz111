package com.erp.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取 Session 并销毁它
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 重定向回登录页
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}