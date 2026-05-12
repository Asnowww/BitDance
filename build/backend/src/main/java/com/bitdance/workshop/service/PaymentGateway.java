package com.bitdance.workshop.service;

import com.bitdance.workshop.domain.WorkshopOrder;

/**
 * 支付网关抽象。
 *
 * MVP 阶段由 MockPaymentGateway 实现：直接把订单标记成 paid。
 * 真实接入微信支付时新增 WechatPayGateway 替换 bean。
 */
public interface PaymentGateway {

    /**
     * 发起支付。
     *
     * @param order 待支付订单（order_status='pending_payment'）
     * @return 支付流水号 payment_txn_no
     */
    String charge(WorkshopOrder order);
}
