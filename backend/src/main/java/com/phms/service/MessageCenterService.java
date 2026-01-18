package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.MessageCenter;
import com.phms.vo.MessageReadVO;

/**
 * 消息中心 Service
 *
 * @author PHMS
 */
public interface MessageCenterService extends IService<MessageCenter> {

    /**
     * 分页查询当前登录人的消息
     */
    Page<MessageCenter> pageMyMessages(Page<MessageCenter> page, Integer isRead);

    /**
     * 标记单条消息已读（仅限本人）
     */
    MessageReadVO markRead(Long id);

    /**
     * 一键已读（仅限本人）
     */
    int markAllRead();

    /**
     * 写入一条消息
     */
    void createMessage(Long hotelId, Integer receiverType, Long receiverId, String title, String content);
}
