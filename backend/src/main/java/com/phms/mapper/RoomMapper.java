package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
