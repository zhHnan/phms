DROP TABLE IF EXISTS biz_products;
-- 商品管理相关表
CREATE TABLE IF NOT EXISTS biz_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    images VARCHAR(2000) DEFAULT NULL COMMENT '图片JSON',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1=上架 0=下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) COMMENT='酒店商品表';

DROP TABLE IF EXISTS biz_product_hotels;
CREATE TABLE IF NOT EXISTS biz_product_hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    hotel_id BIGINT NOT NULL COMMENT '门店ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_product_hotel (product_id, hotel_id)
) COMMENT='商品-门店关联';

DROP TABLE IF EXISTS biz_order_items;
CREATE TABLE IF NOT EXISTS biz_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) COMMENT='订单商品明细';

-- 权限
INSERT INTO `sys_permissions` (`perm_code`, `perm_name`, `module`, `sort_order`) VALUES
('product:list', '商品管理', 'product', 80),
('product:add', '商品新增', 'product', 81),
('product:edit', '商品编辑', 'product', 82),
('product:delete', '商品删除', 'product', 83),
('product:status', '商品上/下架', 'product', 84),
('product:stock', '商品库存调整', 'product', 85);

-- 绑定权限到管理员角色（role_id=9 为超管，role_id=2 为店长）
INSERT INTO `sys_role_permissions` (`role_id`, `perm_id`)
SELECT 9, id FROM `sys_permissions` WHERE `perm_code` LIKE 'product:%';
INSERT INTO `sys_role_permissions` (`role_id`, `perm_id`)
SELECT 2, id FROM `sys_permissions` WHERE `perm_code` LIKE 'product:%';
