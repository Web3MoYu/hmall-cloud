package com.hmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 因为spring容器默认只会扫描启动类包下包及其子类，client扫描不出来，所以需要指定
@EnableFeignClients(basePackages = "com.hmall.api.client")
@SpringBootApplication
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
