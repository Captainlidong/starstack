package com.example.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 私信实时推送处理器：
 * 握手通过后按用户ID登记会话；业务层落库后调用 pushToUser 实时推送在线用户。
 * 连接仅承载轻量信号（新消息提醒 / 心跳），消息正文始终走 HTTP 落库，保证可靠性。
 */
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageWebSocketHandler.class);

    /** 用户ID -> 该用户的所有活跃连接（支持多端登录） */
    private static final Map<Integer, Set<WebSocketSession>> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_SESSIONS.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("WebSocket 连接建立: userId={}, 在线会话数={}", userId, ONLINE_SESSIONS.get(userId).size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> set = ONLINE_SESSIONS.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    ONLINE_SESSIONS.remove(userId);
                }
            }
            log.info("WebSocket 连接关闭: userId={}", userId);
        }
    }

    /**
     * 客户端仅发送心跳信号，业务消息始终走 HTTP 接口
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JSONObject json = JSONUtil.parseObj(message.getPayload());
            if ("ping".equals(json.getStr("type"))) {
                synchronized (session) {
                    session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                }
            }
        } catch (Exception e) {
            log.warn("WebSocket 心跳处理异常: {}", e.getMessage());
        }
    }

    /**
     * 向指定在线用户的所有连接推送实时消息（JSON 字符串）
     * @return 是否至少推送成功一次（在线判定）
     */
    public boolean pushToUser(Integer userId, String payloadJson) {
        Set<WebSocketSession> set = ONLINE_SESSIONS.get(userId);
        if (set == null || set.isEmpty()) {
            return false;
        }
        boolean delivered = false;
        for (WebSocketSession session : set) {
            if (!session.isOpen()) {
                set.remove(session);
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(payloadJson));
                }
                delivered = true;
            } catch (Exception e) {
                set.remove(session);
            }
        }
        return delivered;
    }
}
