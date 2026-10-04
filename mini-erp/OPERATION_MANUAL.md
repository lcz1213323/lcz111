# Mini ERP 操作手册（逐步操作指南）

> 本手册按「从上到下、逐条操作」的方式编写，每一步都写清：**用哪个账号、点哪里、填什么、看到什么结果**。  
> 照着做即可完整跑通：**商品管理 → 采购申请 → 审批 → 生成订单 → 审批订单 → 收货入库** 全流程，并验证 RBAC 权限与审计日志。

---

## 0. 系统与角色速览

| 项目   | 说明                                                                       |
| ---- | ------------------------------------------------------------------------ |
| 系统地址 | <http://localhost:8080/erp>                                              |
| 技术栈  | Servlet/JSP + JSTL + JDBC + MySQL 8.0，Maven + Jetty 9.4（`mvn jetty:run`） |
| 数据库  | `mini_erp`（root / lczimxy233）                                            |

三个测试账号（密码均为 `123456`）：

| 账号       | 真实姓名  | 角色        | 能看到的菜单         | 能做的操作            |
| -------- | ----- | --------- | -------------- | ---------------- |
| `admin`  | 超级管理员 | ADMIN     | 商品管理、采购管理、采购申请 | 全部（增删改查、审批、生成订单） |
| `buyer`  | 采购员小王 | PURCHASER | 采购申请           | 提交采购申请           |
| `keeper` | 库管员小李 | WAREHOUSE | 采购管理           | 查看订单、收货入库确认      |

**完整采购流程**（谁在哪个环节操作）：

```
buyer(采购员)                admin(管理员)                    keeper(库管员)
  │ 1.提交采购申请 PENDING      │                                │
  ├──────────────────────────→│ 2.审批申请 → APPROVED          │
  │                            │ 3.生成订单 → DRAFT             │
  │                            │ 4.审批订单 → APPROVED          │
  │                            ├──────────────────────────────→│ 5.入库确认 → COMPLETED
  │                            │                                │    库存自动增加 ✓
```

---

## 1. 环境准备（一次性）

### 1.1 前置条件

1. 已安装 **JDK 17**、**Maven**、**MySQL 8.0**。
2. MySQL 已启动，监听 `localhost:3306`。
3. 数据库 `mini_erp` 已存在，且包含基础表：  
   `sys_user`、`sys_role`、`sys_user_role`、`product`、`supplier`、`purchase_order`、`purchase_order_detail`。

> ⚠️ 说明：`init.sql` 是「增量补齐脚本」，在已有基础表之上补齐 RBAC 权限表、采购申请表、审计日志表、存储过程、触发器、索引与测试账号。若基础表尚不存在，需先建基础表（本仓库不含基础建表脚本，如需我可生成）。

### 1.2 执行初始化脚本（补齐权限/存储过程/触发器/索引/测试账号）

```bash
cd C:\Users\Administrator\IdeaProjects\mini-erp
mysql -u root -plczimxy233 --default-character-set=utf8mb4 mini_erp < src/main/resources/db/init.sql
```

- 脚本**幂等**，可重复执行（重复执行不会报错，也不会产生重复数据）。
- 执行后自动具备：11 条权限、15 条角色-权限映射、2 个存储过程、2 个审计触发器、若干索引、3 个测试账号。

### 1.3 启动系统

```bash
cd C:\Users\Administrator\IdeaProjects\mini-erp
mvn jetty:run
```

看到类似 `Started ...` 或 `Started Jetty Server` 后，浏览器访问 \*\*<http://localhost:8080/erp\\\*\\\*。>

---

## 2. 商品管理（管理员 admin）

> 采购前必须先有商品，否则申请/订单里没有可选商品。

1. 打开 <http://localhost:8080/erp> ，输入 `admin` / `123456`，点【登 录】。
2. 登录后左侧菜单显示 3 项：**📦 商品管理、🛒 采购管理、📋 采购申请**。
3. 点【📦 商品管理】，进入商品列表页。
4. 点右上角【新增商品】按钮，进入表单页，依次填写：
   | 字段    | 示例值     | 说明           |
   | ----- | ------- | ------------ |
   | 商品编码  | `P001`  | 必填，唯一        |
   | 商品名称  | `测试商品`  | 必填           |
   | 分类    | `办公用品`  | 可空           |
   | 单位    | `个`     | 默认"个"        |
   | 参考采购价 | `10.00` | 生成申请/订单时自动带出 |
   | 参考销售价 | `15.00` |              |
   | 初始库存  | `50`    | 初始库存数量       |
5. 点【提交保存】，页面跳回商品列表，能看到刚新增的商品（库存 50）。
6. 如需修改：在列表该行点【编辑】，改完点【保存修改】。
7. 如需删除：点【删除】，弹窗确认后删除。

> 此页面「新增/编辑/删除」按钮与「操作」列，只有拥有 `product:add/edit/delete` 权限的 **admin** 可见（见第 6 节权限验证）。

---

## 3. 采购申请（采购员 buyer）

1. 先退出 admin（右上角【退出登录】）。
2. 用 `buyer` / `123456` 登录。
3. 左侧菜单只有 **📋 采购申请** 一项（无商品管理、无采购管理——这是菜单级权限控制）。
4. 点【📋 采购申请】→ 点【新建采购申请】，进入表单页：
   | 字段   | 操作/示例               | 说明         |
   | ---- | ------------------- | ---------- |
   | 申请商品 | 下拉选择 `测试商品 (库存:50)` | 选择后自动带出采购价 |
   | 申请数量 | `20`                | 正整数        |
   | 预估单价 | `10.00`             | 自动填入，可改    |
   | 申请原因 | `库存不足，需补货`          | 可空         |
5. 点【提交申请】，回到申请列表，新申请状态为 **PENDING**（待审批）。

> buyer 只能「提交申请」，看不到「审批通过/驳回/生成订单/删除」等按钮（按钮级权限控制）。

---

## 4. 审批采购申请（管理员 admin）

1. 退出 buyer，用 `admin` / `123456` 登录。
2. 点【📋 采购申请】。
3. 找到那条 **PENDING** 状态的申请，行尾有【审批通过】和【驳回】两个按钮。
   - 点【审批通过】→ 弹窗确认 → 状态变为 **APPROVED**。
   - （或点【驳回】→ 输入驳回原因 → 状态变为 **REJECTED**，流程终止。）

> 审批动作会触发数据库触发器 `trg_purchase_request_status_change` 自动写审计日志。

---

## 5. 生成采购订单（管理员 admin）

1. 继续用 admin，在【采购申请】列表，找到那条 **APPROVED** 状态的申请。
2. 点该行的【生成订单】按钮。
3. 页面跳转到「新建采购订单」，此时表格里 **商品名称、申请数量** 已自动带出（来自申请），只需：
   - 供应商：下拉选择一个供应商。
   - 订单日期：选一个日期。
   - 单价：确认单价（默认带出预估单价），可改。
4. 点【保存订单】。

   系统调用存储过程 `sp_approve_and_create_order`，自动完成：
   - 申请状态 APPROVED → **CONVERTED**（已转单）
   - 生成一张新订单（状态 **DRAFT**、关联 `request_id`、含明细）
   - 触发器自动写审计日志
5. 页面跳转到【采购订单】列表，能看到新订单（状态 DRAFT，创建人为 admin）。

---

## 6. 审批采购订单（管理员 admin）

1. 在【采购订单】列表，找到状态为 **DRAFT** 的订单。
2. 点该行【审批通过】→ 确认。
3. 订单状态变为 **APPROVED**（已审批，等待入库）。
   - 该动作会记录审批人、审批时间，并由触发器 `trg_purchase_order_status_change` 写审计。

---

## 7. 收货入库确认（库管员 keeper）

1. 退出 admin，用 `keeper` / `123456` 登录。
2. 左侧菜单只有 **🛒 采购管理** 一项。
3. 点【🛒 采购管理】进入订单列表：
   - 能看到所有订单；
   - 但只对 **APPROVED**（或 RECEIVING）状态的订单显示【入库确认】按钮（keeper 只有 `purchase:receive` 权限）；
   - 看不到【新建采购订单】【审批通过】【删除】按钮。
4. 点那条 APPROVED 订单的【入库确认】→ 弹窗提示"确认收货入库吗？此操作将增加商品库存！"→ 确认。

   系统调用存储过程 `sp_receive_order`，自动完成：
   - 校验订单状态（必须是 APPROVED/RECEIVING）
   - 商品库存 `stock_qty += 采购数量`（例如 50 → 70）
   - 订单状态 APPROVED → **COMPLETED**
   - 记录入库操作人、入库时间，触发器写审计
5. **验证库存**：用 admin 登录 →【商品管理】，确认该商品库存已增加。

---

## 8. 验证 RBAC 权限（可选演示）

### 8.1 菜单级（动态菜单）

| 账号     | 左侧菜单                |
| ------ | ------------------- |
| admin  | 商品管理、采购管理、采购申请（3 项） |
| buyer  | 采购申请（1 项）           |
| keeper | 采购管理（1 项）           |

菜单由 `sidebar.jsp` 用 `<c:forEach items="${userMenus}">` 按当前用户角色动态渲染。

### 8.2 按钮级

- 同一张「采购订单」列表页，admin 看到：新建、审批通过、入库确认、删除；keeper 只看到：入库确认。
- 同一张「商品」列表页，只有 admin 看到「新增商品」「编辑」「删除」与「操作」列。
- 按钮由自定义标签 `<perm:check code="...">` 控制。

### 8.3 URL 级（越权拦截）

- 用 buyer 登录后，直接在地址栏访问 `http://localhost:8080/erp/product` → 返回 **403**（无 `menu:product` 权限）。
- 用 keeper 登录后，直接访问 `http://localhost:8080/erp/purchase_request` → **403**（无 `menu:request` 权限）。
- 由 `AuthFilter` 做认证 + URL 授权。

---

## 9. 查看审计日志（可选，SQL）

状态每次变更都会由触发器写入 `purchase_audit_log`，可用以下 SQL 核对：

```sql
SELECT id, business_type, business_id, operation,
       operator_name, old_status, new_status, created_at
FROM purchase_audit_log
ORDER BY id DESC;
```

- `business_type=REQUEST`：采购申请的 APPROVE / REJECT / CONVERT
- `business_type=ORDER`：采购订单的 APPROVE / RECEIVE
- 每条状态变更只有一条记录（触发器统一写，应用层不再重复写，避免重复日志）。

---

## 10. 性能说明（EXPLAIN + 索引）

系统对高频查询已建索引（`init.sql` 已落地）：

| 表                     | 索引                                                        | 用途                |
| --------------------- | --------------------------------------------------------- | ----------------- |
| product               | idx_product_code / idx_product_name / idx_category        | 商品编码/名称/分类搜索      |
| purchase_order        | idx_order_no / idx_status_created / idx_request_id        | 订单号、状态+时间排序、按申请关联 |
| purchase_order_detail | idx_order_id                                              | 按订单取明细、入库回补库存     |
| purchase_request      | idx_status / idx_applicant / idx_product / idx_created_at | 审批列表按状态筛选         |
| supplier              | idx_supplier_name                                         | 供应商名称搜索           |
| sys_user              | uk_username                                               | 登录校验              |

DAO 层关键方法（`ProductDao`、`PurchaseOrderDao`、`PurchaseRequestDao` 等）的 Javadoc 中写有 `EXPLAIN` 执行计划分析（优化前/优化后对比）。

可自行验证：`EXPLAIN SELECT ... FROM purchase_order po LEFT JOIN supplier s ... ORDER BY po.id DESC;` 观察 `type` 从 `ALL`（全表扫描）变为 `index/eq_ref`。

---

## 11. 常见问题

| 现象           | 处理                                                                   |                       |
| ------------ | -------------------------------------------------------------------- | --------------------- |
| 登录失败         | 确认账号密码 `admin/123456` 等；确认已执行 `init.sql`（账号是脚本种子数据）                  |                       |
| 页面中文乱码       | 确认执行 init.sql 时带 `--default-character-set=utf8mb4`；数据库/表字符集为 utf8mb4 |                       |
| 数据库连接失败      | 确认 MySQL 已启动、端口 3306、账号密码与 `DBUtil.java` 一致                          |                       |
| 端口被占用        | 修改 `pom.xml` 中 Jetty 端口，或先 \`netstat -ano                            | findstr 8080\` 结束占用进程 |
| 生成订单/入库失败    | 确认订单处于正确状态（生成订单要求申请 APPROVED；入库要求订单 APPROVED/RECEIVING）；看控制台日志       |                       |
| 点按钮没反应/按钮看不到 | 用对应角色的账号登录（按钮按权限码控制，如 keeper 无"新建订单"按钮属正常）                           |                       |
