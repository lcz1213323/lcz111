<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录 - Mini ERP</title>
    <style>
        .login-box { width: 300px; margin: 100px auto; padding: 30px; border: 1px solid #ccc; border-radius: 5px; text-align: center; }
        input { width: 90%; padding: 8px; margin: 10px 0; }
        button { width: 95%; padding: 10px; background-color: #007bff; color: white; border: none; cursor: pointer; }
        .error { color: red; }
    </style>
</head>
<body>
    <div class="login-box">
        <h2>Mini ERP 系统登录</h2>
        <p class="error">${msg}</p>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <input type="text" name="username" placeholder="用户名" required>
            <input type="password" name="password" placeholder="密码" required>
            <button type="submit">登 录</button>
        </form>
    </div>
</body>
</html>