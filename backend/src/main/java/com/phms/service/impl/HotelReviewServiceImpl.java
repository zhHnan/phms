package com.phms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.HotelReview;
import com.phms.mapper.HotelReviewMapper;
import com.phms.service.HotelReviewService;
import com.phms.vo.HotelReviewSummaryVO;
import com.phms.vo.RoomReviewStatVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelReviewServiceImpl extends ServiceImpl<HotelReviewMapper, HotelReview> implements HotelReviewService {

	@Override
	public HotelReviewSummaryVO getHotelSummary(Long hotelId) {
		HotelReviewSummaryVO summary = new HotelReviewSummaryVO();
		summary.setHotelId(hotelId);

		// 房间维度统计
		List<RoomReviewStatVO> rooms = this.baseMapper.selectRoomStatsByHotel(hotelId);
		summary.setRooms(rooms);

		// 酒店整体均分与数量
		Double hotelAvg = this.baseMapper.selectHotelAvgScore(hotelId);
		Long hotelCount = this.baseMapper.selectHotelReviewCount(hotelId);

		summary.setHotelAvgScore(hotelAvg == null ? 0.0 : hotelAvg);
		summary.setHotelReviewCount(hotelCount == null ? 0 : hotelCount.intValue());
		return summary;
	}

	public List<HotelReview> listWithUserName(Long hotelId, Integer limit) {
		return this.baseMapper.selectReviewsWithUserName(hotelId, limit);
	}
}
