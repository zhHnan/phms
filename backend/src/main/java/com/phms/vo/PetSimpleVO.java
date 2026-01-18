package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "宠物简要信息")
public class PetSimpleVO {

    @Schema(description = "宠物ID")
    private Long id;

    @Schema(description = "宠物名称")
    private String name;

    @Schema(description = "宠物类型：1=猫 2=狗 3=其他")
    private Integer type;
}
