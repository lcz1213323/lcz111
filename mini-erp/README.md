# Mini ERP 系统使用说明

## 系统概述

Mini ERP 是一个基于 Java Web (Servlet/JSP + JDBC + MySQL) 的企业资源管理系统，实现了**商品管理**、**库存管理**、**采购管理**三大核心模块，并支持 **RBAC 细粒度权限控制**。

---

## 一、技术架构

| 层级   | 技术                             |
| ---- | ------------------------------ |
| 前端   | JSP + JSTL + 自定义权限标签           |
| 后端   | Servlet (Java 17)              |
| 数据库  | MySQL 8.0                      |
| 持久层  | JDBC (原生，无ORM)                 |
| 权限模型 | RBAC (角色-权限-用户)                |
| 容器   | Jetty 9.4 (开发) / Tomcat 9 (生产) |

---

## 二、数据库初始化

### 前置条件

1. MySQL 8.0+ 已安装并运行在 `localhost:3306`
2. 数据库 `mini_erp` 已创建且包含基础表（sys_user, sys_role, sys_user_role, product, supplier, purchase_order, purchase_order_detail）

### 执行初始化脚本

```bash
# 连接数据库并执行脚本
mysql -u root -plczimxy233 mini_erp < src/main/resources/db/init.sql
```

脚本会创建以下新表：

- **sys_permission** — 权限表（菜单+按钮）
- **sys_role_permission** — 角色-权限关联表
- **purchase_request** — 采购申请表
- **purchase_audit_log** — 审计日志表

以及两个核心存储过程：

- **sp_receive_order** — 入库确认（更新库存+状态）
- **sp_approve_and_create_order** — 审批申请并自动生成订单

以及两个审计触发器（状态变更自动写 `purchase_audit_log`，应用层不再重复写日志）：

- **trg_purchase_request_status_change** — 采购申请状态变更审计
- **trg_purchase_order_status_change** — 采购订单状态变更审计

---

## 三、启动系统

```bash
cd mini-erp
mvn jetty:run
```

访问地址：**<http://localhost:8080/erp>**

---

## 四、角色与权限

系统预设三种角色，不同角色登录后看到的菜单和按钮不同：

| 角色  | 角色编码      | 可见菜单           | 可执行操作            |
| --- | --------- | -------------- | ---------------- |
| 管理员 | ADMIN     | 商品管理、采购管理、采购申请 | 所有操作（增删改查、审批、入库） |
| 采购员 | PURCHASER | 采购申请           | 提交采购申请           |
| 库管员 | WAREHOUSE | 采购管理           | 查看订单、入库确认        |

### RBAC 权限模型

```
用户(User) ──→ 用户角色关联(sys_user_role) ──→ 角色(Role)
                                                    │
                                                    ↓
权限(Permission) ←── 角色权限关联(sys_role_permission)
```

- **菜单级权限** (menu_type=1)：控制侧边栏菜单项的显示/隐藏
- **按钮级权限** (menu_type=2)：控制页面中操作按钮的显示/隐藏

### 权限码说明

| 权限编码               | 含义      | 类型 |
| ------------------ | ------- | -- |
| `menu:product`     | 商品管理菜单  | 菜单 |
| `menu:purchase`    | 采购管理菜单  | 菜单 |
| `menu:request`     | 采购申请菜单  | 菜单 |
| `product:add`      | 新增商品    | 按钮 |
| `product:edit`     | 编辑商品    | 按钮 |
| `product:delete`   | 删除商品    | 按钮 |
| `purchase:request` | 提交申请    | 按钮 |
| `purchase:approve` | 审批申请/订单 | 按钮 |
| `purchase:order`   | 生成订单    | 按钮 |
| `purchase:receive` | 入库确认    | 按钮 |
| `purchase:delete`  | 删除订单    | 按钮 |

---

## 五、采购完整流程

```
采购员(buyer)              管理员(admin)                库管员(keeper)
  │                          │                          │
  ├─ 1.提交采购申请 ────────→│                          │
  │   (状态: PENDING)        │                          │
  │                          ├─ 2.审批申请 ─────────────→│
  │                          │   (状态: APPROVED)        │
  │                          ├─ 3.生成采购订单           │
  │                          │   (状态: DRAFT)           │
  │                          ├─ 4.审批订单              │
  │                          │   (状态: APPROVED)        │
  │                          │                          ├─ 5.收货入库确认
  │                          │                          │   (状态: COMPLETED)
  │                          │                          │   自动更新库存 ✓
```

### 操作步骤

**步骤1 — 采购员提交申请**

1. 用采购员账号登录
2. 点击"采购申请" → "新建采购申请"
3. 选择商品、填写数量和原因 → 提交
4. 申请状态为 PENDING（待审批）

**步骤2 — 管理员审批申请**

1. 用管理员账号登录
2. 进入"采购申请"列表
3. 对 PENDING 状态的申请点击"审批通过"（或"驳回"）
4. 申请状态变为 APPROVED（已审批）

**步骤3 — 生成采购订单（管理员）**

1. 用管理员账号登录
2. 对 APPROVED 状态的申请点击"生成订单"
3. 系统跳转到新建订单页面，选择供应商 → 保存
4. 调用存储过程 `sp_approve_and_create_order` 自动：
   - 创建采购订单及明细
   - 更新申请状态为 CONVERTED
   - 触发器自动记录审计日志

**步骤4 — 管理员审批订单**

1. 进入"采购管理"列表
2. 对 DRAFT 状态的订单点击"审批通过"
3. 订单状态变为 APPROVED

**步骤5 — 库管员入库确认**

1. 用库管员账号登录
2. 进入"采购管理"列表
3. 对 APPROVED 状态的订单点击"入库确认"
4. 调用存储过程 `sp_receive_order` 自动：
   - 更新商品库存 (stock_qty += 采购数量)
   - 订单状态变为 COMPLETED
   - 记录审计日志

---

## 六、性能优化

系统对以下高频查询列添加了索引，优化效果显著：

| 表                     | 索引                                 | 优化场景            |
| --------------------- | ---------------------------------- | --------------- |
| product               | idx_product_code, idx_product_name | 商品编码/名称搜索       |
| purchase_order        | idx_order_no, idx_status_created   | 订单号查询、状态筛选+时间排序 |
| purchase_order_detail | idx_order_id                       | 关联订单明细查询        |
| sys_user              | idx_username                       | 登录验证            |
| purchase_request      | idx_status, idx_applicant          | 审批列表筛选          |

DAO 层关键方法均添加了 `EXPLAIN` 分析注释，方便 DBA 审查执行计划。

---

## 七、测试账号

启动系统前请确保数据库中已存在以下测试用户：

```
管理员: admin / 123456    (角色: ADMIN)
采购员: buyer / 123456    (角色: PURCHASER)
库管员: keeper / 123456   (角色: WAREHOUSE)
```

如需新建角色或用户，直接在数据库中操作 `sys_user` 和 `sys_user_role` 表。

---

## 八、项目结构

```
src/main/java/com/erp/
├── entity/       实体类 (Product, User, PurchaseOrder, Permission, PurchaseRequest)
├── dao/          数据访问层 (ProductDao, UserDao, PurchaseOrderDao, PermissionDao, PurchaseRequestDao, SupplierDao)
├── servlet/      Servlet控制器 (ProductServlet, LoginServlet, PurchaseOrderServlet, PurchaseRequestServlet)
├── filter/       过滤器 (AuthFilter — 认证+URL授权)
├── tag/          自定义JSP标签 (HasPermissionTag — 按钮级权限控制)
└── util/         工具类 (DBUtil, PermissionUtil)

src/main/webapp/
├── WEB-INF/
│   ├── web.xml    Servlet/Filter/TagLib注册
│   └── erp.tld    权限标签库描述文件
├── sidebar.jsp    动态侧边栏导航（根据角色权限渲染菜单）
├── footer.jsp     页面尾部
├── login.jsp      登录页
├── product_list.jsp    商品列表
├── add_product.jsp     新增商品
├── edit_product.jsp    编辑商品
├── purchase_order_list.jsp    采购订单列表
├── add_purchase_order.jsp     新建采购订单
├── purchase_request_list.jsp  采购申请列表
└── add_purchase_request.jsp   新建采购申请

src/main/resources/db/
└── init.sql       数据库初始化脚本（权限表、申请表、审计表、存储过程、索引、种子数据）
```
