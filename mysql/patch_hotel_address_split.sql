-- 为门店地址增加省市区编码与详细地址字段，用于级联回填
ALTER TABLE sys_hotels
  ADD COLUMN province_code varchar(20) DEFAULT NULL COMMENT '省编码' AFTER address,
  ADD COLUMN city_code varchar(20) DEFAULT NULL COMMENT '市编码' AFTER province_code,
  ADD COLUMN district_code varchar(20) DEFAULT NULL COMMENT '区/县编码' AFTER city_code,
  ADD COLUMN address_detail varchar(255) DEFAULT NULL COMMENT '详细地址（门牌号/楼层等）' AFTER district_code;

DESC sys_hotels;
