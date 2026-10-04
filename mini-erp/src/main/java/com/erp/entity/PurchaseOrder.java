package com.erp.entity;

import java.util.Date;

/**
 * 采购订单实体，对应数据库 purchase_order 表
 * 增加了采购流程增强所需的字段
 */
public class PurchaseOrder {
    private Long id;
    private String orderNo;        // 订单编号
    private Long supplierId;       // 供应商ID
    private String supplierName;   // 供应商名称（用于页面展示）
    private Long requestId;        // 【新增】来源采购申请ID
    private Date orderDate;        // 订单日期
    private Double totalAmount;    // 总金额
    private String status;         // 状态：DRAFT, APPROVED, RECEIVING, COMPLETED, CANCELLED
    private Long createdBy;        // 创建人ID（修正：之前硬编码1L，现在从Session获取）
    private String createdByName;  // 创建人姓名（用于页面展示）
    private Long receivedBy;       // 【新增】入库操作人ID
    private String receivedByName; // 【新增】入库操作人姓名
    private Date receivedAt;       // 【新增】入库时间
    private Date createdAt;        // 创建时间

    public PurchaseOrder() {}

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public Long getReceivedBy() { return receivedBy; }
    public void setReceivedBy(Long receivedBy) { this.receivedBy = receivedBy; }
    public String getReceivedByName() { return receivedByName; }
    public void setReceivedByName(String receivedByName) { this.receivedByName = receivedByName; }
    public Date getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Date receivedAt) { this.receivedAt = receivedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}