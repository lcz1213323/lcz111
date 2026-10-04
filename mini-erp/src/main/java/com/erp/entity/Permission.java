package com.erp.entity;

/**
 * 权限实体，对应数据库 sys_permission 表
 * 支持菜单级(menu_type=1)和按钮级(menu_type=2)权限
 */
public class Permission {
    private Long id;
    private String permissionCode;  // 权限编码，如 product:add
    private String permissionName;  // 权限名称，如 新增商品
    private String url;             // 菜单URL，按钮级为null
    private Long parentId;          // 父权限ID，0=顶级菜单
    private Integer menuType;       // 1=菜单 2=按钮
    private String icon;            // 菜单图标
    private Integer sortOrder;      // 排序号

    public Permission() {}

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getMenuType() { return menuType; }
    public void setMenuType(Integer menuType) { this.menuType = menuType; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}