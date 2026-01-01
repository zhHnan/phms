package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码记录 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {
}
