package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物档案 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
