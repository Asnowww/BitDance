package com.bitdance.booking.controller;

import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.dto.CancelBookingRequest;
import com.bitdance.booking.dto.CreateBookingRequest;
import com.bitdance.booking.service.TrialBookingService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/h5/trial-bookings")
public class TrialBookingController {

    private final TrialBookingService service;

    public TrialBookingController(TrialBookingService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<BookingDto> create(@Valid @RequestBody CreateBookingRequest body) {
        return ApiResponse.ok(service.create(CurrentUser.getId(), body));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingDto> cancel(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) CancelBookingRequest body
    ) {
        String reason = body == null ? null : body.reason();
        return ApiResponse.ok(service.cancel(CurrentUser.getId(), id, reason));
    }

    @GetMapping
    public ApiResponse<List<BookingDto>> listMine() {
        return ApiResponse.ok(service.listMine(CurrentUser.getId()));
    }
}
