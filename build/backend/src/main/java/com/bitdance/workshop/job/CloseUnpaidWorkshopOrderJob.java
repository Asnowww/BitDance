package com.bitdance.workshop.job;

import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * pending_payment 订单 30 分钟内未支付自动关单。
 * 每 5 分钟扫描一次。失败仅 log.warn，不影响下一轮。
 */
@Component
public class CloseUnpaidWorkshopOrderJob {

    private static final Logger log = LoggerFactory.getLogger(CloseUnpaidWorkshopOrderJob.class);
    private static final long UNPAID_TIMEOUT_MINUTES = 30L;

    private final WorkshopOrderRepository repo;

    public CloseUnpaidWorkshopOrderJob(WorkshopOrderRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    @Transactional
    public int runOnce() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(UNPAID_TIMEOUT_MINUTES);
        List<WorkshopOrder> stale;
        try {
            stale = repo.findByOrderStatusAndCreatedAtBefore("pending_payment", cutoff);
        } catch (RuntimeException ex) {
            log.warn("CloseUnpaidWorkshopOrderJob: cannot query stale orders", ex);
            return 0;
        }
        int closed = 0;
        OffsetDateTime now = OffsetDateTime.now();
        for (WorkshopOrder o : stale) {
            try {
                o.setOrderStatus("canceled");
                o.setCanceledAt(now);
                repo.save(o);
                closed++;
            } catch (RuntimeException ex) {
                log.warn("CloseUnpaidWorkshopOrderJob: failed to cancel order id={}", o.getId(), ex);
            }
        }
        if (closed > 0) {
            log.info("CloseUnpaidWorkshopOrderJob: canceled {} unpaid workshop orders", closed);
        }
        return closed;
    }
}
