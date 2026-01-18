package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONUtil;
import com.phms.common.result.Result;
import com.phms.util.MinioUtil;
import com.phms.util.RabbitMQUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统测试控制器（用于测试MinIO和RabbitMQ连接）
 *
 * @author PHMS
 */
@Slf4j
@Tag(name = "系统测试", description = "用于测试MinIO和RabbitMQ连接")
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final MinioUtil minioUtil;
    private final RabbitMQUtil rabbitMQUtil;

    @Operation(summary = "测试MinIO连接")
    @GetMapping("/minio")
//    @SaCheckPermission("dashboard:view")
    public Result<Map<String, Object>> testMinio() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查默认桶是否存在
            String bucketName = "phms";
            boolean exists = minioUtil.bucketExists(bucketName);
            result.put("bucketExists", exists);
            
            // 如果不存在则创建
            if (!exists) {
                minioUtil.createBucket(bucketName);
                result.put("bucketCreated", true);
            }
            
            result.put("status", "success");
            result.put("message", "MinIO连接正常");
            
            log.info("MinIO连接测试成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("MinIO连接测试失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "MinIO连接失败: " + e.getMessage());
            return Result.fail(JSONUtil.toJsonStr(error));
        }
    }

    @Operation(summary = "测试RabbitMQ连接")
    @GetMapping("/rabbitmq")
//    @SaCheckPermission("dashboard:view")
    public Result<Map<String, Object>> testRabbitMQ() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 发送测试消息
            rabbitMQUtil.sendNotification(
                    1L, 
                    "系统测试", 
                    "这是一条RabbitMQ连接测试消息"
            );
            
            result.put("status", "success");
            result.put("message", "RabbitMQ连接正常，测试消息已发送");
            
            log.info("RabbitMQ连接测试成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("RabbitMQ连接测试失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "RabbitMQ连接失败: " + e.getMessage());
            return Result.fail(JSONUtil.toJsonStr(error));
        }
    }

    @Operation(summary = "测试文件上传")
    @PostMapping("/upload")
//    @SaCheckPermission("dashboard:view")
    public Result<Map<String, Object>> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 上传文件
            String fileName = minioUtil.uploadFile(file, "phms");
            result.put("fileName", fileName);
            
            // 获取访问URL
            String url = minioUtil.getPresignedUrl("phms", fileName);
            result.put("url", url);
            
            result.put("status", "success");
            result.put("message", "文件上传成功");
            
            log.info("文件上传测试成功: {}", fileName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传测试失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "文件上传失败: " + e.getMessage());
            return Result.fail(JSONUtil.toJsonStr(error));
        }
    }

    @Operation(summary = "测试订单超时消息（快速测试：10秒延迟）")
    @GetMapping("/order-timeout")
    public Result<String> testOrderTimeout(
            @RequestParam Long orderId,
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "10") int delaySeconds) {
        
        long delayMillis = delaySeconds * 1000L;
        
        log.info("测试发送订单超时消息: orderId={}, orderNo={}, 延迟={}秒", orderId, orderNo, delaySeconds);
        
        rabbitMQUtil.sendOrderTimeoutMessage(orderId, orderNo, delayMillis);
        
        String message = String.format("订单超时测试消息已发送！订单ID: %d, 将在 %d 秒后触发超时检查", orderId, delaySeconds);
        log.info(message);
        
        return Result.success(message);
    }
}

