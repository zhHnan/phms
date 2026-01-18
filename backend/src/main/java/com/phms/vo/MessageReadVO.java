package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息已读结果
 *
 * 任一同门店员工已读即代表门店完成（共享已读）。
 */
@Data
@Schema(description = "消息已读结果")
public class MessageReadVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "是否已读：0=未读 1=已读")
    private Integer isRead;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;

    @Schema(description = "是否重复已读：true=此前已读，本次不触发更新")
    private Boolean alreadyRead;
}
