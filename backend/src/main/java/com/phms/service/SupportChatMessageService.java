package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.SupportChatMessage;

import java.util.List;

public interface SupportChatMessageService extends IService<SupportChatMessage> {
    void saveMessage(Long userId, Long staffId, String sender, String content, Long roomId, Long hotelId);

    List<SupportChatMessage> listHistory(Long userId, Long staffId, int limit);
}
