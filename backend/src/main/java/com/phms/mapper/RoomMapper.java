package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 房间配置 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    
    /**
     * 分页查询房间列表（关联酒店名称）
     */
    @Select("<script>" +
            "SELECT r.*, h.name as hotel_name FROM biz_rooms r " +
            "LEFT JOIN sys_hotels h ON r.hotel_id = h.id " +
            "WHERE r.is_deleted = 0 " +
            "<if test='hotelId != null'> AND r.hotel_id = #{hotelId} </if>" +
            "<if test='typeName != null and typeName != \"\"'> AND r.type_name LIKE CONCAT('%', #{typeName}, '%') </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='roomNo != null and roomNo != \"\"'> AND r.room_no LIKE CONCAT('%', #{roomNo}, '%') </if>" +
            "ORDER BY r.room_no ASC" +
            "</script>")
    Page<Room> selectPageWithHotel(Page<Room> page,
                                   @Param("hotelId") Long hotelId,
                                   @Param("typeName") String typeName,
                                   @Param("status") Integer status,
                                   @Param("roomNo") String roomNo);

    /**
     * 分页查询可用房间列表（关联酒店名称，检查指定时间段是否有订单冲突）
     */
    @Select("<script>" +
            "SELECT r.id, r.hotel_id, r.room_no, r.type_name, r.price_per_night, " +
            "r.max_pet_num, r.features, r.description, r.is_deleted, " +
            "r.created_at, r.updated_at, h.name as hotel_name, " +
            // 检查是否有时间段冲突的订单
            "<if test='checkInDate != null and checkOutDate != null'>" +
            "  CASE WHEN EXISTS (" +
            "    SELECT 1 FROM biz_orders o " +
            "    WHERE o.room_id = r.id " +
            "    AND o.is_deleted = 0 " +
            "    AND o.status IN (1, 2) " +  // 待入住、入住中（不包括待支付，因为未付款可能取消）
            "    AND NOT (" +
            "      o.check_out_date &lt;= #{checkInDate} OR o.check_in_date &gt;= #{checkOutDate}" +
            "    )" +
            "  ) THEN 1 ELSE r.status END " +
            "</if>" +
            "<if test='checkInDate == null or checkOutDate == null'>" +
            "  r.status " +
            "</if>" +
            "as status " +
            "FROM biz_rooms r " +
            "LEFT JOIN sys_hotels h ON r.hotel_id = h.id " +
            "WHERE r.is_deleted = 0 " +
            "<if test='hotelId != null'> AND r.hotel_id = #{hotelId} </if>" +
            "<if test='roomType != null and roomType != \"\"'> AND r.type_name LIKE CONCAT('%', #{roomType}, '%') </if>" +
            "ORDER BY r.price_per_night ASC" +
            "</script>")
    Page<Room> selectAvailableRoomsWithHotel(Page<Room> page,
                                             @Param("hotelId") Long hotelId,
                                             @Param("roomType") String roomType,
                                             @Param("checkInDate") LocalDate checkInDate,
                                             @Param("checkOutDate") LocalDate checkOutDate);
}
