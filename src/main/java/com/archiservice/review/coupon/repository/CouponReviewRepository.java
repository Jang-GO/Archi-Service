package com.archiservice.review.coupon.repository;

import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.review.coupon.domain.CouponReview;
import com.archiservice.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponReviewRepository extends JpaRepository<CouponReview, Long> {
    @Query("SELECT cr FROM CouponReview cr JOIN FETCH cr.user WHERE cr.coupon.couponId = :couponId ORDER BY cr.createdAt DESC")
    Page<CouponReview> findByCouponIdWithUser(@Param("couponId") Long couponId, Pageable pageable);

    List<CouponReview> findByIsModeratedFalse();

    boolean existsByUserAndCoupon(User user, Coupon coupon);

    int countCouponReviewByCoupon(Coupon coupon);

    @Query("SELECT AVG(r.score) FROM CouponReview r WHERE r.coupon = :coupon")
    Double getAverageRatingByCoupon(@Param("coupon") Coupon coupon);

    @Query("SELECT AVG(r.score) FROM CouponReview r WHERE r.coupon IS NOT NULL")
    Double findAverageRatingByCouponIsNotNull();

    @Query("SELECT cr.coupon.couponId, AVG(cr.score), COUNT(cr.score) FROM CouponReview cr WHERE cr.score IS NOT NULL GROUP BY cr.coupon.couponId")
    List<Object[]> findAverageScoreAndCountByCoupon();

    @Query(value = "SELECT AVG(review_count) FROM (" +
            "SELECT COUNT(*) as review_count FROM coupon_reviews GROUP BY coupon_id" +
            ") as coupon_review_counts",
            nativeQuery = true)
    Double findAverageReviewCountPerCouponNative();

}
