package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.OperationLog;
import com.phms.mapper.OperationLogMapper;
import com.phms.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志 Service 实现类
 *
 * @author PHMS
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public Page<OperationLog> pageList(Page<OperationLog> page, Integer operatorType, String module, String type) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(operatorType != null, OperationLog::getOperatorType, operatorType)
                .eq(StrUtil.isNotBlank(module), OperationLog::getOperationModule, module)
                .eq(StrUtil.isNotBlank(type), OperationLog::getOperationType, type)
                .orderByDesc(OperationLog::getCreatedAt);
        return page(page, wrapper);
    }
}
