-- ========================================
-- 宠物酒店管理系统 (PHMS) 数据库补丁脚本
-- 版本: 1.1.x
-- 说明: 为宠物档案新增疫苗/驱虫信息字段
-- ========================================

SET NAMES utf8mb4;

ALTER TABLE `biz_pets`
  ADD COLUMN IF NOT EXISTS `rabies_vaccine_date` date DEFAULT NULL COMMENT '狂犬疫苗接种日期' AFTER `photo_url`,
  ADD COLUMN IF NOT EXISTS `deworming_date` date DEFAULT NULL COMMENT '驱虫日期' AFTER `rabies_vaccine_date`,
  ADD COLUMN IF NOT EXISTS `vaccine_notes` varchar(500) DEFAULT NULL COMMENT '疫苗/驱虫备注' AFTER `deworming_date`;
