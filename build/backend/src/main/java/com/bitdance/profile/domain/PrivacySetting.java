package com.bitdance.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "privacy_setting")
public class PrivacySetting {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "profile_visibility", nullable = false, length = 16)
    private String profileVisibility = "public";

    @Column(name = "growth_visibility", nullable = false, length = 16)
    private String growthVisibility = "followers";

    @Column(name = "practice_visibility", nullable = false, length = 16)
    private String practiceVisibility = "public";

    @Column(name = "content_visibility", nullable = false, length = 16)
    private String contentVisibility = "public";

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(String v) { this.profileVisibility = v; }
    public String getGrowthVisibility() { return growthVisibility; }
    public void setGrowthVisibility(String v) { this.growthVisibility = v; }
    public String getPracticeVisibility() { return practiceVisibility; }
    public void setPracticeVisibility(String v) { this.practiceVisibility = v; }
    public String getContentVisibility() { return contentVisibility; }
    public void setContentVisibility(String v) { this.contentVisibility = v; }
}
