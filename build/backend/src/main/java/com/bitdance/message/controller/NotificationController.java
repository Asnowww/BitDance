package com.bitdance.message.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.message.dto.NotificationListResponse;
import com.bitdance.message.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/h5/messages")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> list(
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.list(CurrentUser.getId(), category, page, pageSize));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Map<String, Object>> markRead(@PathVariable Long id) {
        service.markRead(CurrentUser.getId(), id);
        return ApiResponse.ok(Map.of("ok", true));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllRead() {
        int affected = service.markAllRead(CurrentUser.getId());
        return ApiResponse.ok(Map.of("ok", true, "affected", affected));
    }
}
