package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Staff;
import com.phms.vo.StaffInfoVO;
import java.util.List;

/**
 * 员工 Service
 *
 * @author PHMS
 */
public interface StaffService extends IService<Staff> {

    /**
     * 分页查询员工列表
     *
     * @param page     分页参数
     * @param hotelId  门店ID
     * @param realName 真实姓名（模糊查询）
     * @param roleType 角色类型
     * @param status   状态
     * @return 分页结果
     */
    Page<StaffInfoVO> pageList(Page<Staff> page, Long hotelId, String realName, Integer roleType, Integer status);

    /**
     * 根据邮箱查询员工
     *
     * @param email 邮箱
     * @return 员工信息
     */
    Staff getByEmail(String email);

    /**
     * 校验邮箱是否唯一
     *
     * @param email 邮箱
     * @param id    排除的员工ID
     * @return true=唯一 false=已存在
     */
    boolean checkEmailUnique(String email, Long id);

    /**
     * 检查该酒店是否已存在店长
     *
     * @param hotelId    酒店ID
     * @param excludeId  排除的员工ID（用于编辑时排除自己）
     * @return true=已存在店长 false=不存在
     */
    boolean hasManager(Long hotelId, Long excludeId);

    /**
     * 查询指定门店下的有效员工（排除平台超管）
     *
     * @param hotelId 门店ID
     * @return 员工列表
     */
    List<Staff> listEnabledByHotel(Long hotelId);
}
