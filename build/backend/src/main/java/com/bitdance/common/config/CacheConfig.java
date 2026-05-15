package com.bitdance.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存抽象配置：
 *  - 默认（local/test）走 spring.cache.type=simple（ConcurrentMapCacheManager），无外部依赖
 *  - 生产（prod）profile 切到 spring.cache.type=redis（spring-boot-starter-data-redis 已引入）
 * 切换由 application-*.yml 控制，本类只负责开关 @EnableCaching。
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
