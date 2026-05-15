package com.bitdance.workshop.service;

import com.bitdance.workshop.domain.WorkshopOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 模拟支付网关：MVP 阶段默认实现，无真实扣款。
 *
 * 切换策略（在 application-prod.yml 配 bitdance.payment.provider=wechat）：
 *  - havingValue 不匹配且 missing-bean 兜底 → MockPaymentGateway 仍生效（本机/测试）
 *  - havingValue=wechat 时 WechatPayGateway 启用，本类靠 @ConditionalOnMissingBean 让位
 */
@Component
@ConditionalOnProperty(
    name = "bitdance.payment.provider",
    havingValue = "mock",
    matchIfMissing = true
)
@ConditionalOnMissingBean(name = "wechatPayGateway")
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String charge(WorkshopOrder order) {
        return "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
