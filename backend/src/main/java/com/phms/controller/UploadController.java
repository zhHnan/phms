package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.phms.common.result.Result;
import com.phms.util.MinioUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author PHMS
 */
@Slf4j
@Tag(name = "文件上传", description = "文件上传和管理接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final MinioUtil minioUtil;

    @Operation(summary = "上传单个文件")
    @PostMapping("/single")
    @SaCheckLogin
    public Result<Map<String, String>> uploadSingle(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "桶名称，默认为phms") @RequestParam(required = false, defaultValue = "phms") String bucket) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.fail("文件不能为空");
            }

            // 验证文件大小（最大10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.fail("文件大小不能超过10MB");
            }

            // 验证文件类型（只允许图片）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.fail("只支持上传图片文件");
            }

            // 上传文件
            String fileName = minioUtil.uploadFile(file, bucket);
            String url = minioUtil.getPublicUrl(bucket, fileName); // 使用公开访问URL

            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("url", url);

            log.info("文件上传成功: {}, URL: {}", fileName, url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传多个文件")
    @PostMapping("/multiple")
    @SaCheckLogin
    public Result<List<Map<String, String>>> uploadMultiple(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "桶名称，默认为phms") @RequestParam(required = false, defaultValue = "phms") String bucket) {
        try {
            if (files == null || files.length == 0) {
                return Result.fail("请选择要上传的文件");
            }

            // 限制最多上传10张图片
            if (files.length > 10) {
                return Result.fail("最多只能上传10张图片");
            }

            List<Map<String, String>> results = new ArrayList<>();

            for (MultipartFile file : files) {
                // 验证文件
                if (file.isEmpty()) {
                    continue;
                }

                // 验证文件大小
                if (file.getSize() > 10 * 1024 * 1024) {
                    log.warn("文件 {} 大小超过10MB，已跳过", file.getOriginalFilename());
                    continue;
                }

                // 验证文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    log.warn("文件 {} 不是图片类型，已跳过", file.getOriginalFilename());
                    continue;
                }

                try {
                    // 上传文件
                    String fileName = minioUtil.uploadFile(file, bucket);
                    String url = minioUtil.getPublicUrl(bucket, fileName); // 使用公开访问URL

                    Map<String, String> result = new HashMap<>();
                    result.put("fileName", fileName);
                    result.put("url", url);
                    results.add(result);

                    log.info("文件上传成功: {}, URL: {}", fileName, url);
                } catch (Exception e) {
                    log.error("文件 {} 上传失败", file.getOriginalFilename(), e);
                }
            }

            if (results.isEmpty()) {
                return Result.fail("没有文件上传成功");
            }

            return Result.success(results);
        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            return Result.fail("批量上传文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    @SaCheckLogin
    public Result<Void> deleteFile(
            @Parameter(description = "文件名") @RequestParam String fileName,
            @Parameter(description = "桶名称，默认为phms") @RequestParam(required = false, defaultValue = "phms") String bucket) {
        try {
            minioUtil.deleteFile(bucket, fileName);
            log.info("文件删除成功: {}/{}", bucket, fileName);
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败: {}/{}", bucket, fileName, e);
            return Result.fail("文件删除失败: " + e.getMessage());
        }
    }
}
