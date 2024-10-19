package com.hmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmall.domain.dto.OrderDetailDTO;
import com.hmall.domain.po.User;
import com.hmall.service.IItemService;
import com.hmall.service.IUserService;
import com.hmall.utils.JwtTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

@SpringBootTest
class ItemServiceImplTest {

    @Autowired
    protected IItemService itemService;

    @Resource
    private IUserService userService;

    @Autowired
    private JwtTool jwtTool;

    @Test
    void testJwt() {
        String token = jwtTool.createToken(1L, Duration.ofMinutes(30));
        System.out.println("token = " + token);
    }

    @Test
    void deductStock() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,"Jack");
        System.out.println(userService.list(wrapper));
    }
}