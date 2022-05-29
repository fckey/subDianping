package com.circle;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fangshaolei
 * @version 1.0.0
 * @ClassName CircleDianPingApplication
 * @Description
 * @createTime 2022/05/29 17:21
 **/
@MapperScan("com.circle.mapper")
@SpringBootApplication
public class CircleDianPingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircleDianPingApplication.class, args);
    }
}
