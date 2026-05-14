package com.bitdance.audit.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要审计的 Service 方法。AOP 切面 AuditLogAspect 拦截带本注解的方法，
 * 成功执行后调用 AuditLogService.record 写入 audit_log。
 *
 * targetId 提取规则（按优先级）：
 *  1. 方法返回值是 record，且首字段是 Long/Number → 用它（适合 DTO id 在首位的设计）
 *  2. 方法返回值有 .getId() / .id() 方法 → 用它
 *  3. 否则取空 → service 内的 record 直接跳过日志（防止 schema target_id NOT NULL 违反）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditAction {
    /** 动作代码，如 "studio.claim.approve"。 */
    String value();
    /** 目标类型，如 "studio_claim"。 */
    String targetType();
}
