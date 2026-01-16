package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.entity.CareLog;
import com.phms.vo.CareLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 宠物护理日志 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface CareLogMapper extends BaseMapper<CareLog> {

    @Select("<script>" +
	    "SELECT " +
	    "  cl.id, " +
	    "  cl.order_id AS orderId, " +
	    "  o.order_no AS orderNo, " +
			"  r.room_no AS roomNo, " +
	    "  (" +
	    "    SELECT GROUP_CONCAT(p.name SEPARATOR '、') " +
	    "    FROM biz_pets p " +
	    "    WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') " +
	    "    AND p.is_deleted = 0" +
	    "  ) AS petName, " +
	    "  CASE cl.care_type " +
	    "    WHEN 1 THEN 'feeding' " +
	    "    WHEN 2 THEN 'walking' " +
	    "    WHEN 3 THEN 'cleaning' " +
	    "    WHEN 4 THEN 'health_check' " +
	    "    ELSE 'other' " +
	    "  END AS logType, " +
	    "  cl.content, " +
	    "  cl.images, " +
	    "  s.real_name AS staffName, " +
	    "  cl.created_at AS createdAt " +
	    "FROM biz_care_logs cl " +
	    "LEFT JOIN biz_orders o ON cl.order_id = o.id " +
			"LEFT JOIN biz_rooms r ON o.room_id = r.id " +
	    "LEFT JOIN sys_staff s ON cl.staff_id = s.id " +
	    "WHERE cl.is_deleted = 0 " +
	    "AND o.is_deleted = 0 " +
	    "AND o.status = 2 " +
	    "<if test='hotelId != null'> AND o.hotel_id = #{hotelId} </if>" +
	    "<if test='orderNo != null and orderNo != \"\"'> AND o.order_no LIKE CONCAT('%', #{orderNo}, '%') </if>" +
	    "<if test='petName != null and petName != \"\"'> AND EXISTS (" +
	    "  SELECT 1 FROM biz_pets p " +
	    "  WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') " +
	    "  AND p.is_deleted = 0 " +
	    "  AND p.name LIKE CONCAT('%', #{petName}, '%')" +
	    ") </if>" +
	    "<if test='careType != null'> AND cl.care_type = #{careType} </if>" +
	    "ORDER BY cl.created_at DESC" +
	    "</script>")
    Page<CareLogVO> selectCareLogVOPage(Page<CareLogVO> page,
				       @Param("hotelId") Long hotelId,
				       @Param("orderNo") String orderNo,
				       @Param("petName") String petName,
				       @Param("careType") Integer careType);

	    @Select("SELECT cl.id, cl.order_id AS orderId, o.order_no AS orderNo, r.room_no AS roomNo, " +
		    "(SELECT GROUP_CONCAT(p.name SEPARATOR '、') FROM biz_pets p WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') AND p.is_deleted = 0) AS petName, " +
		    "CASE cl.care_type WHEN 1 THEN 'feeding' WHEN 2 THEN 'walking' WHEN 3 THEN 'cleaning' WHEN 4 THEN 'health_check' WHEN 5 THEN 'check_in' ELSE 'other' END AS careType, " +
		    "cl.content, cl.images, s.real_name AS staffName, cl.created_at AS createdAt " +
		    "FROM biz_care_logs cl " +
		    "LEFT JOIN biz_orders o ON cl.order_id = o.id " +
		    "LEFT JOIN biz_rooms r ON o.room_id = r.id " +
		    "LEFT JOIN sys_staff s ON cl.staff_id = s.id " +
		    "WHERE cl.is_deleted = 0 AND o.is_deleted = 0 AND cl.order_id = #{orderId} " +
		    "ORDER BY cl.created_at DESC")
		List<CareLogVO> selectCareLogVOByOrderId(@Param("orderId") Long orderId);
}
