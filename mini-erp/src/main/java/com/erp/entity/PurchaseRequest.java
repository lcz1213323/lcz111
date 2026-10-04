package com.erp.entity;

import java.util.Date;

/**
 * 采购申请实体，对应数据库 purchase_request 表
 */
public class PurchaseRequest {
    private Long id;
    private String requestNo;       // 申请编号 PR开头
    private Long applicantId;       // 申请人ID
    private String applicantName;   // 申请人姓名（用于页面展示）
    private Long productId;         // 申请商品ID
    private String productName;     // 商品名称（用于页面展示）
    private Integer quantity;       // 申请数量
    private Double estimatedPrice;  // 预估单价
    private String reason;          // 申请原因
    private String status;          // PENDING/APPROVED/REJECTED/CONVERTED
    private Long approverId;        // 审批人ID
    private String approverName;    // 审批人姓名
    private String approveComment;  // 审批意见
    private Date approveTime;       // 审批时间
    private Date createdAt;         // 创建时间

    public PurchaseRequest() {}

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getApproveComment() { return approveComment; }
    public void setApproveComment(String approveComment) { this.approveComment = approveComment; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}