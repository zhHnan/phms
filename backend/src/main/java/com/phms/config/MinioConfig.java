package com.phms.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 *
 * @author PHMS
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO 服务端点
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 默认桶名称
     */
    private String bucketName;

    /**
     * 图片桶名称
     */
    private String imageBucket;

    /**
     * 创建 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            log.info("MinIO 客户端初始化成功: {}", endpoint);
            return client;
        } catch (Exception e) {
            log.error("MinIO 客户端初始化失败", e);
            throw new RuntimeException("MinIO 客户端初始化失败", e);
        }
    }
}
