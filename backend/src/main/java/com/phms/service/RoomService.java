package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Room;

import java.time.LocalDate;
import java.util.List;

/**
 * 房间 Service
 *
 * @author PHMS
 */
public interface RoomService extends IService<Room> {

    /**
     * 分页查询房间列表
     *
     * @param page     分页参数
     * @param hotelId  门店ID
     * @param typeName 房型名称
     * @param status   状态
     * @return 分页结果
     */
    Page<Room> pageList(Page<Room> page, Long hotelId, String typeName, Integer status);
    
    /**
     * 分页查询房间列表（带酒店名称）
     *
     * @param page     分页参数
     * @param hotelId  门店ID
     * @param typeName 房型名称
     * @param status   状态
     * @param roomNo   房间号
     * @return 分页结果
     */
    Page<Room> pageListWithHotel(Page<Room> page, Long hotelId, String typeName, Integer status, String roomNo);

    /**
     * 查询可用房间
     *
     * @param hotelId      门店ID
     * @param checkInDate  入住日期
     * @param checkOutDate 离店日期
     * @return 可用房间列表
     */
    List<Room> listAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 更新房间状态
     *
     * @param roomId 房间ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long roomId, Integer status);

    /**
     * 分页查询可用房间（用户端）
     *
     * @param page         分页参数
     * @param hotelId      门店ID
     * @param roomType     房型
     * @param checkInDate  入住日期
     * @param checkOutDate 离店日期
     * @return 分页结果
     */
    Page<Room> pageAvailableRooms(Page<Room> page, Long hotelId, String roomType, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 根据门店ID统计各状态房间数量
     *
     * @param hotelId 门店ID
     * @return 统计结果
     */
    List<Room> countByStatus(Long hotelId);

    /**
     * 检查房间在指定日期是否可预订
     *
     * @param roomId       房间ID
     * @param checkInDate  入住日期
     * @param checkOutDate 离店日期
     * @return 是否可预订
     */
    boolean checkAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 根据ID查询房间详情（包含所属酒店名称与地址）
     */
    Room getDetailWithHotel(Long id);
}
