package com.bitdance.workshop.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.workshop.dto.CheckinRequest;
import com.bitdance.workshop.dto.CreateOrderRequest;
import com.bitdance.workshop.dto.WorkshopCalendarEventDto;
import com.bitdance.workshop.dto.OrderDto;
import com.bitdance.workshop.dto.RefundRequest;
import com.bitdance.workshop.dto.WorkshopDetail;
import com.bitdance.workshop.dto.WorkshopListResponse;
import com.bitdance.workshop.service.WorkshopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class WorkshopController {

    private final WorkshopService service;

    public WorkshopController(WorkshopService service) {
        this.service = service;
    }

    // ---------- Browse ----------

    @GetMapping("/public/workshops")
    public ApiResponse<WorkshopListResponse> list(
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Long danceStyleId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.ok(service.list(cityId, danceStyleId, page, pageSize));
    }

    @GetMapping("/public/workshops/{id}")
    public ApiResponse<WorkshopDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id, CurrentUser.getIdOrNull()));
    }

    // ---------- Order ----------

    @PostMapping("/h5/workshop-orders")
    public ApiResponse<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest body) {
        return ApiResponse.ok(service.createOrder(CurrentUser.getId(), body));
    }

    @PostMapping("/h5/workshop-orders/{id}/pay")
    public ApiResponse<OrderDto> pay(@PathVariable Long id) {
        return ApiResponse.ok(service.pay(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/workshop-orders/{id}/cancel")
    public ApiResponse<OrderDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(CurrentUser.getId(), id));
    }

    @PostMapping("/h5/workshop-orders/{id}/refund")
    public ApiResponse<OrderDto> refund(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) RefundRequest body
    ) {
        return ApiResponse.ok(service.refund(CurrentUser.getId(), id, body));
    }

    @GetMapping("/h5/workshop-orders/mine")
    public ApiResponse<List<OrderDto>> mine() {
        return ApiResponse.ok(service.listMyOrders(CurrentUser.getId()));
    }

    @GetMapping("/h5/workshop-orders/{id}")
    public ApiResponse<OrderDto> myOrder(@PathVariable Long id) {
        return ApiResponse.ok(service.getMyOrder(CurrentUser.getId(), id));
    }

    @GetMapping("/h5/workshop-calendar")
    public ApiResponse<List<WorkshopCalendarEventDto>> calendar() {
        return ApiResponse.ok(service.listMyCalendar(CurrentUser.getId()));
    }

    @PostMapping("/h5/workshop-orders/{id}/checkin")
    public ApiResponse<OrderDto> checkin(
        @PathVariable Long id,
        @Valid @RequestBody CheckinRequest body
    ) {
        return ApiResponse.ok(service.checkin(CurrentUser.getId(), id, body));
    }
}
