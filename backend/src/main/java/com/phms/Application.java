package com.phms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 宠物酒店管理系统启动类
 *
 * @author PHMS
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.phms.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("===========================================");
        System.out.println("  宠物酒店管理系统 (PHMS) 启动成功!");
        System.out.println("===========================================");
    }
}
