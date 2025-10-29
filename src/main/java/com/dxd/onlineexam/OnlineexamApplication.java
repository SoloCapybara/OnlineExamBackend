package com.dxd.onlineexam;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.dxd.onlineexam.mapper")
public class OnlineexamApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineexamApplication.class, args);
    }

    @PostConstruct
    public void init(){
        // 设置应用默认时区为上海，确保时间计算与中国时区一致
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        System.out.println("当前时区已设置为:" + TimeZone.getDefault().getID());
    }
}
