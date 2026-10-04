<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="perm" uri="http://erp.com/tags" %>
<html>
<head>
    <title>商品管理 - Mini ERP</title>
    <style>
        table { border-collapse: collapse; width: 100%; margin: 0 auto; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }
        th { background-color: #f2f2f2; }
        .btn { padding: 5px 10px; background-color: #007bff; color: white;
               text-decoration: none; border-radius: 4px; margin: 0 5px; }
        .btn-purchase { background-color: #28a745; }
        .btn-edit { color: #007bff; text-decoration: none; }
        .btn-del { color: red; text-decoration: none; }
        .action-bar { margin-bottom: 20px; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">商品管理</h2>

<div class="action-bar" style="text-align: center;">
    <!-- RBAC按钮级权限控制：替代硬编码的 c:if roleCode == 'ADMIN' -->
    <perm:check code="product:add">
        <a class="btn" href="${pageContext.request.contextPath}/add_product.jsp">新增商品</a>
    </perm:check>

    <perm:check code="menu:purchase">
        <a class="btn btn-purchase" href="${pageContext.request.contextPath}/purchase_order">进入采购管理</a>
    </perm:check>

    <perm:check code="purchase:request">
        <a class="btn btn-purchase" href="${pageContext.request.contextPath}/purchase_request">采购申请</a>
    </perm:check>
</div>

<table>
    <tr>
        <th>ID</th>
        <th>商品编码</th>
        <th>商品名称</th>
        <th>分类</th>
        <th>单位</th>
        <th>库存</th>
        <th>销售价</th>
        <!-- 按钮级权限：只有有编辑或删除权限的人才看到操作列 -->
        <perm:check code="product:edit">
            <th>操作</th>
        </perm:check>
        <perm:check code="product:delete">
            <th>操作</th>
        </perm:check>
    </tr>
    <c:forEach items="${productList}" var="p">
        <tr>
            <td>${p.id}</td>
            <td>${p.productCode}</td>
            <td>${p.productName}</td>
            <td>${p.category}</td>
            <td>${p.unit}</td>
            <td>${p.stockQty}</td>
            <td>${p.salePrice}</td>
            <td>
                <perm:check code="product:edit">
                    <a class="btn-edit" href="${pageContext.request.contextPath}/product?action=edit&id=${p.id}">编辑</a>
                </perm:check>
                <perm:check code="product:delete">
                    | <a class="btn-del" href="${pageContext.request.contextPath}/product?action=delete&id=${p.id}"
                         onclick="return confirm('确定要删除这个商品吗？');">删除</a>
                </perm:check>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty productList}">
        <tr>
            <td colspan="8" style="color: red;">暂无商品数据，请先添加！</td>
        </tr>
    </c:if>
</table>

<%@ include file="footer.jsp" %>
</body>
</html>