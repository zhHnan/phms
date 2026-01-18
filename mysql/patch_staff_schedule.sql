-- 员工排班与在岗状态表

DROP TABLE IF EXISTS `sys_staff_schedule`;
CREATE TABLE `sys_staff_schedule` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `staff_id` bigint UNSIGNED NOT NULL COMMENT '员工ID',
  `hotel_id` bigint UNSIGNED DEFAULT NULL COMMENT '门店ID',
  `work_date` date NOT NULL COMMENT '排班日期',
  `shift_type` tinyint NOT NULL COMMENT '班次：1=早班 2=中班 3=晚班 4=全天班 5=休息',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_staff_date` (`staff_id`, `work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工排班表';

DROP TABLE IF EXISTS `sys_staff_status`;
CREATE TABLE `sys_staff_status` (
  `staff_id` bigint UNSIGNED NOT NULL COMMENT '员工ID',
  `hotel_id` bigint UNSIGNED DEFAULT NULL COMMENT '门店ID',
  `status` tinyint NOT NULL DEFAULT 4 COMMENT '状态：1=在岗 2=离岗 3=忙碌 4=离线',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工在岗状态表';
