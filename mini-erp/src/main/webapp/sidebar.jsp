<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="perm" uri="http://erp.com/tags" %>

<style>
    .erp-layout { display: flex; min-height: 100vh; }
    .sidebar {
        width: 220px; background: #2c3e50; color: #ecf0f1;
        padding: 0; flex-shrink: 0;
    }
    .sidebar .logo { padding: 20px; text-align: center; font-size: 20px; font-weight: bold;
        border-bottom: 1px solid #34495e; background: #1a252f; }
    .sidebar .menu { list-style: none; padding: 0; margin: 0; }
    .sidebar .menu li { border-bottom: 1px solid #34495e; }
    .sidebar .menu li a {
        display: block; padding: 14px 20px; color: #ecf0f1;
        text-decoration: none; font-size: 15px; transition: background 0.2s;
    }
    .sidebar .menu li a:hover { background: #34495e; }
    .sidebar .menu li a.active { background: #3498db; font-weight: bold; }
    .main-content { flex: 1; padding: 0; background: #f5f6fa; }
    .top-bar {
        background: white; padding: 12px 30px; text-align: right;
        font-size: 14px; border-bottom: 1px solid #ddd; box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .top-bar b { color: #2c3e50; }
    .top-bar a { color: #e74c3c; text-decoration: none; margin-left: 10px; }
    .content-area { padding: 30px; }
</style>

<div class="erp-layout">
    <!-- 左侧动态菜单 -->
    <div class="sidebar">
        <div class="logo">🏭 Mini ERP</div>
        <ul class="menu">
            <c:forEach items="${userMenus}" var="menu">
                <li>
                    <a href="${pageContext.request.contextPath}${menu.url}"
                       class="${pageContext.request.servletPath == menu.url ? 'active' : ''}">
                        ${menu.icon} ${menu.permissionName}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-content">
        <div class="top-bar">
            欢迎您，<b>${currentUser.realName}</b> (角色: ${currentUser.roleName})
            <a href="${pageContext.request.contextPath}/logout">【退出登录】</a>
        </div>
        <div class="content-area">