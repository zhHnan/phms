-- ----------------------------
-- 消息中心表 (sys_message_center)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_message_center` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint UNSIGNED DEFAULT NULL COMMENT '所属门店ID（平台消息为NULL）',
  `receiver_type` tinyint NOT NULL COMMENT '接收方类型：1=C端用户 2=B端员工',
  `receiver_id` bigint UNSIGNED DEFAULT NULL COMMENT '接收方ID（sys_users.id或sys_staff.id，可为空，表示门店共享消息）',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint DEFAULT 0 COMMENT '是否已读：0=未读 1=已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_hotel_id` (`hotel_id`),
  INDEX `idx_receiver` (`receiver_type`, `receiver_id`),
  INDEX `idx_receiver_read` (`receiver_type`, `receiver_id`, `is_read`),
  INDEX `idx_hotel_read` (`hotel_id`, `is_read`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心表';

-- 兼容旧表结构：将 receiver_id 改为可空（门店共享消息）
ALTER TABLE `sys_message_center`
  MODIFY COLUMN `receiver_id` bigint UNSIGNED NULL COMMENT '接收方ID（sys_users.id或sys_staff.id，可为空，表示门店共享消息）';
