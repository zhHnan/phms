-- 为房间表添加图片字段
-- 使用 JSON 类型存储图片 URL 列表

USE phms;

-- 添加 images 字段
ALTER TABLE biz_rooms ADD COLUMN images JSON DEFAULT NULL COMMENT '房间图片URL列表（JSON数组）' AFTER description;

-- 更新示例数据（可选）
UPDATE biz_rooms SET images = '[]' WHERE images IS NULL;
