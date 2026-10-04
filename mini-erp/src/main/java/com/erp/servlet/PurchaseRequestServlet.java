package com.erp.servlet;

import com.erp.dao.PurchaseRequestDao;
import com.erp.dao.ProductDao;
import com.erp.dao.SupplierDao;
import com.erp.entity.PurchaseRequest;
import com.erp.entity.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 采购申请Servlet
 * 支持：列表查看、新建申请、审批通过、驳回、删除、生成订单
 */
public class PurchaseRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        PurchaseRequestDao dao = new PurchaseRequestDao();
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if ("new".equals(action)) {
            // 加载商品列表供选择
            req.setAttribute("productList", new ProductDao().findAll());
            req.getRequestDispatcher("/add_purchase_request.jsp").forward(req, resp);
            return;
        }

        if ("delete".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            dao.deleteById(id);
            resp.sendRedirect(req.getContextPath() + "/purchase_request");
            return;
        }

        if ("approve".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            String comment = req.getParameter("comment");
            boolean success = dao.approve(id, currentUser.getId(),
                    currentUser.getRealName(), comment != null ? comment : "");
            if (success) {
                resp.sendRedirect(req.getContextPath() + "/purchase_request");
            } else {
                resp.getWriter().write("审批失败，请查看后台日志！");
            }
            return;
        }

        if ("reject".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            String comment = req.getParameter("comment");
            boolean success = dao.reject(id, currentUser.getId(),
                    currentUser.getRealName(), comment != null ? comment : "");
            if (success) {
                resp.sendRedirect(req.getContextPath() + "/purchase_request");
            } else {
                resp.getWriter().write("驳回失败，请查看后台日志！");
            }
            return;
        }

        if ("convert".equals(action)) {
            // 从申请生成采购订单 —— 跳到订单创建页并预填数据
            long id = Long.parseLong(req.getParameter("id"));
            PurchaseRequest pr = dao.findById(id);
            if (pr != null) {
                req.setAttribute("convertRequest", pr);
                req.setAttribute("requestId", pr.getId());
                req.setAttribute("productList", new ProductDao().findAll());
                req.setAttribute("supplierList", new SupplierDao().findAll());
                req.getRequestDispatcher("/add_purchase_order.jsp").forward(req, resp);
            } else {
                resp.getWriter().write("申请不存在！");
            }
            return;
        }

        // 默认：列表展示
        req.setAttribute("requestList", dao.findAll());
        req.getRequestDispatcher("/purchase_request_list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        String productIdStr = req.getParameter("productId");
        String quantityStr = req.getParameter("quantity");
        String estimatedPriceStr = req.getParameter("estimatedPrice");
        String reason = req.getParameter("reason");

        PurchaseRequest pr = new PurchaseRequest();
        pr.setRequestNo("PR" + System.currentTimeMillis());
        pr.setApplicantId(currentUser.getId());
        pr.setApplicantName(currentUser.getRealName());
        pr.setProductId(Long.parseLong(productIdStr));
        pr.setQuantity(Integer.parseInt(quantityStr));
        pr.setEstimatedPrice(estimatedPriceStr != null && !estimatedPriceStr.isEmpty()
                ? Double.parseDouble(estimatedPriceStr) : 0.0);
        pr.setReason(reason);

        // 查询商品名称
        pr.setProductName(new ProductDao().findById(pr.getProductId()).getProductName());

        PurchaseRequestDao dao = new PurchaseRequestDao();
        boolean success = dao.insert(pr);

        if (success) {
            resp.sendRedirect(req.getContextPath() + "/purchase_request");
        } else {
            req.setAttribute("errorMsg", "保存失败！请检查商品是否存在后重试。");
            req.setAttribute("productList", new ProductDao().findAll());
            req.getRequestDispatcher("/add_purchase_request.jsp").forward(req, resp);
        }
    }
}