package com.yanweiyi.micodebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yanweiyi.micodebackend.mapper")
public class MicodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicodeBackendApplication.class, args);
    }

}
