package com.erp.dao;

import com.erp.entity.PurchaseRequest;
import com.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购申请数据访问层
 * 支持申请CRUD、审批流程、存储过程调用
 */
public class PurchaseRequestDao {

    /**
     * 查询所有采购申请（含申请人和商品名称）
     *
     * EXPLAIN分析:
     * SELECT pr.*, u.real_name AS applicant_name, p.product_name
     * FROM purchase_request pr
     * LEFT JOIN sys_user u ON pr.applicant_id = u.id
     * LEFT JOIN product p ON pr.product_id = p.id
     * ORDER BY pr.id DESC
     *
     * 优化后使用索引:
     *   - purchase_request: type=index (PRIMARY)
     *   - sys_user: type=eq_ref (PRIMARY)
     *   - product: type=eq_ref (PRIMARY)
     */
    public List<PurchaseRequest> findAll() {
        List<PurchaseRequest> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT pr.id, pr.request_no, pr.applicant_id, u.real_name AS applicant_name, " +
                    "pr.product_id, p.product_name, pr.quantity, pr.estimated_price, " +
                    "pr.reason, pr.status, pr.approver_id, pr.approver_name, " +
                    "pr.approve_comment, pr.approve_time, pr.created_at " +
                    "FROM purchase_request pr " +
                    "LEFT JOIN sys_user u ON pr.applicant_id = u.id " +
                    "LEFT JOIN product p ON pr.product_id = p.id " +
                    "ORDER BY pr.id DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * 根据ID查询单个申请
     */
    public PurchaseRequest findById(long id) {
        PurchaseRequest pr = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT pr.*, u.real_name AS applicant_name, p.product_name " +
                    "FROM purchase_request pr " +
                    "LEFT JOIN sys_user u ON pr.applicant_id = u.id " +
                    "LEFT JOIN product p ON pr.product_id = p.id " +
                    "WHERE pr.id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                pr = mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return pr;
    }

    /**
     * 根据状态查询申请列表
     */
    public List<PurchaseRequest> findByStatus(String status) {
        List<PurchaseRequest> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT pr.*, u.real_name AS applicant_name, p.product_name " +
                    "FROM purchase_request pr " +
                    "LEFT JOIN sys_user u ON pr.applicant_id = u.id " +
                    "LEFT JOIN product p ON pr.product_id = p.id " +
                    "WHERE pr.status = ? ORDER BY pr.id DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * 新增采购申请（事务控制，含商品有效性校验）
     */
    public boolean insert(PurchaseRequest pr) {
        Connection conn = null;
        PreparedStatement stmtCheck = null;
        PreparedStatement stmtInsert = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 校验商品是否存在
            String checkSql = "SELECT COUNT(*) FROM product WHERE id = ?";
            stmtCheck = conn.prepareStatement(checkSql);
            stmtCheck.setLong(1, pr.getProductId());
            ResultSet checkRs = stmtCheck.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) == 0) {
                throw new RuntimeException("商品ID " + pr.getProductId() + " 不存在！");
            }

            // 2. 插入申请记录
            String sql = "INSERT INTO purchase_request (request_no, applicant_id, applicant_name, " +
                    "product_id, product_name, quantity, estimated_price, reason, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
            stmtInsert = conn.prepareStatement(sql);
            stmtInsert.setString(1, pr.getRequestNo());
            stmtInsert.setLong(2, pr.getApplicantId());
            stmtInsert.setString(3, pr.getApplicantName());
            stmtInsert.setLong(4, pr.getProductId());
            stmtInsert.setString(5, pr.getProductName());
            stmtInsert.setInt(6, pr.getQuantity());
            stmtInsert.setDouble(7, pr.getEstimatedPrice() != null ? pr.getEstimatedPrice() : 0.0);
            stmtInsert.setString(8, pr.getReason());
            stmtInsert.executeUpdate();

            conn.commit();
            success = true;
        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
                System.out.println("❌ 插入采购申请失败，事务已回滚！" + e.getMessage());
            } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(rs, stmtCheck, null);
            DBUtil.close(null, stmtInsert, conn);
        }
        return success;
    }

    /**
     * 审批通过采购申请（更新状态为APPROVED并记录审计日志）
     */
    public boolean approve(long requestId, long approverId, String approverName, String comment) {
        Connection conn = null;
        PreparedStatement stmtCheck = null;
        PreparedStatement stmtUpdate = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 检查状态
            String checkSql = "SELECT status FROM purchase_request WHERE id = ?";
            stmtCheck = conn.prepareStatement(checkSql);
            stmtCheck.setLong(1, requestId);
            rs = stmtCheck.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if (!"PENDING".equals(status)) {
                    throw new RuntimeException("申请状态不是待审批，无法审批！当前状态：" + status);
                }
            } else {
                throw new RuntimeException("申请不存在！");
            }

            // 2. 更新申请状态
            String updateSql = "UPDATE purchase_request SET status = 'APPROVED', " +
                    "approver_id = ?, approver_name = ?, approve_comment = ?, approve_time = NOW() " +
                    "WHERE id = ?";
            stmtUpdate = conn.prepareStatement(updateSql);
            stmtUpdate.setLong(1, approverId);
            stmtUpdate.setString(2, approverName);
            stmtUpdate.setString(3, comment);
            stmtUpdate.setLong(4, requestId);
            stmtUpdate.executeUpdate();

            // 审计日志由触发器 trg_purchase_request_status_change 自动写入
            conn.commit();
            success = true;
        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
                System.out.println("❌ 审批失败，事务已回滚！原因：" + e.getMessage());
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(rs, stmtCheck, null);
            DBUtil.close(null, stmtUpdate, conn);
        }
        return success;
    }

    /**
     * 驳回采购申请
     */
    public boolean reject(long requestId, long approverId, String approverName, String comment) {
        Connection conn = null;
        PreparedStatement stmtCheck = null;
        PreparedStatement stmtUpdate = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT status FROM purchase_request WHERE id = ?";
            stmtCheck = conn.prepareStatement(checkSql);
            stmtCheck.setLong(1, requestId);
            rs = stmtCheck.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if (!"PENDING".equals(status)) {
                    throw new RuntimeException("申请状态不是待审批，无法驳回！当前状态：" + status);
                }
            } else {
                throw new RuntimeException("申请不存在！");
            }

            String updateSql = "UPDATE purchase_request SET status = 'REJECTED', " +
                    "approver_id = ?, approver_name = ?, approve_comment = ?, approve_time = NOW() " +
                    "WHERE id = ?";
            stmtUpdate = conn.prepareStatement(updateSql);
            stmtUpdate.setLong(1, approverId);
            stmtUpdate.setString(2, approverName);
            stmtUpdate.setString(3, comment);
            stmtUpdate.setLong(4, requestId);
            stmtUpdate.executeUpdate();

            // 审计日志由触发器 trg_purchase_request_status_change 自动写入
            conn.commit();
            success = true;
        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
                System.out.println("❌ 驳回失败，事务已回滚！原因：" + e.getMessage());
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(rs, stmtCheck, null);
            DBUtil.close(null, stmtUpdate, conn);
        }
        return success;
    }

    /**
     * 审批通过后生成采购订单（调用存储过程）
     * 将APPROVED状态的申请转为采购订单
     */
    public Long createOrderFromRequest(long requestId, long approverId, String approverName,
                                        long supplierId, double unitPrice) {
        Connection conn = null;
        CallableStatement cstmt = null;
        Long orderId = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "{CALL sp_approve_and_create_order(?, ?, ?, ?, ?, ?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, requestId);
            cstmt.setLong(2, approverId);
            cstmt.setString(3, approverName);
            cstmt.setLong(4, supplierId);
            cstmt.setDouble(5, unitPrice);
            cstmt.registerOutParameter(6, Types.BIGINT);
            cstmt.execute();
            orderId = cstmt.getLong(6);
            if (orderId == 0) orderId = null;
        } catch (Exception e) {
            System.out.println("❌ 生成订单失败！" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(null, cstmt, conn);
        }
        return orderId;
    }

    /**
     * 根据ID删除申请
     */
    public void deleteById(long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM purchase_request WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(null, stmt, conn);
        }
    }

    /**
     * ResultSet映射到实体
     */
    private PurchaseRequest mapRow(ResultSet rs) throws SQLException {
        PurchaseRequest pr = new PurchaseRequest();
        pr.setId(rs.getLong("id"));
        pr.setRequestNo(rs.getString("request_no"));
        pr.setApplicantId(rs.getLong("applicant_id"));
        try { pr.setApplicantName(rs.getString("applicant_name")); } catch (SQLException e) {}
        pr.setProductId(rs.getLong("product_id"));
        try { pr.setProductName(rs.getString("product_name")); } catch (SQLException e) {}
        pr.setQuantity(rs.getInt("quantity"));
        pr.setEstimatedPrice(rs.getDouble("estimated_price"));
        pr.setReason(rs.getString("reason"));
        pr.setStatus(rs.getString("status"));
        try { pr.setApproverId(rs.getLong("approver_id")); } catch (SQLException e) {}
        try { pr.setApproverName(rs.getString("approver_name")); } catch (SQLException e) {}
        try { pr.setApproveComment(rs.getString("approve_comment")); } catch (SQLException e) {}
        try { pr.setApproveTime(rs.getTimestamp("approve_time")); } catch (SQLException e) {}
        pr.setCreatedAt(rs.getTimestamp("created_at"));
        return pr;
    }
}