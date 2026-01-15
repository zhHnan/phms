package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.CareLog;
import com.phms.vo.CareLogVO;

import java.util.List;

/**
 * 护理日志 Service
 *
 * @author PHMS
 */
public interface CareLogService extends IService<CareLog> {

    /**
     * 分页查询护理日志
     *
     * @param page     分页参数
     * @param orderId  订单ID
     * @param careType 护理类型
     * @return 分页结果
     */
    Page<CareLog> pageList(Page<CareLog> page, Long orderId, Integer careType);

    /**
     * 后台分页查询护理日志（带订单/宠物/员工信息）
     */
    Page<CareLogVO> pageListVO(Page<CareLogVO> page, Long hotelId, String orderNo, String petName, Integer careType);

    /**
     * 根据订单ID查询护理日志
     *
     * @param orderId 订单ID
     * @return 护理日志列表
     */
    List<CareLogVO> listByOrderId(Long orderId);
}
