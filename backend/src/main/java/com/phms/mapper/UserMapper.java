package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * C端用户 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
