package com.bitdance.coachops.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.repository.CoachByUserRepository;
import com.bitdance.common.exception.BizException;
import org.springframework.stereotype.Component;

/**
 * 教练身份守卫：调用 /h5/coach/** 接口前必须确认 user 已通过资质认证（coach 表有记录且 certification_status=approved）。
 */
@Component
public class CoachAccessGuard {

    private final CoachByUserRepository coachRepo;

    public CoachAccessGuard(CoachByUserRepository coachRepo) {
        this.coachRepo = coachRepo;
    }

    public Coach requireCoach(Long userId) {
        Coach c = coachRepo.findByUserId(userId)
            .orElseThrow(() -> new BizException("COACH_NOT_FOUND", "尚未通过教练认证"));
        if (!"approved".equals(c.getCertificationStatus())) {
            throw new BizException("COACH_NOT_APPROVED", "教练资质尚未通过审核");
        }
        return c;
    }
}
