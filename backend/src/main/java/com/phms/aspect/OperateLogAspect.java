package com.phms.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.phms.common.annotation.OperateLog;
import com.phms.common.constant.Constants;
import com.phms.entity.OperationLog;
import com.phms.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 *
 * @author PHMS
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperationLogService operationLogService;

    /**
     * 敏感字段列表（需脱敏）
     */
    private static final String[] SENSITIVE_FIELDS = {"password", "pwd", "secret", "token"};

    /**
     * 切点：标注了 @OperateLog 注解的方法
     */
    @Pointcut("@annotation(com.phms.common.annotation.OperateLog)")
    public void operateLogPointcut() {
    }

    /**
     * 正常返回后记录日志
     */
    @AfterReturning(pointcut = "operateLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null);
    }

    /**
     * 异常抛出后记录日志
     */
    @AfterThrowing(pointcut = "operateLogPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e);
    }

    /**
     * 处理日志记录
     */
    @Async
    protected void handleLog(JoinPoint joinPoint, Exception e) {
        try {
            // 获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperateLog operateLog = method.getAnnotation(OperateLog.class);

            if (operateLog == null) {
                return;
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 构建日志对象
            OperationLog logEntity = new OperationLog();
            logEntity.setOperationModule(operateLog.module());
            logEntity.setOperationType(operateLog.type());
            logEntity.setOperationUrl(request.getRequestURI());
            logEntity.setOperationIp(getIpAddress(request));

            // 获取操作人信息
            if (StpUtil.isLogin()) {
                Object userType = StpUtil.getSession().get("userType");
                if (userType != null) {
                    logEntity.setOperatorType((Integer) userType);
                    logEntity.setOperatorId(StpUtil.getLoginIdAsLong());
                    Object name = StpUtil.getSession().get("name");
                    logEntity.setOperatorName(name != null ? name.toString() : "未知");
                }
            } else {
                logEntity.setOperatorType(Constants.USER_TYPE_CLIENT);
                logEntity.setOperatorId(0L);
                logEntity.setOperatorName("未登录用户");
            }

            // 获取请求参数（脱敏处理）
            String params = getRequestParams(joinPoint);
            logEntity.setOperationParam(params);

            // 设置操作结果
            if (e != null) {
                logEntity.setOperationResult(0);
                logEntity.setFailMsg(StrUtil.sub(e.getMessage(), 0, 255));
            } else {
                logEntity.setOperationResult(1);
            }

            // 保存日志
            operationLogService.save(logEntity);
        } catch (Exception ex) {
            log.error("记录操作日志失败", ex);
        }
    }

    /**
     * 获取请求参数（脱敏处理）
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object paramValue = args[i];

                // 敏感字段脱敏
                boolean isSensitive = false;
                for (String field : SENSITIVE_FIELDS) {
                    if (paramName.toLowerCase().contains(field)) {
                        isSensitive = true;
                        break;
                    }
                }

                if (isSensitive) {
                    params.put(paramName, "***");
                } else if (paramValue != null) {
                    params.put(paramName, paramValue);
                }
            }

            return JSONUtil.toJsonStr(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
