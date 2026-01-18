-- ========================================
-- 宠物酒店管理系统 (PHMS) 数据库补丁脚本
-- 版本: 1.1.0
-- 说明: 添加订单表支持多宠物和备注功能
-- ========================================

SET NAMES utf8mb4;

-- 在订单表中添加新字段
ALTER TABLE `biz_orders` 
ADD COLUMN `pet_ids` json DEFAULT NULL COMMENT '宠物ID列表（JSON数组）' AFTER `pet_id`,
ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '订单备注' AFTER `total_amount`;

-- 删除旧的 pet_id 字段（已被 pet_ids 替代）
ALTER TABLE `biz_orders` 
DROP COLUMN IF EXISTS `pet_id`;

-- 更新room表，添加description字段（如果不存在）
ALTER TABLE `biz_rooms` 
ADD COLUMN IF NOT EXISTS `description` varchar(500) DEFAULT NULL COMMENT '房间描述' AFTER `features`;

-- 在宠物表中添加age字段（如果不存在）
ALTER TABLE `biz_pets` 
ADD COLUMN IF NOT EXISTS `age` int DEFAULT NULL COMMENT '宠物年龄（岁）' AFTER `type`;
