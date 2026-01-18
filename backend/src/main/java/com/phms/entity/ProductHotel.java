package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品-门店关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product_hotels")
@Schema(description = "商品-门店关联")
public class ProductHotel extends BaseEntity {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "门店ID")
    private Long hotelId;
}
