package com.phms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("support_chat_message")
public class SupportChatMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long staffId;

    /** sender: user/staff */
    private String sender;

    private String content;

    private Long roomId;

    private Long hotelId;

    private LocalDateTime createdAt;
}
