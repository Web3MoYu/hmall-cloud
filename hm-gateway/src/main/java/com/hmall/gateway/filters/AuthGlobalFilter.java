package com.hmall.gateway.filters;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Resource
    private AuthProperties authProperties;

    @Resource
    private JwtTool jwtTool;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取request对象
        ServerHttpRequest request = exchange.getRequest();
        // 2.判断是否需要登录拦截
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        // 3.获取token
        String token = null;
        List<String> tokenList = request.getHeaders().get("authorization");
        if (tokenList != null && !tokenList.isEmpty()) {
            token = tokenList.get(0);
        }
        // 4.校验token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5.传递用户信息
        String userInfo = userId.toString();
        // 重新构建exchange
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 在common中添加拦截器设置ThreadLocal保证每个微服务能拿到user-info
        return chain.filter(newExchange);
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (pathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    // 控制顺序在NettyRoutingFilter之前
    @Override
    public int getOrder() {
        return 0;
    }
}
