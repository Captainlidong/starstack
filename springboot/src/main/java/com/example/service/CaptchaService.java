package com.example.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码服务：答案只存服务端内存（单实例部署），验证码一次性使用，2分钟过期
 */
@Service
public class CaptchaService {

    private static final long EXPIRE_MS = 2 * 60 * 1000L;
    private static final int MAX_SIZE = 10000;

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    /**
     * 生成验证码，返回 key 与图片（dataURL 形式，前端直接作为 img src）
     */
    public Map<String, String> generate() {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(130, 40, 4, 8);
        String key = IdUtil.fastSimpleUUID();
        long now = System.currentTimeMillis();
        store.put(key, new CaptchaEntry(captcha.getCode(), now + EXPIRE_MS));
        // 防止异常流量撑爆内存：达到上限时清理一次已过期的条目
        if (store.size() >= MAX_SIZE) {
            store.values().removeIf(entry -> entry.expireAt < now);
        }
        Map<String, String> data = new HashMap<>();
        data.put("key", key);
        data.put("image", captcha.getImageBase64Data());
        return data;
    }

    /**
     * 校验并销毁：无论对错，取出的验证码立即失效，防止重放
     */
    public boolean verify(String key, String code) {
        if (StrUtil.isBlank(key) || StrUtil.isBlank(code)) {
            return false;
        }
        CaptchaEntry entry = store.remove(key);
        return entry != null && entry.expireAt > System.currentTimeMillis()
                && entry.code.equalsIgnoreCase(code.trim());
    }

    private static class CaptchaEntry {
        final String code;
        final long expireAt;

        CaptchaEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
