<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="perm" uri="http://erp.com/tags" %>
<html>
<head>
    <title>采购申请 - Mini ERP</title>
    <style>
        table { border-collapse: collapse; width: 100%; margin: 0 auto; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }
        th { background-color: #f2f2f2; }
        .btn { padding: 5px 10px; color: white; text-decoration: none; border-radius: 4px; margin: 0 3px; }
        .btn-primary { background-color: #007bff; }
        .btn-success { background-color: #28a745; }
        .btn-danger { background-color: #dc3545; }
        .btn-warning { background-color: #ffc107; color: black; }
        .status-PENDING { color: gray; font-weight: bold; }
        .status-APPROVED { color: #007bff; font-weight: bold; }
        .status-REJECTED { color: red; font-weight: bold; }
        .status-CONVERTED { color: green; font-weight: bold; }
        .action-bar { margin-bottom: 20px; text-align: center; }
    </style>
</head>
<body>
<%@ include file="sidebar.jsp" %>

<h2 style="text-align: center">采购申请管理</h2>

<div class="action-bar">
    <perm:check code="purchase:request">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/purchase_request?action=new">新建采购申请</a>
    </perm:check>
    <perm:check code="menu:purchase">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/purchase_order" style="background-color: #6c757d;">采购订单</a>
    </perm:check>
</div>

<table>
    <tr>
        <th>申请编号</th>
        <th>申请人</th>
        <th>商品名称</th>
        <th>数量</th>
        <th>预估单价</th>
        <th>申请原因</th>
        <th>状态</th>
        <th>审批人</th>
        <th>审批时间</th>
        <th>创建时间</th>
        <th>操作</th>
    </tr>
    <c:forEach items="${requestList}" var="r">
        <tr>
            <td>${r.requestNo}</td>
            <td>${r.applicantName}</td>
            <td>${r.productName}</td>
            <td>${r.quantity}</td>
            <td>¥ <fmt:formatNumber value="${r.estimatedPrice}" pattern="#,##0.00"/></td>
            <td>${r.reason}</td>
            <td class="status-${r.status}">${r.status}</td>
            <td>${r.approverName}</td>
            <td><fmt:formatDate value="${r.approveTime}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td><fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td>
                <!-- PENDING状态 → 显示审批和驳回按钮 -->
                <c:if test="${r.status == 'PENDING'}">
                    <perm:check code="purchase:approve">
                        <a class="btn btn-success" href="${pageContext.request.contextPath}/purchase_request?action=approve&id=${r.id}"
                           onclick="return confirm('确认审批通过吗？');">审批通过</a>
                        <a class="btn btn-danger" href="${pageContext.request.contextPath}/purchase_request?action=reject&id=${r.id}"
                           onclick="var c=prompt('请输入驳回原因:'); if(c) this.href+='&comment='+encodeURIComponent(c); return c!=null;">驳回</a>
                    </perm:check>
                </c:if>

                <!-- APPROVED状态 → 显示生成订单按钮 -->
                <c:if test="${r.status == 'APPROVED'}">
                    <perm:check code="purchase:order">
                        <a class="btn btn-warning" href="${pageContext.request.contextPath}/purchase_request?action=convert&id=${r.id}">生成订单</a>
                    </perm:check>
                </c:if>

                <!-- 删除按钮 -->
                <perm:check code="purchase:delete">
                    <a class="btn btn-danger" href="${pageContext.request.contextPath}/purchase_request?action=delete&id=${r.id}"
                       onclick="return confirm('确定要删除吗？');">删除</a>
                </perm:check>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty requestList}">
        <tr><td colspan="11" style="color: red;">暂无采购申请数据！</td></tr>
    </c:if>
</table>

<%@ include file="footer.jsp" %>
</body>
</html>