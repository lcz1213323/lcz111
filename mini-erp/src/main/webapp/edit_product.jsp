<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>编辑商品 - Mini ERP</title>
    <style>
        .form-box { width: 50%; margin: 0 auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
        input { width: 100%; padding: 8px; margin: 5px 0 15px 0; box-sizing: border-box; }
        button { padding: 10px 20px; background-color: #ffc107; color: black; border: none; cursor: pointer; font-weight: bold; }
        a { margin-left: 15px; text-decoration: none; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">编辑商品</h2>
<div class="form-box">
    <form action="${pageContext.request.contextPath}/product" method="post">
        <input type="hidden" name="id" value="${product.id}">
        商品编码: <input type="text" name="productCode" value="${product.productCode}" required>
        商品名称: <input type="text" name="productName" value="${product.productName}" required>
        分类: <input type="text" name="category" value="${product.category}">
        单位: <input type="text" name="unit" value="${product.unit}">
        参考采购价: <input type="number" step="0.01" name="purchasePrice" value="${product.purchasePrice}">
        参考销售价: <input type="number" step="0.01" name="salePrice" value="${product.salePrice}">
        库存: <input type="number" name="stockQty" value="${product.stockQty}">
        <button type="submit">保存修改</button>
        <a href="${pageContext.request.contextPath}/product">返回列表</a>
    </form>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>