-- 为酒店表添加图片字段
-- 执行命令：mysql -u phms -p phms < mysql/patch_hotel_images.sql

USE phms;

-- 添加酒店图片字段
ALTER TABLE sys_hotels 
ADD COLUMN images JSON DEFAULT NULL COMMENT '酒店图片URL列表（JSON数组）' 
AFTER status;

-- 验证
DESC sys_hotels;
