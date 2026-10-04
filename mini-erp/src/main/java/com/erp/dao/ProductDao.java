package com.erp.dao;

import com.erp.entity.Product;
import com.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    // 查询所有商品
    public List<Product> findAll() {
        List<Product> productList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, product_code, product_name, category, unit, purchase_price, sale_price, stock_qty FROM product";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setCategory(rs.getString("category"));
                p.setUnit(rs.getString("unit"));
                p.setPurchasePrice(rs.getDouble("purchase_price"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setStockQty(rs.getInt("stock_qty"));
                productList.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return productList;
    }

    // 新增商品
    public void insert(Product p) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO product (product_code, product_name, category, unit, purchase_price, sale_price, stock_qty) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getProductCode());
            stmt.setString(2, p.getProductName());
            stmt.setString(3, p.getCategory());
            stmt.setString(4, p.getUnit());
            stmt.setDouble(5, p.getPurchasePrice());
            stmt.setDouble(6, p.getSalePrice());
            stmt.setInt(7, p.getStockQty());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(null, stmt, conn);
        }
    }

    // 根据 ID 删除商品
    public void deleteById(long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM product WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(null, stmt, conn);
        }
    }

    // 根据 ID 查询商品（用于编辑回显）
    public Product findById(long id) {
        Product p = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM product WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                p = new Product();
                p.setId(rs.getLong("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setCategory(rs.getString("category"));
                p.setUnit(rs.getString("unit"));
                p.setPurchasePrice(rs.getDouble("purchase_price"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setStockQty(rs.getInt("stock_qty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return p;
    }

    // 更新商品
    public void update(Product p) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE product SET product_code=?, product_name=?, category=?, unit=?, purchase_price=?, sale_price=?, stock_qty=? WHERE id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getProductCode());
            stmt.setString(2, p.getProductName());
            stmt.setString(3, p.getCategory());
            stmt.setString(4, p.getUnit());
            stmt.setDouble(5, p.getPurchasePrice());
            stmt.setDouble(6, p.getSalePrice());
            stmt.setInt(7, p.getStockQty());
            stmt.setLong(8, p.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(null, stmt, conn);
        }
    }
}