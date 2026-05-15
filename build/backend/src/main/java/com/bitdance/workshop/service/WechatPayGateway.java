package com.bitdance.workshop.service;

import com.bitdance.workshop.domain.WorkshopOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 微信支付网关占位。
 *
 * 启用条件：application-*.yml 设 bitdance.payment.provider=wechat。
 * 真实接入留 W6+：调用 Wxpay v3 统一下单 API、签名、回调验签、退款。
 * 当前抛 UnsupportedOperationException 让生产部署时强制失败而非默默走 mock，避免线上误用模拟支付。
 */
@Component("wechatPayGateway")
@ConditionalOnProperty(name = "bitdance.payment.provider", havingValue = "wechat")
public class WechatPayGateway implements PaymentGateway {

    @Override
    public String charge(WorkshopOrder order) {
        throw new UnsupportedOperationException(
            "WechatPayGateway not implemented yet. " +
            "Set bitdance.payment.provider=mock to fallback, " +
            "or implement WechatPay v3 integration in W6+."
        );
    }
}
