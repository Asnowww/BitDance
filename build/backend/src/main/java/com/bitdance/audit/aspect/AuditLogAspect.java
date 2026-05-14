package com.bitdance.audit.aspect;

import com.bitdance.audit.service.AuditLogService;
import com.bitdance.iam.security.CurrentUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogService auditLogService;

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, AuditAction audit) throws Throwable {
        Object ret = pjp.proceed(); // 业务失败让原异常抛出，不审计
        try {
            Long actor = CurrentUser.getIdOrNull();
            Long targetId = extractTargetId(ret);
            auditLogService.record(actor, null, audit.value(), audit.targetType(), targetId);
        } catch (RuntimeException ex) {
            log.warn("audit log aspect failed action={}", audit.value(), ex);
        }
        return ret;
    }

    /** 优先 record 首组件，回退到 getId(). */
    private Long extractTargetId(Object ret) {
        if (ret == null) return null;
        Class<?> cls = ret.getClass();
        if (cls.isRecord()) {
            RecordComponent[] comps = cls.getRecordComponents();
            if (comps.length > 0) {
                try {
                    Object v = comps[0].getAccessor().invoke(ret);
                    if (v instanceof Number n) return n.longValue();
                } catch (ReflectiveOperationException ignored) {
                    // fall through
                }
            }
        }
        try {
            Method m = cls.getMethod("getId");
            Object v = m.invoke(ret);
            if (v instanceof Number n) return n.longValue();
        } catch (ReflectiveOperationException ignored) {
            // no id getter or invocation failed
        }
        return null;
    }
}
