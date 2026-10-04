<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>新建采购申请 - Mini ERP</title>
    <style>
        .form-box { width: 60%; margin: 0 auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
        input, select, textarea { width: 100%; padding: 8px; margin: 5px 0 15px 0; box-sizing: border-box; }
        button { padding: 10px 20px; background-color: #28a745; color: white; border: none; cursor: pointer; }
        a { margin-left: 15px; text-decoration: none; }
        .error { color: red; font-weight: bold; text-align: center; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">新建采购申请</h2>
<p class="error">${errorMsg}</p>

<div class="form-box">
    <form action="${pageContext.request.contextPath}/purchase_request" method="post">
        <label>申请商品：</label>
        <select name="productId" onchange="fillPrice(this)" required>
            <option value="">请选择商品</option>
            <c:forEach items="${productList}" var="p">
                <option value="${p.id}" data-price="${p.purchasePrice}">${p.productName} (库存:${p.stockQty})</option>
            </c:forEach>
        </select>

        <label>申请数量：</label>
        <input type="number" name="quantity" value="1" min="1" required>

        <label>预估单价（自动填入采购价，可修改）：</label>
        <input type="number" step="0.01" name="estimatedPrice" value="0.00" required>

        <label>申请原因：</label>
        <textarea name="reason" rows="3" placeholder="请简要说明采购原因"></textarea>

        <div style="text-align: center;">
            <button type="submit">提交申请</button>
            <a href="${pageContext.request.contextPath}/purchase_request">返回列表</a>
        </div>
    </form>
</div>

<script>
    function fillPrice(selectObj) {
        var price = selectObj.options[selectObj.selectedIndex].getAttribute('data-price');
        if (price) {
            selectObj.closest('.form-box').querySelector('input[name="estimatedPrice"]').value = price;
        }
    }
</script>

<%@ include file="footer.jsp" %>
</body>
</html>