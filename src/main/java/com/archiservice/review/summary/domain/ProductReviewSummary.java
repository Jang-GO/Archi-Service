package com.archiservice.review.summary.domain;

import com.archiservice.common.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "product_review_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReviewSummary extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "review_type", nullable = false, length = 16)
    private String reviewType;

    @Column(name = "total_review_count", nullable = false)
    private Integer totalReviewCount;

    @Column(name = "average_score", nullable = false)
    private Double averageScore;

    @Column(name = "high_rating_summary", columnDefinition = "TEXT")
    private String highRatingSummary;

    @Column(name = "high_rating_count", nullable = false)
    private Integer highRatingCount;

    @Column(name = "low_rating_summary", columnDefinition = "TEXT")
    private String lowRatingSummary;

    @Column(name = "low_rating_count", nullable = false)
    private Integer lowRatingCount;

    @Column(name = "medium_rating_count", nullable = false)
    private Integer mediumRatingCount;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Builder
    public ProductReviewSummary(Long productId, String reviewType, Integer totalReviewCount, Double averageScore,
                               String highRatingSummary, Integer highRatingCount,
                               String lowRatingSummary, Integer lowRatingCount, Integer mediumRatingCount) {
        this.productId = productId;
        this.reviewType = reviewType;
        this.totalReviewCount = totalReviewCount;
        this.averageScore = averageScore;
        this.highRatingSummary = highRatingSummary;
        this.highRatingCount = highRatingCount;
        this.lowRatingSummary = lowRatingSummary;
        this.lowRatingCount = lowRatingCount;
        this.mediumRatingCount = mediumRatingCount;
        this.summaryDate = LocalDate.now();
    }

    public void updateSummary(Integer totalReviewCount, Double averageScore,
                              String highRatingSummary, Integer highRatingCount,
                              String lowRatingSummary, Integer lowRatingCount,
                              Integer mediumRatingCount) {
        this.totalReviewCount = totalReviewCount;
        this.averageScore = averageScore;
        this.highRatingSummary = highRatingSummary;
        this.highRatingCount = highRatingCount;
        this.lowRatingSummary = lowRatingSummary;
        this.lowRatingCount = lowRatingCount;
        this.mediumRatingCount = mediumRatingCount;
        this.summaryDate = LocalDate.now(); // 업데이트 날짜 갱신
    }
}
