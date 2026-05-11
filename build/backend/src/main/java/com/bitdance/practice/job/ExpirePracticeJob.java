package com.bitdance.practice.job;

import com.bitdance.practice.service.PracticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时关闭过期约练帖：每 5 分钟扫描一次，把 published / matched 中 expires_at < now 的转为 expired。
 *
 * 真库联调时可改为接 schema 的 fn_close_expired_practice_posts() 函数；目前用 JPA 路径方便单测。
 */
@Component
public class ExpirePracticeJob {

    private static final Logger log = LoggerFactory.getLogger(ExpirePracticeJob.class);

    private final PracticeService practiceService;

    public ExpirePracticeJob(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void run() {
        try {
            int affected = practiceService.closeExpired();
            if (affected > 0) {
                log.info("expired practice posts closed: {}", affected);
            }
        } catch (Exception ex) {
            log.error("close expired practice posts failed", ex);
        }
    }
}
