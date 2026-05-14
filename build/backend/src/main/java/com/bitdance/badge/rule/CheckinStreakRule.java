package com.bitdance.badge.rule;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CheckinStreakRule implements BadgeRule {

    @Override
    public String type() { return "checkin_streak"; }

    @Override
    public boolean shouldGrant(String eventType, Map<String, Object> metadata, Map<String, Object> config) {
        if (!"checkin".equals(eventType)) return false;
        int required = readInt(config, "days", 7);
        int actual = readInt(metadata, "streak", 0);
        return actual >= required;
    }

    static int readInt(Map<String, Object> map, String key, int dflt) {
        if (map == null) return dflt;
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) { /* fall */ }
        }
        return dflt;
    }
}
