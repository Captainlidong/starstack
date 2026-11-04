package com.example.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败锁定：同一账号（角色+用户名）连续密码错误达到上限后锁定一段时间
 * 内存实现，适用于单实例部署；集群部署可替换为 Redis 等集中式存储
 */
@Service
public class LoginAttemptService {

    /** 允许的最大连续密码错误次数 */
    private static final int MAX_ATTEMPTS = 5;
    /** 锁定时长（毫秒） */
    private static final long LOCK_MS = 5 * 60 * 1000L;
    /** 记录数上限保护，防止异常流量撑爆内存 */
    private static final int MAX_KEYS = 10000;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /**
     * 账号是否锁定中，返回剩余锁定毫秒数（0 表示未锁定）
     */
    public long getLockRemainingMs(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.lockUntil <= System.currentTimeMillis()) {
            return 0L;
        }
        return attempt.lockUntil - System.currentTimeMillis();
    }

    /**
     * 记录一次密码错误；连续错误达到上限即锁定，锁定到期后重新计数
     */
    public void onFailure(String key) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, old) -> {
            if (old == null) {
                old = new Attempt();
            }
            if (old.lockUntil > now) {
                return old; // 锁定期间不累计
            }
            if (old.lockUntil > 0) {
                old.count = 0; // 上一次锁定已到期，重新计数
                old.lockUntil = 0;
            }
            old.count++;
            if (old.count >= MAX_ATTEMPTS) {
                old.lockUntil = now + LOCK_MS;
            }
            return old;
        });
        if (attempts.size() >= MAX_KEYS) {
            attempts.values().removeIf(a -> a.lockUntil <= now);
        }
    }

    /**
     * 登录成功，清空失败计数
     */
    public void onSuccess(String key) {
        attempts.remove(key);
    }

    private static class Attempt {
        int count;
        long lockUntil;
    }
}
