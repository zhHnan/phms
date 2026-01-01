package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.phms.common.constant.Constants;
import com.phms.common.result.Result;
import com.phms.entity.Room;
import com.phms.service.OrderService;
import com.phms.service.RoomService;
import com.phms.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 *
 * @author PHMS
 */
@Tag(name = "仪表盘", description = "门店数据统计接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final OrderService orderService;
    private final RoomService roomService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping
    @SaCheckPermission("dashboard:view")
    public Result<DashboardVO> getDashboardData() {
        // 获取当前门店ID
        Long hotelId = null;
        Object roleType = StpUtil.getSession().get("roleType");
        if (roleType != null && (Integer) roleType != Constants.ROLE_ADMIN) {
            hotelId = (Long) StpUtil.getSession().get("hotelId");
        }

        DashboardVO vo = new DashboardVO();

        // 今日预订数
        vo.setTodayOrders(orderService.countTodayOrders(hotelId));

        // 当前在住宠物数
        vo.setCheckedInPets(orderService.countCheckedInPets(hotelId));

        // 房间状态统计
        List<Room> rooms = roomService.countByStatus(hotelId);
        Map<Integer, Long> roomStatusCount = new HashMap<>();
        roomStatusCount.put(Constants.ROOM_STATUS_FREE, 0L);
        roomStatusCount.put(Constants.ROOM_STATUS_RESERVED, 0L);
        roomStatusCount.put(Constants.ROOM_STATUS_OCCUPIED, 0L);
        roomStatusCount.put(Constants.ROOM_STATUS_CLEANING, 0L);
        roomStatusCount.put(Constants.ROOM_STATUS_MAINTENANCE, 0L);

        for (Room room : rooms) {
            Integer status = room.getStatus();
            roomStatusCount.put(status, roomStatusCount.getOrDefault(status, 0L) + 1);
        }

        vo.setFreeRooms(roomStatusCount.get(Constants.ROOM_STATUS_FREE));
        vo.setReservedRooms(roomStatusCount.get(Constants.ROOM_STATUS_RESERVED));
        vo.setOccupiedRooms(roomStatusCount.get(Constants.ROOM_STATUS_OCCUPIED));
        vo.setCleaningRooms(roomStatusCount.get(Constants.ROOM_STATUS_CLEANING));
        vo.setMaintenanceRooms(roomStatusCount.get(Constants.ROOM_STATUS_MAINTENANCE));
        vo.setTotalRooms((long) rooms.size());

        return Result.success(vo);
    }
}
