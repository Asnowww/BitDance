package com.bitdance.badge;

import com.bitdance.badge.domain.BadgeDefinition;
import com.bitdance.badge.repository.BadgeDefinitionRepository;
import com.bitdance.badge.rule.BadgeRule;
import com.bitdance.badge.rule.CheckinStreakRule;
import com.bitdance.badge.rule.FirstReviewRule;
import com.bitdance.badge.rule.FirstWorkPublishedRule;
import com.bitdance.badge.service.BadgeRuleEngine;
import com.bitdance.growth.domain.GrowthBadge;
import com.bitdance.growth.repository.GrowthBadgeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BadgeRuleEngineTest {

    private BadgeDefinitionRepository defRepo;
    private GrowthBadgeRepository badgeRepo;
    private BadgeRuleEngine engine;

    @BeforeEach
    void setup() {
        defRepo = mock(BadgeDefinitionRepository.class);
        badgeRepo = mock(GrowthBadgeRepository.class);
        List<BadgeRule> rules = List.of(
            new CheckinStreakRule(),
            new FirstReviewRule(),
            new FirstWorkPublishedRule()
        );
        engine = new BadgeRuleEngine(defRepo, badgeRepo, rules, new ObjectMapper());
    }

    private BadgeDefinition def(long id, String code, String ruleType, String config) {
        BadgeDefinition d = new BadgeDefinition() {
            @Override public Long getId() { return id; }
            @Override public String getBadgeCode() { return code; }
            @Override public String getRuleType() { return ruleType; }
            @Override public String getRuleConfig() { return config; }
            @Override public String getStatus() { return "active"; }
        };
        return d;
    }

    @Test
    void checkinStreak_meetsThreshold_grants() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(1L, "streak_7", "checkin_streak", "{\"days\":7}")
        ));
        when(badgeRepo.existsByUserIdAndBadgeId(42L, 1L)).thenReturn(false);

        List<Long> granted = engine.evaluate(42L, "checkin", Map.of("streak", 8), null, null);

        assertThat(granted).containsExactly(1L);
        ArgumentCaptor<GrowthBadge> captor = ArgumentCaptor.forClass(GrowthBadge.class);
        verify(badgeRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(42L);
        assertThat(captor.getValue().getBadgeId()).isEqualTo(1L);
    }

    @Test
    void checkinStreak_belowThreshold_doesNotGrant() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(1L, "streak_7", "checkin_streak", "{\"days\":7}")
        ));
        List<Long> granted = engine.evaluate(42L, "checkin", Map.of("streak", 3), null, null);
        assertThat(granted).isEmpty();
        verify(badgeRepo, never()).save(any());
    }

    @Test
    void checkinStreak_alreadyGranted_skips() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(1L, "streak_7", "checkin_streak", "{\"days\":7}")
        ));
        when(badgeRepo.existsByUserIdAndBadgeId(42L, 1L)).thenReturn(true);
        List<Long> granted = engine.evaluate(42L, "checkin", Map.of("streak", 8), null, null);
        assertThat(granted).isEmpty();
        verify(badgeRepo, never()).save(any());
    }

    @Test
    void firstReview_totalOne_grants() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(2L, "first_review", "first_review", null)
        ));
        when(badgeRepo.existsByUserIdAndBadgeId(42L, 2L)).thenReturn(false);
        List<Long> granted = engine.evaluate(42L, "review", Map.of("totalCount", 1), null, null);
        assertThat(granted).containsExactly(2L);
    }

    @Test
    void firstReview_totalMoreThanOne_doesNotGrant() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(2L, "first_review", "first_review", null)
        ));
        List<Long> granted = engine.evaluate(42L, "review", Map.of("totalCount", 5), null, null);
        assertThat(granted).isEmpty();
    }

    @Test
    void firstWorkPublished_grants() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(3L, "first_work", "first_work_published", null)
        ));
        when(badgeRepo.existsByUserIdAndBadgeId(42L, 3L)).thenReturn(false);
        List<Long> granted = engine.evaluate(42L, "work_published", Map.of("totalCount", 1), null, null);
        assertThat(granted).containsExactly(3L);
    }

    @Test
    void unmatchedEventType_doesNotGrant() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(1L, "streak_7", "checkin_streak", "{\"days\":7}"),
            def(2L, "first_review", "first_review", null)
        ));
        List<Long> granted = engine.evaluate(42L, "random_event", Map.of(), null, null);
        assertThat(granted).isEmpty();
        verify(badgeRepo, never()).save(any());
    }

    @Test
    void multipleBadges_inSingleEvent() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(1L, "streak_3", "checkin_streak", "{\"days\":3}"),
            def(2L, "streak_7", "checkin_streak", "{\"days\":7}")
        ));
        when(badgeRepo.existsByUserIdAndBadgeId(eq(42L), any())).thenReturn(false);
        List<Long> granted = engine.evaluate(42L, "checkin", Map.of("streak", 8), null, null);
        assertThat(granted).containsExactly(1L, 2L);
        verify(badgeRepo, times(2)).save(any());
    }

    @Test
    void unknownRuleType_skipsGracefully() {
        when(defRepo.findByStatusOrderByIdAsc("active")).thenReturn(List.of(
            def(99L, "weird", "non_existent_rule", null)
        ));
        List<Long> granted = engine.evaluate(42L, "checkin", Map.of(), null, null);
        assertThat(granted).isEmpty();
    }

    @Test
    void engine_failureToLoadDefs_returnsEmpty() {
        when(defRepo.findByStatusOrderByIdAsc("active"))
            .thenThrow(new RuntimeException("db down"));
        List<Long> granted = engine.evaluate(42L, "checkin", Map.of("streak", 8), null, null);
        assertThat(granted).isEmpty();
    }
}
