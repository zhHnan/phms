package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.CareLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物护理日志 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface CareLogMapper extends BaseMapper<CareLog> {
}
