package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.entity.Order;
import com.phms.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表（带酒店和房间信息）
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "h.name AS hotelName, " +
            "u.nickname AS userName, " +
            "u.phone AS userPhone, " +
            "r.room_no AS roomNo, " +
            "r.type_name AS roomType, " +
            "r.price_per_night AS roomPrice, " +
            "hr.score AS reviewScore, " +
            "hr.content AS reviewContent, " +
            "hr.created_at AS reviewCreatedAt, " +
            "DATEDIFF(o.check_out_date, o.check_in_date) AS days, " +
            "(" +
            "  SELECT GROUP_CONCAT(p.name SEPARATOR '、') " +
            "  FROM biz_pets p " +
            "  WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') " +
            "  AND p.is_deleted = 0" +
            ") AS petNames " +
            "FROM biz_orders o " +
            "LEFT JOIN sys_hotels h ON o.hotel_id = h.id " +
            "LEFT JOIN sys_users u ON o.user_id = u.id " +
            "LEFT JOIN biz_rooms r ON o.room_id = r.id " +
            "LEFT JOIN biz_hotel_reviews hr ON hr.order_id = o.id AND hr.is_deleted = 0 " +
            "WHERE o.is_deleted = 0 " +
            "<if test='hotelId != null'> AND o.hotel_id = #{hotelId} </if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND (o.order_no LIKE CONCAT('%', #{orderNo}, '%') OR r.room_no LIKE CONCAT('%', #{orderNo}, '%')) </if>" +
            "<if test='petName != null and petName != \"\"'> AND EXISTS (" +
            "  SELECT 1 FROM biz_pets p " +
            "  WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') " +
            "  AND p.is_deleted = 0 " +
            "  AND p.name LIKE CONCAT('%', #{petName}, '%')" +
            ") </if>" +
            "<if test='userId != null'> AND o.user_id = #{userId} </if>" +
            "<if test='status != null'> AND o.status = #{status} </if>" +
            "ORDER BY o.created_at DESC" +
            "</script>")
    Page<OrderVO> selectOrderVOPage(Page<OrderVO> page,
                                     @Param("hotelId") Long hotelId,
                                     @Param("orderNo") String orderNo,
                                     @Param("petName") String petName,
                                     @Param("userId") Long userId,
                                     @Param("status") Integer status);

    /**
     * 根据ID查询订单详情（带酒店和房间信息）
     */
    @Select("SELECT o.*, " +
                        "h.name AS hotelName, " +
                        "u.nickname AS userName, " +
                        "u.phone AS userPhone, " +
                        "r.room_no AS roomNo, " +
                        "r.type_name AS roomType, " +
                        "r.price_per_night AS roomPrice, " +
                        "hr.score AS reviewScore, " +
                        "hr.content AS reviewContent, " +
                        "hr.created_at AS reviewCreatedAt, " +
                        "DATEDIFF(o.check_out_date, o.check_in_date) AS days, " +
                        "(" +
                        "  SELECT GROUP_CONCAT(p.name SEPARATOR '、') " +
                        "  FROM biz_pets p " +
                        "  WHERE JSON_CONTAINS(o.pet_ids, CAST(p.id AS JSON), '$') " +
                        "  AND p.is_deleted = 0" +
                        ") AS petNames " +
            "FROM biz_orders o " +
            "LEFT JOIN sys_hotels h ON o.hotel_id = h.id " +
            "LEFT JOIN sys_users u ON o.user_id = u.id " +
                        "LEFT JOIN biz_rooms r ON o.room_id = r.id " +
                        "LEFT JOIN biz_hotel_reviews hr ON hr.order_id = o.id AND hr.is_deleted = 0 " +
            "WHERE o.id = #{id} AND o.is_deleted = 0")
    OrderVO selectOrderVOById(@Param("id") Long id);
}
