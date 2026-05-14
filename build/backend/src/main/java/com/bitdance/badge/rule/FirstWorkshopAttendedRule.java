package com.bitdance.badge.rule;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirstWorkshopAttendedRule implements BadgeRule {
    @Override public String type() { return "first_workshop_attended"; }

    @Override
    public boolean shouldGrant(String eventType, Map<String, Object> metadata, Map<String, Object> config) {
        if (!"workshop_attended".equals(eventType)) return false;
        return CheckinStreakRule.readInt(metadata, "totalCount", 0) == 1;
    }
}
