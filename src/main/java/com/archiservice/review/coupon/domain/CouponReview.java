package com.archiservice.review.coupon.domain;

import com.archiservice.common.TimeStamp;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.NotMyReviewException;
import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponReview extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_review_id")
    private Long couponReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content")
    private String content;

    @Column(name = "is_moderated", nullable = false)
    private Boolean isModerated = false;

    @Builder
    public CouponReview(User user, Coupon coupon, Integer score, String content) {
        this.user = user;
        this.coupon = coupon;
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

}

