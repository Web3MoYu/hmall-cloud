package com.hmall.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfig {

    // 启动OpenFeign的日志，需要首先在yml中将feign所在包下的日志设为debug，再在启动类上指定配置
    /*
    logging:
      level:
        com.hmall: debug
     */
    @Bean
    public Logger.Level feiginLoggerLevel(){
        return Logger.Level.FULL;
    }
}
