package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.constant.Constants;
import com.phms.common.result.Result;
import com.phms.dto.ProductSaveDTO;
import com.phms.dto.ProductStatusDTO;
import com.phms.dto.ProductStockAdjustDTO;
import com.phms.entity.Product;
import com.phms.service.ProductService;
import com.phms.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 */
@Tag(name = "商品管理", description = "商品的增删改查及上架管理接口")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品列表")
    @GetMapping("/page")
    @SaCheckPermission("product:list")
    public Result<Page<ProductVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "商品名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId) {
        boolean isAdmin = isAdminRole();
        Long operatorHotelId = getHotelId();
        if (!isAdmin) {
            hotelId = operatorHotelId;
        }
        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.pageList(page, name, status, hotelId));
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    @SaCheckPermission("product:list")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    @SaCheckPermission("product:add")
    @OperateLog(module = "product", type = "add", description = "新增商品")
    public Result<Void> add(@Valid @RequestBody ProductSaveDTO dto) {
        productService.saveProduct(dto, getHotelId(), isAdminRole());
        return Result.success();
    }

    @Operation(summary = "编辑商品")
    @PutMapping
    @SaCheckPermission("product:edit")
    @OperateLog(module = "product", type = "update", description = "编辑商品")
    public Result<Void> update(@Valid @RequestBody ProductSaveDTO dto) {
        productService.updateProduct(dto, getHotelId(), isAdminRole());
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @SaCheckPermission("product:delete")
    @OperateLog(module = "product", type = "delete", description = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeProduct(id, getHotelId(), isAdminRole());
        return Result.success();
    }

    @Operation(summary = "商品上/下架")
    @PutMapping("/{id}/status")
    @SaCheckPermission("product:status")
    @OperateLog(module = "product", type = "update", description = "商品上/下架")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusDTO dto) {
        productService.updateStatus(id, dto.getStatus(), getHotelId(), isAdminRole());
        return Result.success();
    }

    @Operation(summary = "商品库存调整")
    @PutMapping("/{id}/stock")
    @SaCheckPermission("product:stock")
    @OperateLog(module = "product", type = "update", description = "商品库存调整")
    public Result<Void> adjustStock(@PathVariable Long id, @Valid @RequestBody ProductStockAdjustDTO dto) {
        productService.adjustStock(id, dto.getDelta(), getHotelId(), isAdminRole());
        return Result.success();
    }

    @Operation(summary = "用户端商品列表")
    @GetMapping("/user/list")
    public Result<List<ProductVO>> userList(@RequestParam Long hotelId) {
        return Result.success(productService.listUserProducts(hotelId));
    }

    private boolean isAdminRole() {
        Object roleTypeObj = StpUtil.getSession().get("roleType");
        Integer roleType = roleTypeObj == null ? null : Integer.valueOf(String.valueOf(roleTypeObj));
        return roleType != null && roleType.equals(Constants.ROLE_ADMIN);
    }

    private Long getHotelId() {
        Object hotelIdObj = StpUtil.getSession().get("hotelId");
        if (hotelIdObj == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(hotelIdObj));
    }
}
