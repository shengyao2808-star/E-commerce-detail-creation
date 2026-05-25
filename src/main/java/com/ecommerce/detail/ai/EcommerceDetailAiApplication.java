package com.ecommerce.detail.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 电商详情页AI工作台主应用类
 * 
 * @author Administrator
 * @version 1.0.0
 * @since 2026-05-24
 */
@SpringBootApplication
@MapperScan("com.ecommerce.detail.ai.mapper")
@EnableAsync
public class EcommerceDetailAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceDetailAiApplication.class, args);
    }
}
