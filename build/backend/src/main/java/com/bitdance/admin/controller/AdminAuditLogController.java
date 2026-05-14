package com.bitdance.admin.controller;

import com.bitdance.admin.dto.AuditLogDto;
import com.bitdance.audit.domain.AuditLog;
import com.bitdance.audit.repository.AuditLogRepository;
import com.bitdance.common.web.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/audit-log")
public class AdminAuditLogController {

    private final AuditLogRepository repo;

    public AdminAuditLogController(AuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ApiResponse<Page<AuditLogDto>> search(
        @RequestParam(required = false) Long actor,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String targetType,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        Page<AuditLog> result = repo.search(actor, action, targetType, PageRequest.of(p - 1, s));
        return ApiResponse.ok(result.map(this::toDto));
    }

    private AuditLogDto toDto(AuditLog a) {
        return new AuditLogDto(
            a.getId(), a.getActorUserId(), a.getActorRoleCode(),
            a.getActionCode(), a.getTargetType(), a.getTargetId(),
            a.getCreatedAt()
        );
    }
}
