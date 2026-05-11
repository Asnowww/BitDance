package com.bitdance.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_dimension_score")
public class ReviewDimensionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "dimension_code", nullable = false, length = 64)
    private String dimensionCode;

    @Column(name = "dimension_name", nullable = false, length = 100)
    private String dimensionName;

    @Column(name = "score", nullable = false)
    private Short score;

    public Long getId() { return id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long v) { this.reviewId = v; }
    public String getDimensionCode() { return dimensionCode; }
    public void setDimensionCode(String v) { this.dimensionCode = v; }
    public String getDimensionName() { return dimensionName; }
    public void setDimensionName(String v) { this.dimensionName = v; }
    public Short getScore() { return score; }
    public void setScore(Short v) { this.score = v; }
}
