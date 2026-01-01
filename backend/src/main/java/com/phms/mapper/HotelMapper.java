package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店信息 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
}
