<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>新建采购订单 - Mini ERP</title>
    <style>
        .container { margin: 0 auto; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }
        th { background-color: #f2f2f2; }
        input, select { padding: 5px; }
        .btn { padding: 8px 15px; background-color: #28a745; color: white; border: none; cursor: pointer; text-decoration: none; }
        .btn-back { background-color: #6c757d; }
        .error-msg { color: red; font-weight: bold; text-align: center; margin-top: 10px; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">新建采购订单</h2>
<p class="error-msg">${errorMsg}</p>

<div class="container">
    <form action="${pageContext.request.contextPath}/purchase_order" method="post">
        <input type="hidden" name="requestId" value="${requestId}">
        <div style="margin-bottom: 20px;">
            <label>供应商：</label>
            <select name="supplierId" required>
                <option value="">请选择供应商</option>
                <c:forEach items="${supplierList}" var="s">
                    <option value="${s.id}">${s.name}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;&nbsp;
            <label>订单日期：</label>
            <input type="date" name="orderDate" required>
        </div>

        <c:choose>
            <c:when test="${not empty requestId}">
                <!-- 从采购申请生成订单：商品/数量来自申请，仅需确认单价 -->
                <table>
                    <tr><th>商品名称</th><th>申请数量</th><th>单价</th></tr>
                    <tr>
                        <td>${convertRequest.productName}</td>
                        <td>${convertRequest.quantity}</td>
                        <td><input type="number" step="0.01" name="unitPrice" value="${convertRequest.estimatedPrice}"></td>
                    </tr>
                </table>
            </c:when>
            <c:otherwise>
                <!-- 手动新建订单：多行商品明细 -->
                <table id="detailTable">
                    <tr>
                        <th>商品名称（自动带出单价）</th>
                        <th>单价</th>
                        <th>数量</th>
                    </tr>
                    <tr>
                        <td>
                            <select name="productId" onchange="fillPrice(this)">
                                <option value="">请选择商品</option>
                                <c:forEach items="${productList}" var="p">
                                    <option value="${p.id}" data-price="${p.purchasePrice}">${p.productName} (库存:${p.stockQty})</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td><input type="number" step="0.01" name="unitPrice" value="0.00"></td>
                        <td><input type="number" name="quantity" value="1"></td>
                    </tr>
                    <tr>
                        <td>
                            <select name="productId" onchange="fillPrice(this)">
                                <option value="">请选择商品</option>
                                <c:forEach items="${productList}" var="p">
                                    <option value="${p.id}" data-price="${p.purchasePrice}">${p.productName} (库存:${p.stockQty})</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td><input type="number" step="0.01" name="unitPrice" value="0.00"></td>
                        <td><input type="number" name="quantity" value="1"></td>
                    </tr>
                </table>
            </c:otherwise>
        </c:choose>

        <div style="margin-top: 20px; text-align: center;">
            <button type="submit" class="btn">保存订单</button>
            <a href="${pageContext.request.contextPath}/purchase_order" class="btn btn-back">返回列表</a>
        </div>
    </form>
</div>

<script>
    function fillPrice(selectObj) {
        var price = selectObj.options[selectObj.selectedIndex].getAttribute('data-price');
        if (price) {
            selectObj.parentElement.nextElementSibling.querySelector('input').value = price;
        }
    }
</script>

<%@ include file="footer.jsp" %>
</body>
</html>