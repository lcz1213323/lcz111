-- ============================================================
-- Mini ERP — 一次性 DB 补丁（MySQL 8.0 正确语法）
-- 将数据库 schema 与代码对齐：缺失列 / 权限种子 / 存储过程 / 测试用户
-- ============================================================
USE mini_erp;

-- 1. purchase_order 缺失列
ALTER TABLE purchase_order
    ADD COLUMN request_id BIGINT DEFAULT NULL COMMENT '来源采购申请ID' AFTER supplier_id,
    ADD COLUMN received_by BIGINT DEFAULT NULL COMMENT '入库操作人ID' AFTER created_by,
    ADD COLUMN received_at DATETIME DEFAULT NULL COMMENT '入库时间' AFTER received_by;

-- 2. 预置权限数据
INSERT IGNORE INTO sys_permission (id, permission_code, permission_name, url, parent_id, menu_type, icon, sort_order) VALUES
(1,  'menu:product',    '商品管理',   '/product',          0, 1, '📦', 1),
(2,  'menu:purchase',   '采购管理',   '/purchase_order',    0, 1, '🛒', 2),
(3,  'menu:request',    '采购申请',   '/purchase_request',  0, 1, '📋', 3),
(11, 'product:add',     '新增商品',   NULL, 1, 2, NULL, 1),
(12, 'product:edit',    '编辑商品',   NULL, 1, 2, NULL, 2),
(13, 'product:delete',  '删除商品',   NULL, 1, 2, NULL, 3),
(21, 'purchase:request','提交申请',   NULL, 2, 2, NULL, 1),
(22, 'purchase:approve','审批申请',   NULL, 2, 2, NULL, 2),
(23, 'purchase:order',  '生成订单',   NULL, 2, 2, NULL, 3),
(24, 'purchase:receive','入库确认',   NULL, 2, 2, NULL, 4),
(25, 'purchase:delete', '删除订单',   NULL, 2, 2, NULL, 5);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(1,1),(1,2),(1,3),(1,11),(1,12),(1,13),(1,21),(1,22),(1,23),(1,24),(1,25),
(2,3),(2,21),
(3,2),(3,24);

-- 3. 存储过程
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
    SELECT status INTO v_status FROM purchase_order WHERE id = p_order_id;
    IF v_status != 'APPROVED' AND v_status != 'RECEIVING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '订单状态不允许入库';
    END IF;
    UPDATE product p
    INNER JOIN purchase_order_detail d ON p.id = d.product_id
    SET p.stock_qty = p.stock_qty + d.quantity
    WHERE d.order_id = p_order_id;
    UPDATE purchase_order
    SET status = 'COMPLETED', received_by = p_receiver_id, received_at = NOW()
    WHERE id = p_order_id;
    INSERT INTO purchase_audit_log (business_type, business_id, operation, operator_id, operator_name, old_status, new_status)
    VALUES ('ORDER', p_order_id, 'RECEIVE', p_receiver_id, p_receiver_name, v_status, 'COMPLETED');
    COMMIT;
END //
DELIMITER ;

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
    SELECT product_id, quantity INTO v_product_id, v_quantity FROM purchase_request WHERE id = p_request_id;
    UPDATE purchase_request SET status = 'CONVERTED', approver_id = p_approver_id, approver_name = p_approver_name, approve_time = NOW()
    WHERE id = p_request_id AND status = 'APPROVED';
    SET v_order_no = CONCAT('PO', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
    SET v_total = v_quantity * p_unit_price;
    INSERT INTO purchase_order (order_no, supplier_id, order_date, total_amount, status, created_by, request_id)
    VALUES (v_order_no, p_supplier_id, CURDATE(), v_total, 'APPROVED', p_approver_id, p_request_id);
    SET p_order_id = LAST_INSERT_ID();
    INSERT INTO purchase_order_detail (order_id, product_id, quantity, unit_price, line_total)
    VALUES (p_order_id, v_product_id, v_quantity, p_unit_price, v_total);
    INSERT INTO purchase_audit_log (business_type, business_id, operation, operator_id, operator_name, old_status, new_status)
    VALUES ('REQUEST', p_request_id, 'CONVERT', p_approver_id, p_approver_name, 'APPROVED', 'CONVERTED');
    COMMIT;
END //
DELIMITER ;

-- 4. 补充 keeper 测试用户（库管员）
INSERT INTO sys_user (id, username, password, real_name, status)
VALUES (3, 'keeper', '123456', '库管员小李', 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (3, 3);
