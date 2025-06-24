package com.archiservice.review.vas.domain;

import com.archiservice.common.TimeStamp;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.NotMyReviewException;
import com.archiservice.product.plan.domain.Plan;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vas_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VasReview extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vas_review_id")
    private Long vasReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vas_id", nullable = false)
    private Vas vas;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content")
    private String content;

    @Column(name = "is_moderated", nullable = false)
    private Boolean isModerated = false;

    @Builder
    public VasReview(User user, Vas vas, Integer score, String content, boolean isModerated) {
        this.user = user;
        this.vas = vas;
        this.score = score;
        this.content = content;
        this.isModerated = isModerated;
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

}
