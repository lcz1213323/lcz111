package com.erp.servlet;

import com.erp.dao.ProductDao;
import com.erp.entity.Product;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ProductServlet extends HttpServlet {

    // GET 请求：根据 action 参数判断是 删除、编辑回显 还是 列表展示
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        ProductDao dao = new ProductDao();

        if ("delete".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            dao.deleteById(id);
            resp.sendRedirect(req.getContextPath() + "/product");
            return;
        } else if ("edit".equals(action)) {
            long id = Long.parseLong(req.getParameter("id"));
            Product p = dao.findById(id);
            req.setAttribute("product", p);
            req.getRequestDispatcher("/edit_product.jsp").forward(req, resp);
            return;
        }

        // 默认列表展示
        List<Product> productList = dao.findAll();
        req.setAttribute("productList", productList);
        req.getRequestDispatcher("/product_list.jsp").forward(req, resp);
    }

    // POST 请求：根据是否有 id 参数，决定是 新增 还是 修改
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String code = req.getParameter("productCode");
        String name = req.getParameter("productName");
        String category = req.getParameter("category");
        String unit = req.getParameter("unit");
        double purchasePrice = Double.parseDouble(req.getParameter("purchasePrice"));
        double salePrice = Double.parseDouble(req.getParameter("salePrice"));
        int stockQty = Integer.parseInt(req.getParameter("stockQty"));

        Product p = new Product();
        p.setProductCode(code);
        p.setProductName(name);
        p.setCategory(category);
        p.setUnit(unit);
        p.setPurchasePrice(purchasePrice);
        p.setSalePrice(salePrice);
        p.setStockQty(stockQty);

        ProductDao dao = new ProductDao();
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            // 有 id，说明是编辑更新
            p.setId(Long.parseLong(idStr));
            dao.update(p);
        } else {
            // 没有 id，说明是新增
            dao.insert(p);
        }

        resp.sendRedirect(req.getContextPath() + "/product");
    }
}