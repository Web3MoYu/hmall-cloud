package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {

    // 启动OpenFeign的日志，需要首先在yml中将feign所在包下的日志设为debug，再在启动类上指定配置
    /*
    logging:
      level:
        com.hmall: debug
     */
    @Bean
    public Logger.Level feiginLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return template -> {
            Long user = UserContext.getUser();
            if (user != null) {
                template.header("user-info", user.toString());
            }
        };
    }
}
