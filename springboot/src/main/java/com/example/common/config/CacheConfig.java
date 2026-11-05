package com.example.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 查询缓存配置（Spring Cache）：
 * 仅缓存无用户上下文的热点只读查询，写操作通过 @CacheEvict 即时失效。
 * 各缓存独立 TTL：热榜/活动榜 60s、分类 10min、仪表盘 30s。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // 默认规格：60 秒过期，容量兜底
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .maximumSize(10));
        manager.setCacheNames(java.util.Arrays.asList("blogTop", "activityTop", "categoryAll", "dashboardStats"));
        // 分类列表几乎不变：10 分钟长缓存
        manager.registerCustomCache("categoryAll",
                Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(2).build());
        // 仪表盘一次请求 17 个查询：30 秒短缓存足够缓解高频刷新
        manager.registerCustomCache("dashboardStats",
                Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(2).build());
        return manager;
    }
}
