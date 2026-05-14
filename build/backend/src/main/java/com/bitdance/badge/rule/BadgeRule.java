package com.bitdance.badge.rule;

import java.util.Map;

/**
 * 徽章发放规则接口。
 *
 * 每条规则只做纯函数判定：基于事件类型 + 当前事件 metadata + 规则 config 决定是否授予。
 * 不查 repo，避免规则之间互相耦合且方便单测。
 * 触发点 service 负责把判定所需的 metadata（如 streak、totalCount）算好传进来。
 */
public interface BadgeRule {

    /** 与 badge_definition.rule_type 字段值对齐。 */
    String type();

    /** 判定是否应授予徽章。 */
    boolean shouldGrant(String eventType, Map<String, Object> metadata, Map<String, Object> config);
}
