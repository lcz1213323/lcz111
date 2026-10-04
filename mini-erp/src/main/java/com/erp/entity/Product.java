package com.erp.entity;

/**
 * 商品实体类，对应数据库 product 表
 */
public class Product {
    private Long id;
    private String productCode;   // 商品编码
    private String productName;   // 商品名称
    private String category;      // 分类
    private String unit;          // 单位
    private Double purchasePrice; // 参考采购价
    private Double salePrice;     // 参考销售价
    private Integer stockQty;     // 当前库存

    // 无参构造方法
    public Product() {}

    // 以下是 Getter 和 Setter 方法，用于读取和修改属性
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }

    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }

    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
}