package com.archiservice.review.plan.domain;

import com.archiservice.common.TimeStamp;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.NotMyReviewException;
import com.archiservice.product.plan.domain.Plan;
import com.archiservice.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanReview extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_review_id")
    private Long planReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content")
    private String content;

    @Column(name = "is_moderated", nullable = false)
    private Boolean isModerated = false;

    @Builder
    public PlanReview(User user, Plan plan, Integer score, String content) {
        this.user = user;
        this.plan = plan;
        this.score = score;
        this.content = content;
    }

    public void updateReview(Integer score, String content) {
        this.score = score;
        this.content = content;
        this.isModerated=false;
    }

    public void validateOwner(Long userId) {
        if (!this.user.getUserId().equals(userId)) {
            throw new NotMyReviewException(ErrorCode.NOT_MY_REVIEW.getMessage());
        }
    }

    public void markAsModerated() {
        this.isModerated = true;
    }
}
