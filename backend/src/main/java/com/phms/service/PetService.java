package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Pet;

import java.util.List;

/**
 * 宠物档案 Service
 *
 * @author PHMS
 */
public interface PetService extends IService<Pet> {

    /**
     * 分页查询宠物列表
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @param name   宠物名称
     * @param type   宠物类型
     * @return 分页结果
     */
    Page<Pet> pageList(Page<Pet> page, Long userId, String name, Integer type);

    /**
     * 根据用户ID查询宠物列表
     *
     * @param userId 用户ID
     * @return 宠物列表
     */
    List<Pet> listByUserId(Long userId);

    /**
     * 根据订单ID查询关联的宠物列表
     *
     * @param orderId 订单ID
     * @return 宠物列表
     */
    List<Pet> getPetsByOrderId(Long orderId);
}
