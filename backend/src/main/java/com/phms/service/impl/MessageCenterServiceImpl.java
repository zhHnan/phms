package com.phms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.entity.MessageCenter;
import com.phms.mapper.MessageCenterMapper;
import com.phms.service.MessageCenterService;
import com.phms.vo.MessageReadVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息中心 Service 实现类
 *
 * @author PHMS
 */
@Service
public class MessageCenterServiceImpl extends ServiceImpl<MessageCenterMapper, MessageCenter> implements MessageCenterService {

    @Override
    public Page<MessageCenter> pageMyMessages(Page<MessageCenter> page, Integer isRead) {
        Integer receiverType = currentReceiverType();
        boolean isAdmin = isAdmin();
        Long hotelId = currentHotelIdRequiredIfNotAdmin(isAdmin);

        LambdaQueryWrapper<MessageCenter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageCenter::getReceiverType, receiverType)
            .eq(isRead != null, MessageCenter::getIsRead, isRead)
            .orderByDesc(MessageCenter::getCreatedAt);

        // 超管：查看全平台
        if (isAdmin) {
            return page(page, wrapper);
        }

        // 店长/员工：只看本门店共享消息
        wrapper.eq(MessageCenter::getHotelId, hotelId);
        return page(page, wrapper);
    }

    @Override
    public MessageReadVO markRead(Long id) {
        Integer receiverType = currentReceiverType();
        boolean isAdmin = isAdmin();
        Long hotelId = currentHotelIdRequiredIfNotAdmin(isAdmin);

        // 先查状态，避免重复已读触发 UPDATE（幂等）
        LambdaQueryWrapper<MessageCenter> query = new LambdaQueryWrapper<>();
        query.eq(MessageCenter::getId, id)
            .eq(MessageCenter::getReceiverType, receiverType);
        if (!isAdmin) {
            query.eq(MessageCenter::getHotelId, hotelId);
        }
        MessageCenter current = getOne(query);
        if (current == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        MessageReadVO vo = new MessageReadVO();
        vo.setId(current.getId());

        if (current.getIsRead() != null && current.getIsRead() == 1) {
            vo.setIsRead(1);
            vo.setReadTime(current.getReadTime());
            vo.setAlreadyRead(true);
            return vo;
        }

        LambdaUpdateWrapper<MessageCenter> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MessageCenter::getId, id)
                .eq(MessageCenter::getReceiverType, receiverType)
                .eq(MessageCenter::getIsRead, 0)
                .set(MessageCenter::getIsRead, 1)
                .set(MessageCenter::getReadTime, LocalDateTime.now());

        if (!isAdmin) {
            wrapper.eq(MessageCenter::getHotelId, hotelId);
        }
        boolean updated = update(wrapper);
        if (!updated) {
            // 并发情况下可能被他人先点了已读：重新查一次返回已读状态
            MessageCenter latest = getOne(query);
            if (latest == null) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            vo.setIsRead(latest.getIsRead());
            vo.setReadTime(latest.getReadTime());
            vo.setAlreadyRead(true);
            return vo;
        }

        vo.setIsRead(1);
        vo.setReadTime(LocalDateTime.now());
        vo.setAlreadyRead(false);
        return vo;
    }

    @Override
    public int markAllRead() {
        Integer receiverType = currentReceiverType();
        boolean isAdmin = isAdmin();
        Long hotelId = currentHotelIdRequiredIfNotAdmin(isAdmin);

        LambdaUpdateWrapper<MessageCenter> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MessageCenter::getReceiverType, receiverType)
                .eq(MessageCenter::getIsRead, 0)
                .set(MessageCenter::getIsRead, 1)
                .set(MessageCenter::getReadTime, LocalDateTime.now());

        if (!isAdmin) {
            wrapper.eq(MessageCenter::getHotelId, hotelId);
        }
        return getBaseMapper().update(null, wrapper);
    }

    @Override
    public void createMessage(Long hotelId, Integer receiverType, Long receiverId, String title, String content) {
        if (receiverType == null) {
            return;
        }
        MessageCenter msg = new MessageCenter();
        msg.setHotelId(hotelId);
        msg.setReceiverType(receiverType);
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        save(msg);
    }

    private Integer currentReceiverType() {
        Object userType = StpUtil.getSession().get("userType");
        if (ObjectUtil.isNull(userType)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        int type = (Integer) userType;
        if (type != Constants.USER_TYPE_CLIENT && type != Constants.USER_TYPE_STAFF) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return type;
    }

    private boolean isAdmin() {
        Object roleType = StpUtil.getSession().get("roleType");
        return roleType != null && (Integer) roleType == Constants.ROLE_ADMIN;
    }

    private Long currentHotelIdRequiredIfNotAdmin(boolean isAdmin) {
        if (isAdmin) {
            return null;
        }
        Object hotelId = StpUtil.getSession().get("hotelId");
        if (hotelId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return Long.valueOf(String.valueOf(hotelId));
    }

}
