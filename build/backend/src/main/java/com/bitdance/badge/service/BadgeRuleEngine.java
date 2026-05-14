package com.bitdance.badge.service;

import com.bitdance.badge.domain.BadgeDefinition;
import com.bitdance.badge.repository.BadgeDefinitionRepository;
import com.bitdance.badge.rule.BadgeRule;
import com.bitdance.growth.domain.GrowthBadge;
import com.bitdance.growth.repository.GrowthBadgeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 徽章发放引擎。
 *
 * 入参：userId + eventType（如 "checkin"/"review"/"work_published"/"workshop_attended"/"practice_completed"）
 *      + metadata（事件相关数据，如 {"streak": 7} 或 {"totalCount": 1}）
 *      + 可选 sourceType/sourceRefId（写入 growth_badge 记录来源）
 *
 * 流程：
 *  1. 拉所有 status='active' 的 BadgeDefinition
 *  2. 按 ruleType 找对应 BadgeRule（Spring 注入的全部 BadgeRule 按 type() 索引）
 *  3. 解析 rule_config jsonb 字符串为 Map（null 容错）
 *  4. 调 rule.shouldGrant 判定
 *  5. 命中且 growth_badge 未授予则 grant（写表 + uk 防重复）
 *
 * 失败降级：单条徽章判定/写入失败仅 log.warn，不影响业务主流程。
 */
@Service
public class BadgeRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(BadgeRuleEngine.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final BadgeDefinitionRepository defRepo;
    private final GrowthBadgeRepository badgeRepo;
    private final Map<String, BadgeRule> rulesByType;
    private final ObjectMapper objectMapper;

    public BadgeRuleEngine(
        BadgeDefinitionRepository defRepo,
        GrowthBadgeRepository badgeRepo,
        List<BadgeRule> rules,
        ObjectMapper objectMapper
    ) {
        this.defRepo = defRepo;
        this.badgeRepo = badgeRepo;
        this.rulesByType = new HashMap<>();
        for (BadgeRule r : rules) rulesByType.put(r.type(), r);
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<Long> evaluate(Long userId, String eventType, Map<String, Object> metadata,
                                String sourceType, Long sourceRefId) {
        if (userId == null || eventType == null) return List.of();
        List<Long> granted = new ArrayList<>();
        List<BadgeDefinition> defs;
        try {
            defs = defRepo.findByStatusOrderByIdAsc("active");
        } catch (RuntimeException ex) {
            log.warn("BadgeRuleEngine: cannot load active definitions", ex);
            return List.of();
        }
        for (BadgeDefinition def : defs) {
            try {
                BadgeRule rule = rulesByType.get(def.getRuleType());
                if (rule == null) continue; // 未注册的规则类型跳过
                Map<String, Object> config = parseConfig(def.getRuleConfig());
                if (!rule.shouldGrant(eventType, metadata, config)) continue;
                if (badgeRepo.existsByUserIdAndBadgeId(userId, def.getId())) continue;
                GrowthBadge b = new GrowthBadge();
                b.setUserId(userId);
                b.setBadgeId(def.getId());
                b.setSourceType(sourceType);
                b.setSourceRefId(sourceRefId);
                b.setAwardedAt(OffsetDateTime.now());
                badgeRepo.save(b);
                granted.add(def.getId());
            } catch (RuntimeException ex) {
                log.warn("BadgeRuleEngine: failed evaluating badge={} for user={}",
                    def.getBadgeCode(), userId, ex);
            }
        }
        return granted;
    }

    public List<Long> evaluate(Long userId, String eventType, Map<String, Object> metadata) {
        return evaluate(userId, eventType, metadata, null, null);
    }

    private Map<String, Object> parseConfig(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (Exception ex) {
            log.debug("BadgeRuleEngine: cannot parse rule_config={}", raw);
            return Map.of();
        }
    }
}
