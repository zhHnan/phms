-- 为护理日志增加门店ID与图片字段（用于门店权限过滤与多图上传）
-- 注意：若表内已有历史数据，需先允许NULL并回填，再改为NOT NULL
ALTER TABLE biz_care_logs
  ADD COLUMN hotel_id bigint UNSIGNED NULL COMMENT '关联门店ID' AFTER order_id,
  ADD COLUMN images text DEFAULT NULL COMMENT '图片URL列表（逗号分隔）' AFTER content;

-- 回填历史数据
UPDATE biz_care_logs cl
JOIN biz_orders o ON cl.order_id = o.id
SET cl.hotel_id = o.hotel_id
WHERE cl.hotel_id IS NULL;

-- 约束为 NOT NULL
ALTER TABLE biz_care_logs
  MODIFY COLUMN hotel_id bigint UNSIGNED NOT NULL COMMENT '关联门店ID';

-- 可选：更新护理类型注释（仅注释，不影响数据）
ALTER TABLE biz_care_logs
  MODIFY COLUMN care_type tinyint NOT NULL COMMENT '1=喂食 2=遛弯 3=清洁 4=体检 5=其他';

DESC biz_care_logs;
