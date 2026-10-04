<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>新增商品 - Mini ERP</title>
    <style>
        .form-box { width: 50%; margin: 0 auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
        input { width: 100%; padding: 8px; margin: 5px 0 15px 0; box-sizing: border-box; }
        button { padding: 10px 20px; background-color: #28a745; color: white; border: none; cursor: pointer; }
        a { margin-left: 15px; text-decoration: none; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">新增商品</h2>
<div class="form-box">
    <form action="${pageContext.request.contextPath}/product" method="post">
        商品编码: <input type="text" name="productCode" required>
        商品名称: <input type="text" name="productName" required>
        分类: <input type="text" name="category">
        单位: <input type="text" name="unit" value="个">
        参考采购价: <input type="number" step="0.01" name="purchasePrice" value="0.00">
        参考销售价: <input type="number" step="0.01" name="salePrice" value="0.00">
        初始库存: <input type="number" name="stockQty" value="0">
        <button type="submit">提交保存</button>
        <a href="${pageContext.request.contextPath}/product">返回列表</a>
    </form>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>