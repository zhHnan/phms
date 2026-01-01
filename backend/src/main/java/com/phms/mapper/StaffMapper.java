package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内部员工 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
