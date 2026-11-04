package com.example.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.AdminService;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * WebSocket 握手拦截器：握手阶段校验 JWT 令牌，通过后把用户ID放入会话属性
 * （浏览器原生 WebSocket 无法自定义请求头，令牌通过 URL 参数传递，仅握手阶段使用）
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 1. 从 URL 参数获取令牌（ws 握手无法自定义 header）
            String token = servletRequest.getParameter(Constants.TOKEN);
            if (ObjectUtil.isEmpty(token)) {
                return false;
            }
            // 2. 解析令牌并验签（与 JwtInterceptor 同一套校验逻辑）
            String userRole = JWT.decode(token).getAudience().get(0);
            String userId = userRole.split("-")[0];
            String role = userRole.split("-")[1];
            Account account;
            if (RoleEnum.ADMIN.name().equals(role)) {
                account = adminService.selectById(Integer.valueOf(userId));
            } else if (RoleEnum.USER.name().equals(role)) {
                account = userService.selectById(Integer.valueOf(userId));
            } else {
                return false;
            }
            if (ObjectUtil.isNull(account)) {
                return false;
            }
            JWTVerifier jwtVerifier = com.auth0.jwt.JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            jwtVerifier.verify(token);
            // 3. 校验通过：把用户ID写入会话属性
            attributes.put("userId", account.getId());
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 握手认证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }
}
