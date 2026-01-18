-- ========================================
-- 宠物酒店管理系统 (PHMS) 数据库补丁脚本
-- 版本: 1.1.x
-- 说明: 新增订单完成后的酒店评价/满意度（1-5分）
-- ========================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `biz_hotel_reviews` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` bigint UNSIGNED NOT NULL COMMENT '关联订单ID（唯一）',
  `hotel_id` bigint UNSIGNED NOT NULL COMMENT '酒店ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `score` tinyint NOT NULL COMMENT '满意度评分：1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  INDEX `idx_hotel_id` (`hotel_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店评价表';
