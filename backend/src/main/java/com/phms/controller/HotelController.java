package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.entity.Hotel;
import com.phms.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店管理控制器
 *
 * @author PHMS
 */
@Tag(name = "门店管理", description = "门店的增删改查接口")
@RestController
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "分页查询门店列表")
    @GetMapping("/page")
    @SaCheckPermission("hotel:list")
    public Result<Page<Hotel>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "门店名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<Hotel> page = new Page<>(pageNum, pageSize);
        return Result.success(hotelService.pageList(page, name, status));
    }

    @Operation(summary = "查询所有门店列表")
    @GetMapping("/list")
    public Result<List<Hotel>> list() {
        return Result.success(hotelService.list());
    }

    @Operation(summary = "根据ID查询门店详情")
    @GetMapping("/{id}")
    public Result<Hotel> getById(@PathVariable Long id) {
        return Result.success(hotelService.getById(id));
    }

    @Operation(summary = "新增门店")
    @PostMapping
    @SaCheckPermission("hotel:add")
    @OperateLog(module = "hotel", type = "add", description = "新增门店")
    public Result<Void> add(@Valid @RequestBody Hotel hotel) {
        hotelService.save(hotel);
        return Result.success();
    }

    @Operation(summary = "修改门店")
    @PutMapping
    @SaCheckPermission("hotel:edit")
    @OperateLog(module = "hotel", type = "update", description = "修改门店")
    public Result<Void> update(@Valid @RequestBody Hotel hotel) {
        hotelService.updateById(hotel);
        return Result.success();
    }

    @Operation(summary = "删除门店")
    @DeleteMapping("/{id}")
    @SaCheckPermission("hotel:delete")
    @OperateLog(module = "hotel", type = "delete", description = "删除门店")
    public Result<Void> delete(@PathVariable Long id) {
        hotelService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "修改门店状态")
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("hotel:edit")
    @OperateLog(module = "hotel", type = "update", description = "修改门店状态")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setStatus(status);
        hotelService.updateById(hotel);
        return Result.success();
    }
}
