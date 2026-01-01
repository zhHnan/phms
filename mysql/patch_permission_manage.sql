-- ========================================
-- 权限管理功能补丁脚本
-- 用于已有数据库添加角色表和权限管理相关权限
-- ========================================

-- 创建角色表
CREATE TABLE IF NOT EXISTS `sys_roles` (
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

-- 插入角色数据（忽略重复）
INSERT IGNORE INTO `sys_roles` (`id`, `role_name`, `role_code`, `description`, `sort_order`, `status`) VALUES
(1, '普通员工', 'STAFF', '门店普通员工，负责日常运营工作', 1, 1),
(2, '店长', 'MANAGER', '门店店长，负责门店管理工作', 2, 1),
(9, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 9, 1);

-- 修改 sys_role_permissions 表字段名（如果存在旧字段）
-- 注意：如果表已存在 role_id 字段则跳过
-- ALTER TABLE `sys_role_permissions` CHANGE COLUMN `role_type` `role_id` tinyint UNSIGNED NOT NULL COMMENT '角色ID';

-- 添加角色管理和权限管理权限记录（忽略重复）
INSERT IGNORE INTO `sys_permissions` (`perm_code`, `perm_name`, `module`, `sort_order`) VALUES
('role:list', '角色列表', 'role', 65),
('role:add', '新增角色', 'role', 66),
('role:edit', '编辑角色', 'role', 67),
('role:delete', '删除角色', 'role', 68),
('permission:manage', '权限管理', 'permission', 70);

-- 为超管添加角色和权限管理权限
INSERT IGNORE INTO `sys_role_permissions` (`role_id`, `perm_id`)
SELECT 9, id FROM `sys_permissions` WHERE `perm_code` IN ('role:list', 'role:add', 'role:edit', 'role:delete', 'permission:manage');
