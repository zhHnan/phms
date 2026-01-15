package com.phms.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.entity.Pet;
import com.phms.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物档案控制器
 *
 * @author PHMS
 */
@Tag(name = "宠物档案", description = "宠物档案的增删改查接口")
@RestController
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(summary = "分页查询宠物列表")
    @GetMapping("/page")
    public Result<Page<Pet>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "宠物名称") @RequestParam(required = false) String name,
            @Parameter(description = "宠物类型") @RequestParam(required = false) Integer type) {
        // 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Pet> page = new Page<>(pageNum, pageSize);
        return Result.success(petService.pageList(page, userId, name, type));
    }

    @Operation(summary = "查询当前用户的宠物列表")
    @GetMapping("/my-pets")
    public Result<List<Pet>> myPets() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(petService.listByUserId(userId));
    }

    @Operation(summary = "根据ID查询宠物详情")
    @GetMapping("/{id}")
    public Result<Pet> getById(@PathVariable Long id) {
        Pet pet = petService.getById(id);
        assertPetOwner(pet);
        return Result.success(pet);
    }

    @Operation(summary = "新增宠物")
    @PostMapping
    @OperateLog(module = "pet", type = "add", description = "新增宠物")
    public Result<Void> add(@Valid @RequestBody Pet pet) {
        // 设置当前用户ID
        pet.setUserId(StpUtil.getLoginIdAsLong());
        petService.save(pet);
        return Result.success();
    }

    @Operation(summary = "修改宠物")
    @PutMapping
    @OperateLog(module = "pet", type = "update", description = "修改宠物")
    public Result<Void> update(@Valid @RequestBody Pet pet) {
        if (pet != null && pet.getId() != null) {
            Pet dbPet = petService.getById(pet.getId());
            assertPetOwner(dbPet);
            // 防止篡改 owner
            if (dbPet != null) {
                pet.setUserId(dbPet.getUserId());
            }
        }
        petService.updateById(pet);
        return Result.success();
    }

    @Operation(summary = "删除宠物")
    @DeleteMapping("/{id}")
    @OperateLog(module = "pet", type = "delete", description = "删除宠物")
    public Result<Void> delete(@PathVariable Long id) {
        Pet pet = petService.getById(id);
        assertPetOwner(pet);
        petService.removeById(id);
        return Result.success();
    }

    private void assertPetOwner(Pet pet) {
        if (pet == null) {
            return;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        if (pet.getUserId() == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
