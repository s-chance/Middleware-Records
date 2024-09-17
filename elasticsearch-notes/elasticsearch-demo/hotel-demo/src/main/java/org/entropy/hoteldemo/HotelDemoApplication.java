package org.entropy.hoteldemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.entropy.hoteldemo.mapper")
public class HotelDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelDemoApplication.class, args);
	}

}
