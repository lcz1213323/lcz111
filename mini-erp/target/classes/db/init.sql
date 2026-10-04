-- ============================================================
-- Mini ERP — 数据库初始化脚本（MySQL 8.0 唯一权威入口）
-- 在已有的 mini_erp 数据库（含基础表）之上补齐：
--   RBAC 权限表、采购申请/审计表、purchase_order 增强列、
--   索引、存储过程、审计触发器、测试账号。
-- 执行方式: mysql -u root -plczimxy233 --default-character-set=utf8mb4 mini_erp < init.sql
-- 幂等：可重复执行。
-- ============================================================
USE mini_erp;

-- ============================================================
-- 一、新增表
-- ============================================================

-- 权限表（菜单 + 按钮）
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码，如 product:add',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称，如 新增商品',
    url VARCHAR(200) DEFAULT NULL COMMENT '菜单URL，按钮级为NULL',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID，0=顶级菜单',
    menu_type TINYINT DEFAULT 1 COMMENT '1=菜单 2=按钮',
    icon VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_type (menu_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 采购申请表
CREATE TABLE IF NOT EXISTS purchase_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no VARCHAR(30) NOT NULL UNIQUE COMMENT '申请编号 PR开头',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    product_id BIGINT NOT NULL COMMENT '申请商品ID',
    product_name VARCHAR(100) COMMENT '商品名称',
    quantity INT NOT NULL COMMENT '申请数量',
    estimated_price DECIMAL(10,2) COMMENT '预估单价',
    reason VARCHAR(500) COMMENT '申请原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CONVERTED',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) DEFAULT NULL,
    approve_comment VARCHAR(500) DEFAULT NULL,
    approve_time DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_applicant (applicant_id),
    INDEX idx_product (product_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 采购审计日志表
CREATE TABLE IF NOT EXISTS purchase_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_type VARCHAR(20) NOT NULL COMMENT 'REQUEST/ORDER/RECEIVE',
    business_id BIGINT NOT NULL COMMENT '业务单据ID',
    operation VARCHAR(20) NOT NULL COMMENT 'CREATE/APPROVE/REJECT/CONVERT/RECEIVE/STATUS_CHANGE',
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50),
    old_status VARCHAR(20) DEFAULT NULL,
    new_status VARCHAR(20) DEFAULT NULL,
    comment VARCHAR(500) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_business (business_type, business_id),
    INDEX idx_created_at (created_at),
    INDEX idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 二、purchase_order 增强列（幂等，缺才加）
-- ============================================================

DROP PROCEDURE IF EXISTS sp_add_column;
DELIMITER //
CREATE PROCEDURE sp_add_column(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(200)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = p_table
                     AND COLUMN_NAME = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_definition);
        PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
    END IF;
END //
DELIMITER ;

CALL sp_add_column('purchase_order', 'request_id',  'request_id BIGINT DEFAULT NULL COMMENT ''来源采购申请ID''');
CALL sp_add_column('purchase_order', 'received_by', 'received_by BIGINT DEFAULT NULL COMMENT ''入库操作人ID''');
CALL sp_add_column('purchase_order', 'received_at', 'received_at DATETIME DEFAULT NULL COMMENT ''入库时间''');
DROP PROCEDURE IF EXISTS sp_add_column;

-- ============================================================
-- 三、高频查询索引（幂等，缺才加；基础表已带的不重复添加）
-- ============================================================

DROP PROCEDURE IF EXISTS sp_add_index;
DELIMITER //
CREATE PROCEDURE sp_add_index(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_definition VARCHAR(200)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = p_table
                     AND INDEX_NAME = p_index) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` ', p_definition);
        PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
    END IF;
END //
DELIMITER ;

-- 采购订单按来源申请、订单号、状态+时间排序（订单号已有唯一索引时跳过）
CALL sp_add_index('purchase_order', 'idx_request_id', '(request_id)');
-- 商品分类、编码、名称搜索
CALL sp_add_index('product', 'idx_category', '(category)');
-- 供应商名称搜索
CALL sp_add_index('supplier', 'idx_supplier_name', '(supplier_name)');
DROP PROCEDURE IF EXISTS sp_add_index;

-- ============================================================
-- 四、预置权限数据（先清后插，保证最终状态一致）
-- ============================================================

DELETE FROM sys_role_permission WHERE permission_id IN (SELECT id FROM sys_permission);
DELETE FROM sys_permission;

INSERT INTO sys_permission (id, permission_code, permission_name, url, parent_id, menu_type, icon, sort_order) VALUES
-- 菜单级权限 (menu_type=1)
(1,  'menu:product',    '商品管理',   '/product',          0, 1, '📦', 1),
(2,  'menu:purchase',   '采购管理',   '/purchase_order',    0, 1, '🛒', 2),
(3,  'menu:request',    '采购申请',   '/purchase_request',  0, 1, '📋', 3),
-- 按钮级权限 (menu_type=2)
(11, 'product:add',     '新增商品',   NULL, 1, 2, NULL, 1),
(12, 'product:edit',    '编辑商品',   NULL, 1, 2, NULL, 2),
(13, 'product:delete',  '删除商品',   NULL, 1, 2, NULL, 3),
(21, 'purchase:request','提交申请',   NULL, 2, 2, NULL, 1),
(22, 'purchase:approve','审批申请',   NULL, 2, 2, NULL, 2),
(23, 'purchase:order',  '生成订单',   NULL, 2, 2, NULL, 3),
(24, 'purchase:receive','入库确认',   NULL, 2, 2, NULL, 4),
(25, 'purchase:delete', '删除订单',   NULL, 2, 2, NULL, 5);

-- 分配权限给角色（1=管理员 2=采购员 3=库管员）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- 管理员：全部权限
(1,1),(1,2),(1,3),(1,11),(1,12),(1,13),(1,21),(1,22),(1,23),(1,24),(1,25),
-- 采购员：采购申请
(2,3),(2,21),
-- 库管员：采购管理 + 入库确认
(3,2),(3,24);

-- ============================================================
-- 五、存储过程（事务：审批转单 / 入库确认）
-- 说明：审计日志统一由第六部分触发器写入，此处不再手动 INSERT。
-- ============================================================

-- 5.1 审批通过后从申请生成采购订单（生成 DRAFT，随后单独审批）
DROP PROCEDURE IF EXISTS sp_approve_and_create_order;
DELIMITER //
CREATE PROCEDURE sp_approve_and_create_order(
    IN p_request_id BIGINT,
    IN p_approver_id BIGINT,
    IN p_approver_name VARCHAR(50),
    IN p_supplier_id BIGINT,
    IN p_unit_price DECIMAL(10,2),
    OUT p_order_id BIGINT
)
BEGIN
    DECLARE v_product_id BIGINT;
    DECLARE v_quantity INT;
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_order_no VARCHAR(30);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 1. 取申请的商品与数量
    SELECT product_id, quantity INTO v_product_id, v_quantity
    FROM purchase_request WHERE id = p_request_id;

    -- 2. 申请状态 APPROVED -> CONVERTED（触发器自动记审计）
    UPDATE purchase_request SET status = 'CONVERTED',
        approver_id = p_approver_id, approver_name = p_approver_name,
        approve_time = NOW()
    WHERE id = p_request_id AND status = 'APPROVED';

    -- 3. 生成订单号并计算总额
    SET v_order_no = CONCAT('PO', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
    SET v_total = v_quantity * p_unit_price;

    -- 4. 创建采购订单（DRAFT，之后由审批动作流转）
    INSERT INTO purchase_order (order_no, supplier_id, order_date,
        total_amount, status, created_by, request_id)
    VALUES (v_order_no, p_supplier_id, CURDATE(), v_total,
        'DRAFT', p_approver_id, p_request_id);

    SET p_order_id = LAST_INSERT_ID();

    -- 5. 创建订单明细
    INSERT INTO purchase_order_detail (order_id, product_id,
        quantity, unit_price, line_total)
    VALUES (p_order_id, v_product_id, v_quantity, p_unit_price, v_total);

    COMMIT;
END //
DELIMITER ;

-- 5.2 收货入库确认（更新库存 + 订单完成，触发器自动记审计）
DROP PROCEDURE IF EXISTS sp_receive_order;
DELIMITER //
CREATE PROCEDURE sp_receive_order(
    IN p_order_id BIGINT,
    IN p_receiver_id BIGINT,
    IN p_receiver_name VARCHAR(50)
)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 校验订单状态
    SELECT status INTO v_status FROM purchase_order WHERE id = p_order_id;
    IF v_status != 'APPROVED' AND v_status != 'RECEIVING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '订单状态不允许入库';
    END IF;

    -- 从订单明细回补库存
    UPDATE product p
    INNER JOIN purchase_order_detail d ON p.id = d.product_id
    SET p.stock_qty = p.stock_qty + d.quantity
    WHERE d.order_id = p_order_id;

    -- 订单完成（触发器自动记审计 RECEIVE）
    UPDATE purchase_order
    SET status = 'COMPLETED', received_by = p_receiver_id, received_at = NOW()
    WHERE id = p_order_id;

    COMMIT;
END //
DELIMITER ;

-- ============================================================
-- 六、审计触发器（状态变更自动写入 purchase_audit_log）
-- 应用层 DAO 不再手动写审计，避免重复记录。
-- ============================================================

-- 6.1 采购申请状态变更
DROP TRIGGER IF EXISTS trg_purchase_request_status_change;
DELIMITER //
CREATE TRIGGER trg_purchase_request_status_change
AFTER UPDATE ON purchase_request
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        INSERT INTO purchase_audit_log (business_type, business_id,
            operation, operator_id, operator_name, old_status, new_status, comment)
        VALUES ('REQUEST', NEW.id,
            CASE NEW.status
                WHEN 'APPROVED' THEN 'APPROVE'
                WHEN 'REJECTED' THEN 'REJECT'
                WHEN 'CONVERTED' THEN 'CONVERT'
                ELSE 'UPDATE'
            END,
            COALESCE(NEW.approver_id, NEW.applicant_id),
            COALESCE(NEW.approver_name, NEW.applicant_name),
            OLD.status, NEW.status, NEW.approve_comment);
    END IF;
END //
DELIMITER ;

-- 6.2 采购订单状态变更（审批 / 入库）
DROP TRIGGER IF EXISTS trg_purchase_order_status_change;
DELIMITER //
CREATE TRIGGER trg_purchase_order_status_change
AFTER UPDATE ON purchase_order
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        INSERT INTO purchase_audit_log (business_type, business_id,
            operation, operator_id, operator_name, old_status, new_status)
        VALUES ('ORDER', NEW.id,
            CASE NEW.status
                WHEN 'APPROVED' THEN 'APPROVE'
                WHEN 'COMPLETED' THEN 'RECEIVE'
                ELSE 'STATUS_CHANGE'
            END,
            COALESCE(NEW.received_by, NEW.approved_by, NEW.created_by),
            (SELECT real_name FROM sys_user
             WHERE id = COALESCE(NEW.received_by, NEW.approved_by, NEW.created_by)),
            OLD.status, NEW.status);
    END IF;
END //
DELIMITER ;

-- ============================================================
-- 七、测试账号（幂等）
-- ============================================================

INSERT INTO sys_user (id, username, password, real_name, status)
VALUES (1, 'admin', '123456', '超级管理员', 1),
       (2, 'buyer', '123456', '采购员小王', 1),
       (3, 'keeper', '123456', '库管员小李', 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), status = VALUES(status);

-- admin -> 管理员(1), buyer -> 采购员(2), keeper -> 库管员(3)
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1,1),(2,2),(3,3);
