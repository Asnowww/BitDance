package com.bitdance.practice.repository;

import com.bitdance.practice.domain.GroupClassIntentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupClassIntentParticipantRepository extends JpaRepository<GroupClassIntentParticipant, Long> {
    Optional<GroupClassIntentParticipant> findByIntentIdAndUserId(Long intentId, Long userId);
    List<GroupClassIntentParticipant> findByIntentIdAndParticipantStatus(Long intentId, String participantStatus);
    boolean existsByIntentIdAndUserIdAndParticipantStatus(Long intentId, Long userId, String participantStatus);
}
