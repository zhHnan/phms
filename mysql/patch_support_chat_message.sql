-- 添加客服聊天记录表
CREATE TABLE IF NOT EXISTS `support_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `staff_id` BIGINT NOT NULL COMMENT '客服ID',
  `sender` VARCHAR(16) NOT NULL COMMENT '发送方 user/staff',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `room_id` BIGINT NULL DEFAULT NULL COMMENT '房间ID',
  `hotel_id` BIGINT NULL DEFAULT NULL COMMENT '酒店ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_staff_time` (`user_id`, `staff_id`, `created_at`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服聊天记录';
