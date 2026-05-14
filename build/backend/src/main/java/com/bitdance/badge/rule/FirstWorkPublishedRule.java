package com.bitdance.badge.rule;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirstWorkPublishedRule implements BadgeRule {
    @Override public String type() { return "first_work_published"; }

    @Override
    public boolean shouldGrant(String eventType, Map<String, Object> metadata, Map<String, Object> config) {
        if (!"work_published".equals(eventType)) return false;
        return CheckinStreakRule.readInt(metadata, "totalCount", 0) == 1;
    }
}
