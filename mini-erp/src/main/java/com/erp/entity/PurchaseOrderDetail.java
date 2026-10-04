package com.erp.entity;

public class PurchaseOrderDetail {
    private Long id;
    private Long orderId;      // 所属订单ID
    private Long productId;    // 商品ID
    private Integer quantity;  // 采购数量
    private Double unitPrice;  // 单价
    private Double lineTotal;  // 行总金额 (数量 * 单价)

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
}