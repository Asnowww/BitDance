package com.bitdance.profile.repository;

import com.bitdance.profile.domain.PrivacySetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacySettingRepository extends JpaRepository<PrivacySetting, Long> {
}
