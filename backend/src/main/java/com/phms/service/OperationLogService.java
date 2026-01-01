package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.OperationLog;

/**
 * 操作日志 Service
 *
 * @author PHMS
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param page         分页参数
     * @param operatorType 操作人类型
     * @param module       操作模块
     * @param type         操作类型
     * @return 分页结果
     */
    Page<OperationLog> pageList(Page<OperationLog> page, Integer operatorType, String module, String type);
}
