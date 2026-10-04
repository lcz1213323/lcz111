<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="perm" uri="http://erp.com/tags" %>
<html>
<head>
    <title>采购订单 - Mini ERP</title>
    <style>
        table { border-collapse: collapse; width: 100%; margin: 0 auto; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }
        th { background-color: #f2f2f2; }
        .btn { padding: 5px 10px; background-color: #007bff; color: white;
               text-decoration: none; border-radius: 4px; }
        .status-DRAFT { color: gray; font-weight: bold; }
        .status-APPROVED { color: #007bff; font-weight: bold; }
        .status-RECEIVING { color: orange; font-weight: bold; }
        .status-COMPLETED { color: green; font-weight: bold; }
        .status-CANCELLED { color: red; font-weight: bold; }
        .btn-del { color: red; text-decoration: none; }
        .btn-approve { color: #007bff; text-decoration: none; font-weight: bold; }
        .btn-receive { color: #28a745; text-decoration: none; font-weight: bold; }
        .action-bar { margin-bottom: 20px; text-align: center; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">采购订单管理</h2>

<div class="action-bar">
    <perm:check code="purchase:order">
        <a class="btn" href="${pageContext.request.contextPath}/purchase_order?action=new">新建采购订单</a>
    </perm:check>
    <perm:check code="purchase:request">
        <a class="btn" href="${pageContext.request.contextPath}/purchase_request" style="background-color: #6c757d;">采购申请</a>
    </perm:check>
    <perm:check code="menu:product">
        <a class="btn" href="${pageContext.request.contextPath}/product" style="background-color: #6c757d;">返回商品管理</a>
    </perm:check>
</div>

<table>
    <tr>
        <th>订单编号</th>
        <th>供应商</th>
        <th>订单日期</th>
        <th>总金额</th>
        <th>状态</th>
        <th>创建人</th>
        <th>创建时间</th>
        <th>入库时间</th>
        <th>操作</th>
    </tr>
    <c:forEach items="${orderList}" var="order">
        <tr>
            <td>${order.orderNo}</td>
            <td>${order.supplierName}</td>
            <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd"/></td>
            <td>¥ <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00"/></td>
            <td class="status-${order.status}">${order.status}</td>
            <td>${order.createdByName}</td>
            <td><fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td>
                <c:if test="${not empty order.receivedAt}">
                    <fmt:formatDate value="${order.receivedAt}" pattern="yyyy-MM-dd HH:mm"/>
                </c:if>
            </td>
            <td>
                <!-- DRAFT状态 + 有审批权限 → 显示审批按钮 -->
                <c:if test="${order.status == 'DRAFT'}">
                    <perm:check code="purchase:approve">
                        <a class="btn-approve" href="${pageContext.request.contextPath}/purchase_order?action=approve&id=${order.id}"
                           onclick="return confirm('确认审批通过吗？审批后订单状态变为已审批，等待入库。');">审批通过</a> |
                    </perm:check>
                </c:if>

                <!-- APPROVED/RECEIVING状态 + 有入库权限 → 显示入库确认按钮 -->
                <c:if test="${order.status == 'APPROVED' || order.status == 'RECEIVING'}">
                    <perm:check code="purchase:receive">
                        <a class="btn-receive" href="${pageContext.request.contextPath}/purchase_order?action=receive&id=${order.id}"
                           onclick="return confirm('确认收货入库吗？此操作将增加商品库存！');">入库确认</a> |
                    </perm:check>
                </c:if>

                <!-- 删除按钮：只有有删除权限才显示 -->
                <perm:check code="purchase:delete">
                    <a class="btn-del" href="${pageContext.request.contextPath}/purchase_order?action=delete&id=${order.id}"
                       onclick="return confirm('确定要删除这张订单吗？');">删除</a>
                </perm:check>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty orderList}">
        <tr>
            <td colspan="9" style="color: red;">暂无采购订单，请先添加！</td>
        </tr>
    </c:if>
</table>

<%@ include file="footer.jsp" %>
</body>
</html>