package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.HotelReview;
import com.phms.vo.HotelReviewSummaryVO;
import java.util.List;

public interface HotelReviewService extends IService<HotelReview> {

	/**
	 * 酒店评分汇总：整体均分/数量及各房间均分
	 */
	HotelReviewSummaryVO getHotelSummary(Long hotelId);

	/**
	 * 获取酒店评论列表，包含用户昵称
	 */
	List<HotelReview> listWithUserName(Long hotelId, Integer limit);
}
