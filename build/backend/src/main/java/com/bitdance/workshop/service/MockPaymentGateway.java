package com.bitdance.workshop.service;

import com.bitdance.workshop.domain.WorkshopOrder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String charge(WorkshopOrder order) {
        // MVP：无真扣款，直接生成模拟流水号。
        return "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
