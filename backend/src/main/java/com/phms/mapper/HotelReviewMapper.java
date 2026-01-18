package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.HotelReview;
import com.phms.vo.RoomReviewStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HotelReviewMapper extends BaseMapper<HotelReview> {

	@Select("SELECT r.id AS roomId, r.room_no AS roomNo, r.type_name AS roomType, " +
			"COUNT(hr.id) AS reviewCount, AVG(hr.score) AS avgScore " +
			"FROM biz_rooms r " +
			"LEFT JOIN biz_orders o ON o.room_id = r.id AND o.is_deleted = 0 " +
			"LEFT JOIN biz_hotel_reviews hr ON hr.order_id = o.id AND hr.is_deleted = 0 " +
			"WHERE r.is_deleted = 0 AND r.hotel_id = #{hotelId} " +
			"GROUP BY r.id, r.room_no, r.type_name")
	List<RoomReviewStatVO> selectRoomStatsByHotel(@Param("hotelId") Long hotelId);

	@Select("SELECT IFNULL(AVG(score),0) FROM biz_hotel_reviews WHERE is_deleted = 0 AND hotel_id = #{hotelId}")
	Double selectHotelAvgScore(@Param("hotelId") Long hotelId);

	@Select("SELECT COUNT(*) FROM biz_hotel_reviews WHERE is_deleted = 0 AND hotel_id = #{hotelId}")
	Long selectHotelReviewCount(@Param("hotelId") Long hotelId);

	@Select("SELECT hr.*, u.nickname AS user_name FROM biz_hotel_reviews hr " +
			"LEFT JOIN sys_users u ON u.id = hr.user_id " +
			"WHERE hr.is_deleted = 0 AND hr.hotel_id = #{hotelId} " +
			"ORDER BY hr.created_at DESC LIMIT #{limit}")
	java.util.List<HotelReview> selectReviewsWithUserName(@Param("hotelId") Long hotelId,
			@Param("limit") Integer limit);
}
