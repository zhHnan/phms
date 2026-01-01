package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Hotel;

/**
 * 门店信息 Service
 *
 * @author PHMS
 */
public interface HotelService extends IService<Hotel> {

    /**
     * 分页查询门店列表
     *
     * @param page   分页参数
     * @param name   门店名称（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    Page<Hotel> pageList(Page<Hotel> page, String name, Integer status);

    /**
     * 根据门店编码查询
     *
     * @param code 门店编码
     * @return 门店信息
     */
    Hotel getByCode(String code);
}
