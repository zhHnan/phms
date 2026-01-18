package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息中心表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_center")
@Schema(description = "消息中心")
public class MessageCenter extends BaseEntity {

    @Schema(description = "所属门店ID（平台消息为NULL）")
    private Long hotelId;

    @Schema(description = "接收方类型：1=C端用户 2=B端员工")
    private Integer receiverType;

    @Schema(description = "接收方ID（sys_users.id 或 sys_staff.id）")
    private Long receiverId;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "是否已读：0=未读 1=已读")
    private Integer isRead;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;
}
