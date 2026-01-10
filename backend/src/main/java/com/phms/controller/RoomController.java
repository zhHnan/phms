package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.entity.Room;
import com.phms.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 房间管理控制器
 *
 * @author PHMS
 */
@Tag(name = "房间管理", description = "房间的增删改查及状态管理接口")
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "分页查询房间列表")
    @GetMapping("/page")
    @SaCheckPermission("room:list")
    public Result<Page<Room>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "房间号") @RequestParam(required = false) String roomNo,
            @Parameter(description = "房型名称") @RequestParam(required = false) String typeName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        return Result.success(roomService.pageListWithHotel(page, hotelId, typeName, status, roomNo));
    }

    @Operation(summary = "查询门店所有房间")
    @GetMapping("/list")
    public Result<List<Room>> list(
            @Parameter(description = "门店ID") @RequestParam Long hotelId) {
        return Result.success(roomService.lambdaQuery().eq(Room::getHotelId, hotelId).list());
    }

    @Operation(summary = "查询可用房间")
    @GetMapping("/available")
    public Result<Page<Room>> listAvailable(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "房型") @RequestParam(required = false) String roomType,
            @Parameter(description = "入住日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @Parameter(description = "离店日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        // 查询空闲状态的房间
        return Result.success(roomService.pageAvailableRooms(page, hotelId, roomType, checkInDate, checkOutDate));
    }

    @Operation(summary = "根据ID查询房间详情")
    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        return Result.success(roomService.getDetailWithHotel(id));
    }

    @Operation(summary = "新增房间")
    @PostMapping
    @SaCheckPermission("room:add")
    @OperateLog(module = "room", type = "add", description = "新增房间")
    public Result<Void> add(@Valid @RequestBody Room room) {
        roomService.save(room);
        return Result.success();
    }

    @Operation(summary = "修改房间")
    @PutMapping
    @SaCheckPermission("room:edit")
    @OperateLog(module = "room", type = "update", description = "修改房间")
    public Result<Void> update(@Valid @RequestBody Room room) {
        roomService.updateById(room);
        return Result.success();
    }

    @Operation(summary = "删除房间")
    @DeleteMapping("/{id}")
    @SaCheckPermission("room:delete")
    @OperateLog(module = "room", type = "delete", description = "删除房间")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "检查房间在指定日期是否可预订")
    @GetMapping("/check-availability")
    public Result<Boolean> checkAvailability(
            @Parameter(description = "房间ID") @RequestParam Long roomId,
            @Parameter(description = "入住日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @Parameter(description = "离店日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        boolean available = roomService.checkAvailability(roomId, checkInDate, checkOutDate);
        return Result.success(available);
    }

    @Operation(summary = "修改房间状态")
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("room:status")
    @OperateLog(module = "room", type = "update", description = "修改房间状态")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        roomService.updateStatus(id, status);
        return Result.success();
    }
}
