package org.entropy.restclient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.entropy.restclient.mapper")
public class RestClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestClientApplication.class, args);
    }
}
