package com.bitdance.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "coach_dance_style")
@IdClass(CoachDanceStyle.PK.class)
public class CoachDanceStyle {

    @Id
    @Column(name = "coach_id")
    private Long coachId;

    @Id
    @Column(name = "dance_style_id")
    private Long danceStyleId;

    @Column(name = "proficiency_level", length = 32)
    private String proficiencyLevel;

    public Long getCoachId() { return coachId; }
    public Long getDanceStyleId() { return danceStyleId; }
    public String getProficiencyLevel() { return proficiencyLevel; }

    public static class PK implements Serializable {
        private Long coachId;
        private Long danceStyleId;
        public PK() {}
        public PK(Long coachId, Long danceStyleId) {
            this.coachId = coachId; this.danceStyleId = danceStyleId;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(coachId, pk.coachId) && Objects.equals(danceStyleId, pk.danceStyleId);
        }
        @Override public int hashCode() { return Objects.hash(coachId, danceStyleId); }
    }
}
