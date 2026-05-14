package com.bitdance.audit.service;

import com.bitdance.audit.domain.AuditLog;
import com.bitdance.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审计日志写入服务。
 *
 * 设计目标：
 *  - 业务事务失败不应导致审计日志一起回滚（用 REQUIRES_NEW 独立事务）
 *  - 审计失败不应反向影响业务（try/catch 包裹，仅 log.warn）
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorUserId, String actorRoleCode,
                       String actionCode, String targetType, Long targetId) {
        if (targetId == null) {
            log.debug("Skip audit log: targetId is null for action {}", actionCode);
            return;
        }
        try {
            AuditLog a = new AuditLog();
            a.setActorUserId(actorUserId);
            a.setActorRoleCode(actorRoleCode);
            a.setActionCode(actionCode);
            a.setTargetType(targetType);
            a.setTargetId(targetId);
            repo.save(a);
        } catch (RuntimeException ex) {
            log.warn("audit log write failed action={} targetType={} targetId={}",
                actionCode, targetType, targetId, ex);
        }
    }
}
