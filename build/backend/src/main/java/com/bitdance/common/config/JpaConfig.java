package com.bitdance.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        // M1/M2 本机验收：实体审计字段统一使用 OffsetDateTime，避免新增用户/评价/收藏时 LocalDateTime 转换失败。
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
