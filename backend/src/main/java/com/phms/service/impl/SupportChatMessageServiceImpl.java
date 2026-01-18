package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.SupportChatMessage;
import com.phms.mapper.SupportChatMessageMapper;
import com.phms.service.SupportChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SupportChatMessageServiceImpl extends ServiceImpl<SupportChatMessageMapper, SupportChatMessage>
        implements SupportChatMessageService {

    @Override
    public void saveMessage(Long userId, Long staffId, String sender, String content, Long roomId, Long hotelId) {
        SupportChatMessage msg = new SupportChatMessage();
        msg.setUserId(userId);
        msg.setStaffId(staffId);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setRoomId(roomId);
        msg.setHotelId(hotelId);
        msg.setCreatedAt(LocalDateTime.now());
        this.save(msg);
    }

    @Override
    public List<SupportChatMessage> listHistory(Long userId, Long staffId, int limit) {
        if (userId == null || staffId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupportChatMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(SupportChatMessage::getUserId, userId)
                .eq(SupportChatMessage::getStaffId, staffId)
                .orderByDesc(SupportChatMessage::getCreatedAt)
                .last("limit " + limit);
        List<SupportChatMessage> list = this.list(qw);
        // reverse to ascending for display
        list.sort(Comparator.comparing(SupportChatMessage::getCreatedAt));
        return list;
    }
}
