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
    public Page<Room> pageAvailableRooms(Page<Room> page, Long hotelId, String roomType, LocalDate checkInDate,
            LocalDate checkOutDate, String orderBy, String orderDirection) {
        // 查询所有符合条件的房间（包含评分数据）
        List<Room> allRooms = baseMapper.selectAvailableRoomsWithHotel(hotelId, roomType);

        // 根据orderBy参数排序
        if (orderBy != null && !orderBy.isEmpty()) {
            boolean desc = "desc".equalsIgnoreCase(orderDirection);
            allRooms.sort((r1, r2) -> {
                int result = 0;
                switch (orderBy) {
                    case "roomScore":
                        result = compareDouble(r1.getRoomAvgScore(), r2.getRoomAvgScore());
                        break;
                    case "roomCount":
                        result = compareInteger(r1.getRoomReviewCount(), r2.getRoomReviewCount());
                        break;
                    case "hotelScore":
                        result = compareDouble(r1.getHotelAvgScore(), r2.getHotelAvgScore());
                        break;
                    case "hotelCount":
                        result = compareInteger(r1.getHotelReviewCount(), r2.getHotelReviewCount());
                        break;
                    default:
                        result = r1.getPricePerNight().compareTo(r2.getPricePerNight());
                }
                return desc ? -result : result;
            });
        } else {
            // 默认按价格升序
            allRooms.sort((r1, r2) -> r1.getPricePerNight().compareTo(r2.getPricePerNight()));
        }

        // 手动分页
        int total = allRooms.size();
        int start = (int) ((page.getCurrent() - 1) * page.getSize());
        int end = Math.min(start + (int) page.getSize(), total);

        List<Room> pageRecords = start < total ? allRooms.subList(start, end) : List.of();

        Page<Room> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(pageRecords);
        return result;
    }

    private int compareDouble(Double d1, Double d2) {
        double v1 = d1 != null ? d1 : 0.0;
        double v2 = d2 != null ? d2 : 0.0;
        return Double.compare(v1, v2);
    }

    private int compareInteger(Integer i1, Integer i2) {
        int v1 = i1 != null ? i1 : 0;
        int v2 = i2 != null ? i2 : 0;
        return Integer.compare(v1, v2);
    }

    @Override
    public List<Room> countByStatus(Long hotelId) {
        return lambdaQuery()
                .eq(hotelId != null, Room::getHotelId, hotelId)
                .list();
    }

    @Override
    public boolean checkAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        // 检查房间是否存在且状态为空闲
        // Room room = getById(roomId);
        // if (room == null || room.getStatus() != Constants.ROOM_STATUS_FREE) {
        // return false;
        // }

        // 检查指定时间段是否有订单冲突
        // 返回冲突订单数量，0表示无冲突（可预订）
        int conflictCount = baseMapper.countConflictOrders(roomId, checkInDate, checkOutDate);
        return conflictCount == 0;
    }

    @Override
    public Room getDetailWithHotel(Long id) {
        return baseMapper.selectByIdWithHotel(id);
    }
}
