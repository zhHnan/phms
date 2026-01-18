package com.phms.util;

import com.phms.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 *
 * @author PHMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 检查桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查桶是否存在失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 创建桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建桶失败: {}", bucketName, e);
            throw new RuntimeException("创建桶失败", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file       文件
     * @param bucketName 桶名称
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String bucketName) {
        try {
            // 确保桶存在
            createBucket(bucketName);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功: {}/{}", bucketName, fileName);
            return fileName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传文件（使用默认桶）
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, minioConfig.getBucketName());
    }

    /**
     * 获取文件访问URL（带过期时间）
     *
     * @param bucketName 桶名称
     * @param fileName   文件名
     * @param expiry     过期时间（秒）
     * @return 访问URL
     */
    public String getPresignedUrl(String bucketName, String fileName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(fileName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取文件URL失败: {}/{}", bucketName, fileName, e);
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    /**
     * 获取文件访问URL（默认7天过期）
     */
    public String getPresignedUrl(String bucketName, String fileName) {
        return getPresignedUrl(bucketName, fileName, 7 * 24 * 60 * 60);
    }

    /**
     * 获取文件公开访问URL（永久有效，需要bucket设置为公开读取）
     *
     * @param bucketName 桶名称
     * @param fileName   文件名
     * @return 公开访问URL
     */
    public String getPublicUrl(String bucketName, String fileName) {
        // 构建公开访问URL: http://endpoint/bucketName/fileName
        String endpoint = minioConfig.getEndpoint();
        return String.format("%s/%s/%s", endpoint, bucketName, fileName);
    }

    /**
     * 删除文件
     */
    public void deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            log.info("文件删除成功: {}/{}", bucketName, fileName);
        } catch (Exception e) {
            log.error("文件删除失败: {}/{}", bucketName, fileName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String bucketName, String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: {}/{}", bucketName, fileName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }
}
