package com.phms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.dto.OrderCreateDTO;
import com.phms.entity.Order;
import com.phms.entity.Hotel;
import com.phms.entity.Pet;
import com.phms.entity.Room;
import com.phms.mapper.OrderMapper;
import com.phms.entity.Staff;
import com.phms.entity.User;
import com.phms.service.OrderService;
import com.phms.service.HotelService;
import com.phms.service.MessageCenterService;
import com.phms.service.PetService;
import com.phms.service.StaffService;
import com.phms.service.RoomService;
import com.phms.service.UserService;
import com.phms.util.RabbitMQUtil;
import com.phms.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 订单 Service 实现类
 *
 * @author PHMS
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final RoomService roomService;
    private final PetService petService;
    private final HotelService hotelService;
    private final StaffService staffService;
    private final UserService userService;
    private final MessageCenterService messageCenterService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitMQUtil rabbitMQUtil;
    
    private static final String LOCK_PREFIX = "order:lock:room:";
    private static final long LOCK_TIMEOUT = 10; // 锁超时时间（秒）

    @Override
    public Page<Order> pageList(Page<Order> page, Long hotelId, Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, Order::getHotelId, hotelId)
                .eq(userId != null, Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public Page<OrderVO> pageListVO(Page<OrderVO> page, Long hotelId, Long userId, Integer status) {
        return orderMapper.selectOrderVOPage(page, hotelId, userId, status);
    }

    @Override
    public OrderVO getOrderVOById(Long id) {
        return orderMapper.selectOrderVOById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateDTO dto) {
        // 构建分布式锁的key：room_id + check_in + check_out
        String lockKey = LOCK_PREFIX + dto.getRoomId() + ":" + dto.getCheckInDate() + ":" + dto.getCheckOutDate();
        String lockValue = UUID.randomUUID().toString();
        
        // 尝试获取锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单处理中，请稍后重试");
        }
        
        try {
            // 获取当前用户ID（lambda中使用，需final/有效final）
            final Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查房间是否存在
            Room room = roomService.getById(dto.getRoomId());
            if (room == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "房间不存在");
            }

            // 验证宠物数量是否超过房间容量
            List<Long> petIds = dto.getPetIds();
            if (petIds == null || petIds.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请至少选择一只宠物");
            }
            if (petIds.size() > room.getMaxPetNum()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, 
                    String.format("该房间最多容纳 %d 只宠物，您选择了 %d 只", room.getMaxPetNum(), petIds.size()));
            }

            // 验证宠物类型是否与房间类型匹配
            List<Pet> pets = petService.listByIds(petIds);
            if (pets.size() != petIds.size()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "部分宠物不存在");
            }
            
            // 检查所有宠物是否属于当前用户
            boolean allOwnedByUser = pets.stream().allMatch(pet -> pet.getUserId().equals(userId));
            if (!allOwnedByUser) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "只能选择自己的宠物");
            }
            
            // 判断是否为VIP套间（允许混搭）
            String roomTypeName = room.getTypeName().toLowerCase();
            boolean isVIPRoom = roomTypeName.contains("vip") || roomTypeName.contains("suite");
            
            if (!isVIPRoom) {
                // 非VIP房间需要检查宠物类型限制
                // 1. 检查房间类型对应的允许宠物类型
                Integer allowedPetType;
                if (roomTypeName.contains("cat")) {
                    allowedPetType = 1; // 猫咪房间只允许猫
                } else if (roomTypeName.contains("dog")) {
                    allowedPetType = 2; // 狗狗房间只允许狗
                } else {
                    allowedPetType = null;
                }

                // 2. 验证所有宠物类型是否符合房间要求
                if (allowedPetType != null) {
                    boolean allPetsMatch = pets.stream().allMatch(pet -> pet.getType().equals(allowedPetType));
                    if (!allPetsMatch) {
                        String petTypeName = allowedPetType == 1 ? "猫咪" : "狗狗";
                        throw new BusinessException(ResultCode.PARAM_ERROR, 
                            String.format("该房间仅限%s入住，请选择相应类型的宠物", petTypeName));
                    }
                }
                
                // 3. 验证多只宠物时类型是否一致
                if (pets.size() > 1) {
                    long distinctTypes = pets.stream().map(Pet::getType).distinct().count();
                    if (distinctTypes > 1) {
                        throw new BusinessException(ResultCode.PARAM_ERROR, 
                            "该房间不允许不同类型的宠物一起居住，仅VIP套间支持混搭");
                    }
                }
            }

            // 双重检查日期是否有冲突（在锁保护下进行检查）
            final LocalDate newCheckInDate = dto.getCheckInDate();
            final LocalDate newCheckOutDate = dto.getCheckOutDate();
            long conflictCount = lambdaQuery()
                    .eq(Order::getRoomId, dto.getRoomId())
                    .eq(Order::getIsDeleted, 0)
                    .in(Order::getStatus, 
                        Constants.ORDER_STATUS_PENDING,      // 0 待支付
                        Constants.ORDER_STATUS_PAID,         // 1 待入住
                        Constants.ORDER_STATUS_CHECKED_IN)   // 2 入住中
                    .and(wrapper -> wrapper
                        .lt(Order::getCheckInDate, newCheckOutDate)  // 现有订单的入住日期 < 新订单的退房日期
                        .gt(Order::getCheckOutDate, newCheckInDate)   // 现有订单的退房日期 > 新订单的入住日期
                    )
                    .count();
            if (conflictCount > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "该房间在所选日期已被预订，请重新选择");
            }

            // 计算天数和金额
            long days = ChronoUnit.DAYS.between(newCheckInDate, newCheckOutDate);
            if (days <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "离店日期必须晚于入住日期");
            }
            BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(days));

            // 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setHotelId(dto.getHotelId());
            order.setPetIds(JSONUtil.toJsonStr(petIds));
            order.setRoomId(dto.getRoomId());
            order.setCheckInDate(newCheckInDate);
            order.setCheckOutDate(newCheckOutDate);
            order.setTotalAmount(totalAmount);
            order.setRemark(dto.getRemark());
            order.setStatus(Constants.ORDER_STATUS_PENDING);
            save(order);

            // 更新房间状态为已预订
            roomService.updateStatus(dto.getRoomId(), Constants.ROOM_STATUS_RESERVED);

            // 发送订单超时检查消息（1分钟延迟）
            rabbitMQUtil.sendOrderTimeoutMessage(order.getId(), order.getOrderNo(), 60 * 1000L);

            return order;
        } finally {
            // 释放锁（仅当持有锁时才释放）
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法支付");
        }

        // 更新订单状态和支付时间
        order.setStatus(Constants.ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        boolean updated = updateById(order);
        if (updated) {
            notifyPaymentSuccess(order);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING && 
            order.getStatus() != Constants.ORDER_STATUS_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法取消");
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        updateById(order);

        // 释放房间
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_FREE);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法办理入住");
        }

        // 校验当前日期是否是入住日期
        LocalDate today = LocalDate.now();
        LocalDate checkInDate = order.getCheckInDate();
        
        if (today.isBefore(checkInDate)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, 
                String.format("入住日期为 %s，当前无法办理入住", checkInDate));
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_CHECKED_IN);
        boolean updated = updateById(order);

        // 更新房间状态为入住中
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_OCCUPIED);

        if (updated) {
            notifyCheckIn(order);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_CHECKED_IN) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法办理退房");
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_COMPLETED);
        boolean updated = updateById(order);

        // 更新房间状态为待清洁
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_CLEANING);

        if (updated) {
            notifyCheckOut(order);
        }

        return updated;
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getOrderNo, orderNo).one();
    }

    @Override
    public long countTodayOrders(Long hotelId) {
        LocalDate today = LocalDate.now();
        return lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .ge(Order::getCreatedAt, today.atStartOfDay())
                .lt(Order::getCreatedAt, today.plusDays(1).atStartOfDay())
                .count();
    }

    @Override
    public long countCheckedInPets(Long hotelId) {
        return lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .eq(Order::getStatus, Constants.ORDER_STATUS_CHECKED_IN)
                .count();
    }

    @Override
    public BigDecimal calculateTodayRevenue(Long hotelId) {
        LocalDate today = LocalDate.now();
        List<Order> orders = lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .in(Order::getStatus, Constants.ORDER_STATUS_PAID, Constants.ORDER_STATUS_CHECKED_IN, Constants.ORDER_STATUS_COMPLETED)
                .ge(Order::getPayTime, today.atStartOfDay())
                .lt(Order::getPayTime, today.plusDays(1).atStartOfDay())
                .list();
        
        return orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateMonthRevenue(Long hotelId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);
        
        List<Order> orders = lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .in(Order::getStatus, Constants.ORDER_STATUS_PAID, Constants.ORDER_STATUS_CHECKED_IN, Constants.ORDER_STATUS_COMPLETED)
                .ge(Order::getPayTime, firstDayOfMonth.atStartOfDay())
                .lt(Order::getPayTime, firstDayOfNextMonth.atStartOfDay())
                .list();
        
        return orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotalRevenue(Long hotelId) {
        List<Order> orders = lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .in(Order::getStatus, Constants.ORDER_STATUS_PAID, Constants.ORDER_STATUS_CHECKED_IN, Constants.ORDER_STATUS_COMPLETED)
                .isNotNull(Order::getPayTime)
                .list();
        
        return orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void notifyPaymentSuccess(Order order) {
        Hotel hotel = hotelService.getById(order.getHotelId());
        Room room = roomService.getById(order.getRoomId());
        User user = userService.getById(order.getUserId());
        String hotelName = hotel != null ? hotel.getName() : "未知门店";
        String roomNo = room != null ? room.getRoomNo() : "未知房间";
        String userName = resolveUserName(user, order);
        int petCount = resolvePetCount(order);

        String content = String.format(
                "用户%s已支付订单%s，酒店：%s，房间：%s，入住：%s，离店：%s，宠物数：%d。请提前做好接待准备。",
                userName, order.getOrderNo(), hotelName, roomNo, order.getCheckInDate(), order.getCheckOutDate(), petCount
        );
        notifyHotelStaff(order, "订单支付成功提醒", content);
    }

    private void notifyCheckIn(Order order) {
        Hotel hotel = hotelService.getById(order.getHotelId());
        Room room = roomService.getById(order.getRoomId());
        String hotelName = hotel != null ? hotel.getName() : "未知门店";
        String roomNo = room != null ? room.getRoomNo() : "未知房间";
        int petCount = resolvePetCount(order);

        String content = String.format(
                "订单%s已办理入住，酒店：%s，房间：%s，入住：%s，预计离店：%s，宠物数：%d。请做好宠物护理提醒与记录。",
                order.getOrderNo(), hotelName, roomNo, order.getCheckInDate(), order.getCheckOutDate(), petCount
        );
        notifyHotelStaff(order, "客户已入住提醒", content);
    }

    private void notifyCheckOut(Order order) {
        Hotel hotel = hotelService.getById(order.getHotelId());
        Room room = roomService.getById(order.getRoomId());
        String hotelName = hotel != null ? hotel.getName() : "未知门店";
        String roomNo = room != null ? room.getRoomNo() : "未知房间";

        String content = String.format(
                "订单%s已退房，酒店：%s，房间：%s。请及时安排房间清洁与消杀，确保后续入住体验。",
                order.getOrderNo(), hotelName, roomNo
        );
        notifyHotelStaff(order, "客户已退房提醒", content);
    }

    private void notifyHotelStaff(Order order, String title, String content) {
        if (order == null || order.getHotelId() == null) {
            return;
        }
        List<Staff> staffList = staffService.listEnabledByHotel(order.getHotelId());
        if (staffList == null || staffList.isEmpty()) {
            return;
        }

        // 消息中心：门店共享一条消息（任何同酒店员工看到，一人已读即代表门店完成）
        messageCenterService.createMessage(order.getHotelId(), Constants.USER_TYPE_STAFF, null, title, content);

        // 推送：仍按员工逐个推送（用于实时通知）
        for (Staff staff : staffList) {
            rabbitMQUtil.sendNotification(staff.getId(), title, content);
        }
    }

    private String resolveUserName(User user, Order order) {
        if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
            return user.getNickname();
        }
        return "用户" + order.getUserId();
    }

    private int resolvePetCount(Order order) {
        try {
            return JSONUtil.parseArray(order.getPetIds()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PH" + LocalDateTimeUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") 
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
