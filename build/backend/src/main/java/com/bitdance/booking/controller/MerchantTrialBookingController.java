package com.bitdance.booking.controller;

import com.bitdance.booking.dto.BookingDto;
import com.bitdance.booking.dto.RejectBookingRequest;
import com.bitdance.booking.service.MerchantTrialBookingService;
import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchant/trial-bookings")
public class MerchantTrialBookingController {

    private final MerchantTrialBookingService service;

    public MerchantTrialBookingController(MerchantTrialBookingService service) {
        this.service = service;
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<BookingDto> confirm(@PathVariable Long id) {
        return ApiResponse.ok(service.confirm(CurrentUser.getId(), id));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<BookingDto> reject(
        @PathVariable Long id,
        @Valid @RequestBody(required = false) RejectBookingRequest body
    ) {
        String reason = body == null ? null : body.reason();
        return ApiResponse.ok(service.reject(CurrentUser.getId(), id, reason));
    }

    @PostMapping("/{id}/attend")
    public ApiResponse<BookingDto> attend(@PathVariable Long id) {
        return ApiResponse.ok(service.attend(CurrentUser.getId(), id));
    }

    @PostMapping("/{id}/no-show")
    public ApiResponse<BookingDto> noShow(@PathVariable Long id) {
        return ApiResponse.ok(service.noShow(CurrentUser.getId(), id));
    }
}
