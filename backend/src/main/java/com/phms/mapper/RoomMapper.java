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
     * 分页查询可用房间列表（关联酒店名称）
     * 返回所有房间，不考虑状态和订单冲突（详情页会单独检查可用性）
     */
    @Select("<script>" +
            "SELECT r.id, r.hotel_id, r.room_no, r.type_name, r.price_per_night, " +
            "r.max_pet_num, r.features, r.description, r.images, r.is_deleted, " +
            "r.created_at, r.updated_at, h.name as hotel_name, r.status " +
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

    /**
     * 查询房间详情（关联酒店名称与地址）
     */
    @Select("SELECT r.*, h.name as hotel_name, h.address as hotel_address " +
            "FROM biz_rooms r " +
            "LEFT JOIN sys_hotels h ON r.hotel_id = h.id " +
            "WHERE r.id = #{id} AND r.is_deleted = 0")
    Room selectByIdWithHotel(@Param("id") Long id);

    /**
     * 检查房间在指定时间段是否有订单冲突
     * 返回冲突订单数量，0表示无冲突（可预订）
     * 冲突条件：新订单的退房日期 > 现有订单的入住日期 AND 新订单的入住日期 < 现有订单的退房日期
     * 状态说明：0=待支付, 1=待入住, 2=入住中 都算占用房间
     */
    @Select("SELECT COUNT(1) FROM biz_orders o " +
            "WHERE o.room_id = #{roomId} " +
            "AND o.is_deleted = 0 " +
            "AND o.status IN (0, 1, 2) " +  // 待支付、待入住、入住中
            "AND #{checkOutDate} > o.check_in_date " +
            "AND #{checkInDate} < o.check_out_date")
    int countConflictOrders(@Param("roomId") Long roomId,
                           @Param("checkInDate") LocalDate checkInDate,
                           @Param("checkOutDate") LocalDate checkOutDate);
}