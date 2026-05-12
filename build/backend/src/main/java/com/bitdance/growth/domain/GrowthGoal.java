package com.bitdance.growth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "growth_goal")
public class GrowthGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_period", nullable = false, length = 16)
    private String goalPeriod;

    @Column(name = "target_minutes", nullable = false)
    private Integer targetMinutes = 0;

    @Column(name = "target_times", nullable = false)
    private Integer targetTimes = 0;

    @Column(name = "current_minutes", nullable = false)
    private Integer currentMinutes = 0;

    @Column(name = "current_times", nullable = false)
    private Integer currentTimes = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "goal_status", nullable = false, length = 16)
    private String goalStatus = "active";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getGoalPeriod() { return goalPeriod; }
    public void setGoalPeriod(String v) { this.goalPeriod = v; }
    public Integer getTargetMinutes() { return targetMinutes; }
    public void setTargetMinutes(Integer v) { this.targetMinutes = v; }
    public Integer getTargetTimes() { return targetTimes; }
    public void setTargetTimes(Integer v) { this.targetTimes = v; }
    public Integer getCurrentMinutes() { return currentMinutes; }
    public void setCurrentMinutes(Integer v) { this.currentMinutes = v; }
    public Integer getCurrentTimes() { return currentTimes; }
    public void setCurrentTimes(Integer v) { this.currentTimes = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { this.startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { this.endDate = v; }
    public String getGoalStatus() { return goalStatus; }
    public void setGoalStatus(String v) { this.goalStatus = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
