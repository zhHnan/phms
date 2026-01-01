package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.entity.Room;
import com.phms.mapper.RoomMapper;
import com.phms.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 房间 Service 实现类
 *
 * @author PHMS
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Override
    public Page<Room> pageList(Page<Room> page, Long hotelId, String typeName, Integer status) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, Room::getHotelId, hotelId)
                .like(StrUtil.isNotBlank(typeName), Room::getTypeName, typeName)
                .eq(status != null, Room::getStatus, status)
                .orderByAsc(Room::getRoomNo);
        return page(page, wrapper);
    }

    @Override
    public Page<Room> pageListWithHotel(Page<Room> page, Long hotelId, String typeName, Integer status, String roomNo) {
        return baseMapper.selectPageWithHotel(page, hotelId, typeName, status, roomNo);
    }

    @Override
    public List<Room> listAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        // 查询空闲状态的房间
        return lambdaQuery()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getStatus, Constants.ROOM_STATUS_FREE)
                .list();
    }

    @Override
    public boolean updateStatus(Long roomId, Integer status) {
        return lambdaUpdate()
                .eq(Room::getId, roomId)
                .set(Room::getStatus, status)
                .update();
    }

    @Override
    public Page<Room> pageAvailableRooms(Page<Room> page, Long hotelId, String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, Room::getHotelId, hotelId)
                .like(StrUtil.isNotBlank(roomType), Room::getTypeName, roomType)
                .eq(Room::getStatus, Constants.ROOM_STATUS_FREE)
                .orderByAsc(Room::getPricePerNight);
        return page(page, wrapper);
    }

    @Override
    public List<Room> countByStatus(Long hotelId) {
        return lambdaQuery()
                .eq(hotelId != null, Room::getHotelId, hotelId)
                .list();
    }
}
