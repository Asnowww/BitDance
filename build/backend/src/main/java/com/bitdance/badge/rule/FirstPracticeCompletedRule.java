package com.bitdance.badge.rule;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirstPracticeCompletedRule implements BadgeRule {
    @Override public String type() { return "first_practice_completed"; }

    @Override
    public boolean shouldGrant(String eventType, Map<String, Object> metadata, Map<String, Object> config) {
        if (!"practice_completed".equals(eventType)) return false;
        return CheckinStreakRule.readInt(metadata, "totalCount", 0) == 1;
    }
}
