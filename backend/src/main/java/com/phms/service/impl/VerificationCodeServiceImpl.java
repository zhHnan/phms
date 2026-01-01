package com.phms.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.entity.VerificationCode;
import com.phms.mapper.VerificationCodeMapper;
import com.phms.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 验证码 Service 实现类
 *
 * @author PHMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl extends ServiceImpl<VerificationCodeMapper, VerificationCode> 
        implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public String sendCode(String phone, Integer type) {
        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 存入Redis（5分钟有效期）
        String redisKey = Constants.REDIS_CODE_PREFIX + type + ":" + phone;
        redisTemplate.opsForValue().set(redisKey, code, Constants.CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 存入数据库（记录）
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setTarget(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(Constants.CODE_EXPIRE_MINUTES));
        verificationCode.setIsUsed(0);
        save(verificationCode);
        
        // 模拟发送短信（实际生产环境对接短信服务商）
        log.info("【宠物酒店】验证码已发送到 {}，验证码：{}，有效期 {} 分钟", 
                phone, code, Constants.CODE_EXPIRE_MINUTES);
        
        // 返回验证码（仅测试环境，生产环境应返回null）
        return code;
    }

    @Override
    public boolean verifyCode(String phone, String code, Integer type) {
        String redisKey = Constants.REDIS_CODE_PREFIX + type + ":" + phone;
        String cachedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证成功，删除验证码
            redisTemplate.delete(redisKey);
            
            // 更新数据库记录为已使用
            lambdaUpdate()
                    .eq(VerificationCode::getTarget, phone)
                    .eq(VerificationCode::getCode, code)
                    .eq(VerificationCode::getType, type)
                    .set(VerificationCode::getIsUsed, 1)
                    .update();
            
            return true;
        }
        
        return false;
    }
}
