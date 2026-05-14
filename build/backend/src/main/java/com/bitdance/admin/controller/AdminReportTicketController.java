package com.bitdance.admin.controller;

import com.bitdance.admin.dto.HandleReportRequest;
import com.bitdance.admin.dto.ReportTicketDto;
import com.bitdance.admin.service.AdminReportTicketService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/report-tickets")
public class AdminReportTicketController {

    private final AdminReportTicketService service;

    public AdminReportTicketController(AdminReportTicketService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Page<ReportTicketDto>> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String targetType,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.list(status, targetType, page, pageSize));
    }

    @PostMapping("/{id}/process")
    public ApiResponse<ReportTicketDto> process(@PathVariable Long id) {
        return ApiResponse.ok(service.process(CurrentUser.getId(), id));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<ReportTicketDto> close(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleReportRequest body
    ) {
        return ApiResponse.ok(service.close(CurrentUser.getId(), id, body));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ReportTicketDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) HandleReportRequest body
    ) {
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, body));
    }
}
