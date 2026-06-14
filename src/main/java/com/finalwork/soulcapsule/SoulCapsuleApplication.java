package com.finalwork.soulcapsule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.finalwork.soulcapsule.mapper")
public class SoulCapsuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoulCapsuleApplication.class, args);
    }
}
