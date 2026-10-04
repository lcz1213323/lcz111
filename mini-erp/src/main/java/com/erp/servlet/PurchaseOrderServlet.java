package com.erp.servlet;

import com.erp.dao.PurchaseOrderDao;
import com.erp.dao.SupplierDao;
import com.erp.dao.ProductDao;
import com.erp.entity.PurchaseOrder;
import com.erp.entity.PurchaseOrderDetail;
import com.erp.entity.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购订单Servlet（增强版）
 * 新增：入库确认action、审批分离、从申请转订单、审计日志
 */
public class PurchaseOrderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        PurchaseOrderDao dao = new PurchaseOrderDao();
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if ("new".equals(action)) {
            SupplierDao supplierDao = new SupplierDao();
            req.setAttribute("supplierList", supplierDao.findAll());
            req.setAttribute("productList", new ProductDao().findAll());

            // 如果是来自采购申请的转换，预填数据
            String requestIdStr = req.getParameter("requestId");
            if (requestIdStr != null) {
                req.setAttribute("requestId", requestIdStr);
            }

            req.getRequestDispatcher("/add_purchase_order.jsp").forward(req, resp);
            return;
        }

        if ("delete".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            dao.deleteById(id);
            resp.sendRedirect(req.getContextPath() + "/purchase_order");
            return;
        }

        // 【重构】审批：只变更状态，不更新库存（DRAFT → APPROVED）
        if ("approve".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            boolean success = dao.approveOrder(id, currentUser.getId());
            if (success) {
                resp.sendRedirect(req.getContextPath() + "/purchase_order");
            } else {
                resp.getWriter().write("审批失败，请查看后台日志！可能订单不是草稿状态。");
            }
            return;
        }

        // 【新增】收货入库确认（APPROVED → COMPLETED，更新库存）
        if ("receive".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            boolean success = dao.receiveOrder(id, currentUser.getId(), currentUser.getRealName());
            if (success) {
                resp.sendRedirect(req.getContextPath() + "/purchase_order");
            } else {
                resp.getWriter().write("入库确认失败，请查看后台日志！可能订单状态不允许入库。");
            }
            return;
        }

        // 【新增】从采购申请直接生成订单（调用存储过程）
        if ("create_from_request".equals(action)) {
            long requestId = Long.parseLong(req.getParameter("requestId"));
            long supplierId = Long.parseLong(req.getParameter("supplierId"));
            double unitPrice = Double.parseDouble(req.getParameter("unitPrice"));

            Long orderId = dao.createOrderFromRequest(requestId, currentUser.getId(),
                    currentUser.getRealName(), supplierId, unitPrice);
            if (orderId != null) {
                resp.sendRedirect(req.getContextPath() + "/purchase_order");
            } else {
                resp.getWriter().write("生成订单失败，请查看后台日志！");
            }
            return;
        }

        // 默认列表展示
        List<PurchaseOrder> orderList = dao.findAll();
        req.setAttribute("orderList", orderList);
        req.getRequestDispatcher("/purchase_order_list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        String supplierIdStr = req.getParameter("supplierId");
        String requestIdStr = req.getParameter("requestId");
        String orderDateStr = req.getParameter("orderDate");
        String[] productIds = req.getParameterValues("productId");
        String[] quantities = req.getParameterValues("quantity");
        String[] unitPrices = req.getParameterValues("unitPrice");

        // 【修复】从采购申请生成订单：调用存储过程 sp_approve_and_create_order，
        // 申请状态转 CONVERTED、订单自动带 request_id 生成（DRAFT）。
        if (requestIdStr != null && !requestIdStr.isEmpty()) {
            PurchaseOrderDao dao = new PurchaseOrderDao();
            long requestId = Long.parseLong(requestIdStr);
            long supplierId = Long.parseLong(supplierIdStr);
            double unitPrice = Double.parseDouble(req.getParameter("unitPrice"));
            Long orderId = dao.createOrderFromRequest(requestId, currentUser.getId(),
                    currentUser.getRealName(), supplierId, unitPrice);
            if (orderId != null) {
                resp.sendRedirect(req.getContextPath() + "/purchase_order");
            } else {
                resp.getWriter().write("生成订单失败，请查看后台日志！");
            }
            return;
        }

        List<PurchaseOrderDetail> details = new ArrayList<>();
        double totalAmount = 0.0;

        if (productIds != null) {
            for (int i = 0; i < productIds.length; i++) {
                if (productIds[i] == null || productIds[i].isEmpty()) continue;
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setProductId(Long.parseLong(productIds[i]));
                int qty = Integer.parseInt(quantities[i]);
                double price = Double.parseDouble(unitPrices[i]);
                detail.setQuantity(qty);
                detail.setUnitPrice(price);
                detail.setLineTotal(qty * price);
                totalAmount += (qty * price);
                details.add(detail);
            }
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setOrderNo("PO" + System.currentTimeMillis());
        po.setSupplierId(Long.parseLong(supplierIdStr));

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            po.setOrderDate(sdf.parse(orderDateStr));
        } catch (Exception e) { e.printStackTrace(); }
        po.setTotalAmount(totalAmount);
        po.setStatus("DRAFT");
        // 【修正】使用当前登录用户ID，而非硬编码1L
        po.setCreatedBy(currentUser.getId());

        PurchaseOrderDao dao = new PurchaseOrderDao();
        boolean success = dao.insertOrder(po, details);

        if (success) {
            resp.sendRedirect(req.getContextPath() + "/purchase_order");
        } else {
            req.setAttribute("errorMsg", "保存失败！可能包含不存在的商品ID。请检查后重试。");
            req.getRequestDispatcher("/purchase_order").forward(req, resp);
        }
    }
}