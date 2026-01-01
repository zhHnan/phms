package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
