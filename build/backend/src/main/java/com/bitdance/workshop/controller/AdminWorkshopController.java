package com.bitdance.workshop.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.workshop.dto.WorkshopAdminItem;
import com.bitdance.workshop.service.AdminWorkshopService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminWorkshopController {

    private final AdminWorkshopService service;

    public AdminWorkshopController(AdminWorkshopService service) {
        this.service = service;
    }

    @GetMapping({"/admin/workshops", "/h5/coach/platform/workshops"})
    public ApiResponse<Page<WorkshopAdminItem>> list(
        @RequestParam(required = false) String auditStatus,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.listByAuditStatus(auditStatus, page, pageSize));
    }

    @PostMapping({"/admin/workshops/{id}/approve", "/h5/coach/platform/workshops/{id}/approve"})
    public ApiResponse<WorkshopAdminItem> approve(@PathVariable Long id) {
        return ApiResponse.ok(service.approve(CurrentUser.getId(), id));
    }

    @PostMapping({"/admin/workshops/{id}/reject", "/h5/coach/platform/workshops/{id}/reject"})
    public ApiResponse<WorkshopAdminItem> reject(@PathVariable Long id) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id));
    }
}
