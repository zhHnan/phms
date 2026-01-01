-- ========================================
-- 宠物酒店管理系统 (PHMS) 数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2024
-- ========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 门店信息表 (sys_hotels)
-- ----------------------------
DROP TABLE IF EXISTS `sys_hotels`;
CREATE TABLE `sys_hotels` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '门店名称',
  `code` varchar(50) DEFAULT NULL COMMENT '门店编码（唯一标识）',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `manager_name` varchar(50) DEFAULT NULL COMMENT '店长姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT 1 COMMENT '1=营业 0=停业',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店信息表';

-- ----------------------------
-- 2. 内部员工表 (sys_staff)
-- ----------------------------
DROP TABLE IF EXISTS `sys_staff`;
CREATE TABLE `sys_staff` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint UNSIGNED DEFAULT NULL COMMENT '归属门店ID（超管为NULL）',
  `email` varchar(100) NOT NULL COMMENT '登录邮箱',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role_type` tinyint NOT NULL COMMENT '1=普通员工 2=店长 9=平台超管',
  `status` tinyint DEFAULT 1 COMMENT '1=正常 0=禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  INDEX `idx_hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部员工表';

-- ----------------------------
-- 3. C端用户表 (sys_users)
-- ----------------------------
DROP TABLE IF EXISTS `sys_users`;
CREATE TABLE `sys_users` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL COMMENT '手机号（登录/验证码）',
  `password` varchar(100) DEFAULT NULL COMMENT 'BCrypt加密密码（选填）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `status` tinyint DEFAULT 1 COMMENT '1=正常 0=禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户表';

-- ----------------------------
-- 4. 验证码记录表 (sys_verification_codes)
-- ----------------------------
DROP TABLE IF EXISTS `sys_verification_codes`;
CREATE TABLE `sys_verification_codes` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `target` varchar(20) NOT NULL COMMENT '手机号',
  `code` varchar(6) NOT NULL COMMENT '6位验证码',
  `type` tinyint NOT NULL COMMENT '1=登录 2=注册',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `is_used` tinyint DEFAULT 0 COMMENT '0=未使用 1=已使用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_target_type` (`target`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码记录表';

-- ----------------------------
-- 5. 登录日志表 (sys_login_logs)
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_logs`;
CREATE TABLE `sys_login_logs` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `login_type` tinyint NOT NULL COMMENT '登录主体类型：1=C端用户 2=B端员工',
  `user_id` bigint UNSIGNED DEFAULT NULL COMMENT 'C端用户ID（login_type=1时必填）',
  `staff_id` bigint UNSIGNED DEFAULT NULL COMMENT 'B端员工ID（login_type=2时必填）',
  `login_way` tinyint NOT NULL COMMENT '登录方式：1=手机号验证码 2=邮箱密码 3=第三方登录（预留）',
  `login_ip` varchar(50) NOT NULL COMMENT '登录IP地址',
  `device_info` varchar(255) DEFAULT NULL COMMENT '设备信息（如浏览器型号、手机品牌）',
  `login_status` tinyint NOT NULL COMMENT '登录状态：1=成功 0=失败',
  `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因（如验证码错误、密码不匹配）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_login_type` (`login_type`),
  INDEX `idx_create_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ----------------------------
-- 6. 操作日志表 (sys_operation_logs)
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_logs`;
CREATE TABLE `sys_operation_logs` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `operator_type` tinyint NOT NULL COMMENT '操作人类型：1=C端用户 2=B端员工',
  `operator_id` bigint UNSIGNED NOT NULL COMMENT '操作人ID（关联sys_users.id或sys_staff.id）',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名/昵称（冗余存储，便于查询）',
  `operation_module` varchar(50) NOT NULL COMMENT '操作模块：如order=订单管理、pet=宠物档案、room=房态管理',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：add=新增 update=修改 delete=删除 query=查询',
  `operation_param` text DEFAULT NULL COMMENT '操作入参（JSON格式字符串，完整保留请求参数）',
  `operation_result` tinyint NOT NULL COMMENT '操作结果：1=成功 0=失败',
  `fail_msg` varchar(255) DEFAULT NULL COMMENT '失败信息（如异常堆栈摘要）',
  `operation_ip` varchar(50) NOT NULL COMMENT '操作IP地址',
  `operation_url` varchar(255) DEFAULT NULL COMMENT '请求接口地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_operator` (`operator_type`, `operator_id`),
  INDEX `idx_module_time` (`operation_module`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 7. 房间配置表 (biz_rooms)
-- ----------------------------
DROP TABLE IF EXISTS `biz_rooms`;
CREATE TABLE `biz_rooms` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint UNSIGNED NOT NULL COMMENT '所属门店ID',
  `room_no` varchar(20) NOT NULL COMMENT '房间号（如A-101）',
  `type_name` varchar(50) NOT NULL COMMENT '房型（豪华猫屋/标准狗舍）',
  `price_per_night` decimal(10,2) NOT NULL COMMENT '每晚价格',
  `max_pet_num` tinyint DEFAULT 1 COMMENT '最大容纳宠物数',
  `features` json DEFAULT NULL COMMENT '设施标签（监控、空调等）',
  `status` tinyint DEFAULT 0 COMMENT '0=空闲 1=已预订 2=入住中 3=待清洁 4=维修',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hotel_room_no` (`hotel_id`, `room_no`),
  INDEX `idx_hotel_status` (`hotel_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- ----------------------------
-- 8. 宠物档案表 (biz_pets)
-- ----------------------------
DROP TABLE IF EXISTS `biz_pets`;
CREATE TABLE `biz_pets` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '主人ID',
  `name` varchar(50) NOT NULL COMMENT '宠物名字',
  `type` tinyint NOT NULL COMMENT '1=猫 2=狗 3=异宠',
  `weight` decimal(5,2) DEFAULT NULL COMMENT '宠物体重（kg）',
  `notes` varchar(500) DEFAULT NULL COMMENT '性格/健康备注',
  `photo_url` varchar(255) DEFAULT NULL COMMENT '宠物照片URL',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物档案表';

-- ----------------------------
-- 9. 订单表 (biz_orders)
-- ----------------------------
DROP TABLE IF EXISTS `biz_orders`;
CREATE TABLE `biz_orders` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号（唯一）',
  `hotel_id` bigint UNSIGNED NOT NULL COMMENT '门店ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `pet_id` bigint UNSIGNED NOT NULL COMMENT '宠物ID',
  `room_id` bigint UNSIGNED NOT NULL COMMENT '房间ID',
  `check_in_date` date NOT NULL COMMENT '预计入住日期',
  `check_out_date` date NOT NULL COMMENT '预计离店日期',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` tinyint DEFAULT 0 COMMENT '0=待支付 1=待入住 2=入住中 3=已完成 4=已取消',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_hotel_date` (`hotel_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 10. 宠物护理日志表 (biz_care_logs)
-- ----------------------------
DROP TABLE IF EXISTS `biz_care_logs`;
CREATE TABLE `biz_care_logs` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` bigint UNSIGNED NOT NULL COMMENT '关联订单ID',
  `staff_id` bigint UNSIGNED NOT NULL COMMENT '操作员工ID',
  `care_type` tinyint NOT NULL COMMENT '1=喂食 2=遛弯 3=清洁 4=体检',
  `content` varchar(500) DEFAULT NULL COMMENT '护理详情（如：喂食皇家狗粮100g）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物护理日志表';

-- ----------------------------
-- 11. 角色表 (sys_roles)
-- ----------------------------
DROP TABLE IF EXISTS `sys_roles`;
CREATE TABLE `sys_roles` (
  `id` tinyint UNSIGNED NOT NULL COMMENT '角色ID（与role_type一致）',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '1=正常 0=禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 12. 权限表 (sys_permissions)
-- ----------------------------
DROP TABLE IF EXISTS `sys_permissions`;
CREATE TABLE `sys_permissions` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(50) NOT NULL COMMENT '权限编码（如 hotel:list）',
  `perm_name` varchar(100) NOT NULL COMMENT '权限名称',
  `module` varchar(50) NOT NULL COMMENT '所属模块',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ----------------------------
-- 13. 角色权限关联表 (sys_role_permissions)
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permissions`;
CREATE TABLE `sys_role_permissions` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` tinyint UNSIGNED NOT NULL COMMENT '角色ID',
  `perm_id` bigint UNSIGNED NOT NULL COMMENT '权限ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 初始化数据
-- ========================================

-- 插入角色数据
INSERT INTO `sys_roles` (`id`, `role_name`, `role_code`, `description`, `sort_order`, `status`) VALUES
(1, '普通员工', 'STAFF', '门店普通员工，负责日常运营工作', 1, 1),
(2, '店长', 'MANAGER', '门店店长，负责门店管理工作', 2, 1),
(9, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 9, 1);

-- 插入默认超级管理员 (密码: admin123, BCrypt加密)
INSERT INTO `sys_staff` (`hotel_id`, `email`, `password`, `real_name`, `role_type`, `status`) VALUES
(NULL, 'admin@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 9, 1);

-- 插入示例门店
INSERT INTO `sys_hotels` (`name`, `code`, `address`, `manager_name`, `phone`, `status`) VALUES
('宠物之家·朝阳店', 'HOTEL001', '北京市朝阳区建国路88号', '张店长', '13800138001', 1),
('宠物之家·海淀店', 'HOTEL002', '北京市海淀区中关村大街66号', '李店长', '13800138002', 1),
('宠物之家·浦东店', 'HOTEL003', '上海市浦东新区陆家嘴环路100号', '王店长', '13800138003', 1);

-- 插入示例店长 (密码: manager123)
INSERT INTO `sys_staff` (`hotel_id`, `email`, `password`, `real_name`, `role_type`, `status`) VALUES
(1, 'manager1@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张店长', 2, 1),
(2, 'manager2@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李店长', 2, 1),
(3, 'manager3@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王店长', 2, 1);

-- 插入示例员工 (密码: staff123)
INSERT INTO `sys_staff` (`hotel_id`, `email`, `password`, `real_name`, `role_type`, `status`) VALUES
(1, 'staff1@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '朝阳店员工1', 1, 1),
(1, 'staff2@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '朝阳店员工2', 1, 1),
(2, 'staff3@phms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '海淀店员工1', 1, 1);

-- 插入示例房间
INSERT INTO `biz_rooms` (`hotel_id`, `room_no`, `type_name`, `price_per_night`, `max_pet_num`, `features`, `status`) VALUES
(1, 'A-101', '豪华猫屋', 198.00, 1, '["监控", "空调", "猫爬架"]', 0),
(1, 'A-102', '豪华猫屋', 198.00, 1, '["监控", "空调", "猫爬架"]', 0),
(1, 'B-101', '标准狗舍', 158.00, 1, '["监控", "空调"]', 0),
(1, 'B-102', '标准狗舍', 158.00, 1, '["监控", "空调"]', 0),
(1, 'C-101', '豪华狗舍', 258.00, 2, '["监控", "空调", "独立院子"]', 0),
(2, 'A-101', '豪华猫屋', 188.00, 1, '["监控", "空调", "猫爬架"]', 0),
(2, 'A-102', '豪华猫屋', 188.00, 1, '["监控", "空调", "猫爬架"]', 0),
(2, 'B-101', '标准狗舍', 148.00, 1, '["监控", "空调"]', 0),
(3, 'A-101', '豪华猫屋', 218.00, 1, '["监控", "空调", "猫爬架", "恒温系统"]', 0),
(3, 'B-101', '标准狗舍', 178.00, 1, '["监控", "空调"]', 0);

-- 插入示例C端用户 (密码: user123)
INSERT INTO `sys_users` (`phone`, `password`, `nickname`, `avatar`, `balance`, `status`) VALUES
('13900139001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '宠物爱好者小明', NULL, 500.00, 1),
('13900139002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '爱猫人士小红', NULL, 300.00, 1);

-- 插入示例宠物
INSERT INTO `biz_pets` (`user_id`, `name`, `type`, `weight`, `notes`, `photo_url`) VALUES
(1, '旺财', 2, 12.50, '性格活泼，喜欢玩球', NULL),
(1, '小花', 1, 4.20, '性格温顺，有点怕生', NULL),
(2, '咪咪', 1, 3.80, '英短蓝猫，需要每天梳毛', NULL);

-- 插入权限数据
INSERT INTO `sys_permissions` (`perm_code`, `perm_name`, `module`, `sort_order`) VALUES
('dashboard:view', '查看工作台', 'dashboard', 1),
('hotel:list', '门店列表', 'hotel', 10),
('hotel:add', '新增门店', 'hotel', 11),
('hotel:edit', '编辑门店', 'hotel', 12),
('hotel:delete', '删除门店', 'hotel', 13),
('staff:list', '员工列表', 'staff', 20),
('staff:add', '新增员工', 'staff', 21),
('staff:edit', '编辑员工', 'staff', 22),
('staff:delete', '删除员工', 'staff', 23),
('room:list', '房间列表', 'room', 30),
('room:add', '新增房间', 'room', 31),
('room:edit', '编辑房间', 'room', 32),
('room:delete', '删除房间', 'room', 33),
('room:status', '修改房间状态', 'room', 34),
('order:list', '订单列表', 'order', 40),
('order:checkin', '办理入住', 'order', 41),
('order:checkout', '办理退房', 'order', 42),
('care:list', '护理日志列表', 'care', 50),
('care:add', '新增护理日志', 'care', 51),
('log:login', '登录日志', 'log', 60),
('log:operation', '操作日志', 'log', 61),
('role:list', '角色列表', 'role', 65),
('role:add', '新增角色', 'role', 66),
('role:edit', '编辑角色', 'role', 67),
('role:delete', '删除角色', 'role', 68),
('permission:manage', '权限管理', 'permission', 70);

-- 超管权限 (role_id=9) - 拥有所有权限
INSERT INTO `sys_role_permissions` (`role_id`, `perm_id`) values (9, 23),
                                                                 (9, 24),
                                                                 (9, 25),
                                                                  (9, 26);


-- 店长权限 (role_id=2) - 默认不包含权限管理
INSERT INTO `sys_role_permissions` (`role_id`, `perm_id`)
SELECT 2, id FROM `sys_permissions` WHERE `perm_code` IN (
  'dashboard:view',
  'staff:list', 'staff:add', 'staff:edit',
  'room:list', 'room:add', 'room:edit', 'room:status',
  'order:list', 'order:checkin', 'order:checkout',
  'care:list', 'care:add',
  'log:login', 'log:operation'
);

-- 普通员工权限 (role_id=1)
INSERT INTO `sys_role_permissions` (`role_id`, `perm_id`)
SELECT 1, id FROM `sys_permissions` WHERE `perm_code` IN (
  'dashboard:view',
  'room:list',
  'order:list', 'order:checkin', 'order:checkout',
  'care:list', 'care:add'
);
