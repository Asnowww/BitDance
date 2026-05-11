package com.bitdance.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "avatar_asset_id")
    private Long avatarAssetId;

    @Column(name = "gender", nullable = false, length = 8)
    private String gender = "unknown";

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "current_level", length = 32)
    private String currentLevel;

    @Column(name = "learning_goal", columnDefinition = "text")
    private String learningGoal;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Long getAvatarAssetId() { return avatarAssetId; }
    public void setAvatarAssetId(Long id) { this.avatarAssetId = id; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String v) { this.currentLevel = v; }
    public String getLearningGoal() { return learningGoal; }
    public void setLearningGoal(String v) { this.learningGoal = v; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }
}
