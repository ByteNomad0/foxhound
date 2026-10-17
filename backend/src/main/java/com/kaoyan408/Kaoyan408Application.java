package com.kaoyan408;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kaoyan408.mapper")
public class Kaoyan408Application {

    public static void main(String[] args) {
        SpringApplication.run(Kaoyan408Application.class, args);
    }
}
