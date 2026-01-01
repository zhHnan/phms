package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
