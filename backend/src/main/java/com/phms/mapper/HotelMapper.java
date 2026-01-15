package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门店信息 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {

    /**
     * 查询所有营业中的酒店及其评分数据
     * 返回所有营业酒店，Java层负责排序
     */
    @Select("SELECT h.*, " +
            "COALESCE(hotel_stats.avg_score, 0) as avg_score, " +
            "COALESCE(hotel_stats.review_count, 0) as review_count " +
            "FROM sys_hotels h " +
            "LEFT JOIN (" +
            "  SELECT hotel_id, AVG(score) as avg_score, COUNT(*) as review_count " +
            "  FROM biz_hotel_reviews " +
            "  WHERE is_deleted = 0 " +
            "  GROUP BY hotel_id" +
            ") hotel_stats ON h.id = hotel_stats.hotel_id " +
            "WHERE h.is_deleted = 0 AND h.status = 1")
    List<Hotel> selectAllHotelsWithScore();
}
