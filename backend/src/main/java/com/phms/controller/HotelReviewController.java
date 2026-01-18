package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.dto.HotelReviewCreateDTO;
import com.phms.entity.HotelReview;
import com.phms.entity.Order;
import com.phms.service.HotelReviewService;
import com.phms.service.OrderService;
import com.phms.vo.HotelReviewVO;
import com.phms.vo.HotelReviewSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 酒店评价控制器（C端）
 */
@Tag(name = "酒店评价", description = "订单完成后对酒店进行评分与评价")
@RestController
@RequestMapping("/hotel-review")
@RequiredArgsConstructor
public class HotelReviewController {

    private final HotelReviewService hotelReviewService;
    private final OrderService orderService;

    @Operation(summary = "查询订单评价（C端）")
    @GetMapping("/order/{orderId}")
    @SaCheckLogin
    public Result<HotelReviewVO> getByOrderId(@PathVariable Long orderId) {
        assertClientUser();
        Long userId = StpUtil.getLoginIdAsLong();

        HotelReview review = hotelReviewService.lambdaQuery()
                .eq(HotelReview::getOrderId, orderId)
                .eq(HotelReview::getUserId, userId)
                .one();
        if (review == null) {
            return Result.success(null);
        }
        HotelReviewVO vo = toVO(review);
        return Result.success(vo);
    }

    @Operation(summary = "查询酒店评分汇总（包含房间均分）")
    @GetMapping("/summary/{hotelId}")
    public Result<HotelReviewSummaryVO> summary(@PathVariable Long hotelId) {
        HotelReviewSummaryVO vo = hotelReviewService.getHotelSummary(hotelId);
        return Result.success(vo);
    }

    @Operation(summary = "查询酒店评论列表")
    @GetMapping("/list/{hotelId}")
    public Result<java.util.List<HotelReviewVO>> list(
            @PathVariable Long hotelId,
            @Parameter(description = "限制返回数量，默认10条") @RequestParam(defaultValue = "10") Integer limit) {
        Integer limitValue = Math.min(limit, 100);
        java.util.List<HotelReview> reviews = hotelReviewService.listWithUserName(hotelId, limitValue);

        java.util.List<HotelReviewVO> vos = reviews.stream().map(this::toVO).toList();
        return Result.success(vos);
    }

    @Operation(summary = "提交酒店评价（C端）", description = "仅允许已完成订单且一单一评")
    @PostMapping
    @SaCheckLogin
    public Result<Void> create(@Valid @RequestBody HotelReviewCreateDTO dto) {
        assertClientUser();

        Long userId = StpUtil.getLoginIdAsLong();
        Order order = orderService.getById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() == null || !order.getStatus().equals(Constants.ORDER_STATUS_COMPLETED)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅已完成订单可评价");
        }

        boolean exists = hotelReviewService.lambdaQuery()
                .eq(HotelReview::getOrderId, dto.getOrderId())
                .eq(HotelReview::getUserId, userId)
                .exists();
        if (exists) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该订单已评价");
        }

        HotelReview review = new HotelReview();
        review.setOrderId(dto.getOrderId());
        review.setHotelId(order.getHotelId());
        review.setUserId(userId);
        review.setScore(dto.getScore());
        review.setContent(StrUtil.isBlank(dto.getContent()) ? null : dto.getContent().trim());

        hotelReviewService.save(review);
        return Result.success();
    }

    private void assertClientUser() {
        Object userTypeObj = StpUtil.getSession().get("userType");
        Integer userType = userTypeObj == null ? null : Integer.valueOf(String.valueOf(userTypeObj));
        if (userType == null || !userType.equals(Constants.USER_TYPE_CLIENT)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private HotelReviewVO toVO(HotelReview review) {
        HotelReviewVO vo = new HotelReviewVO();
        vo.setOrderId(review.getOrderId());
        vo.setHotelId(review.getHotelId());
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getCreatedAt());
        vo.setUserName(review.getUserName());
        return vo;
    }
}
