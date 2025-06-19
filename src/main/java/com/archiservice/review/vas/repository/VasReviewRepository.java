package com.archiservice.review.vas.repository;

import com.archiservice.review.coupon.domain.CouponReview;
import com.archiservice.review.vas.domain.VasReview;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VasReviewRepository extends JpaRepository<VasReview, Long> {

    @Query("SELECT vr FROM VasReview vr JOIN FETCH vr.user WHERE vr.vas.vasId = :vasId ORDER BY vr.createdAt DESC")
    Page<VasReview> findByVasIdWithUser(@Param("vasId") Long vasId, Pageable pageable);

    boolean existsByUserAndVas(User user, Vas vas);

    List<VasReview> findByIsModeratedFalse();

    @Query("SELECT AVG(r.score) FROM VasReview r WHERE r.vas IS NOT NULL")
    Double findAverageRatingByVasIsNotNull();

    @Query("SELECT vr.vas.vasId, AVG(vr.score), COUNT(vr.score) FROM VasReview vr WHERE vr.score IS NOT NULL GROUP BY vr.vas.vasId")
    List<Object[]> findAverageScoreAndCountByVas();

    @Query(value = "SELECT AVG(review_count) FROM (" +
            "SELECT COUNT(*) as review_count FROM vas_reviews GROUP BY vas_id" +
            ") as vas_review_counts",
            nativeQuery = true)
    Double findAverageReviewCountPerVasNative();

    @Query("SELECT v.vasId, COUNT(vr) FROM VasReview vr JOIN vr.vas v " +
            "WHERE vr.isModerated = true GROUP BY v.vasId HAVING COUNT(vr) >= 5")
    List<Object[]> findReviewGroupsByVas();

    @Query("SELECT vr.content FROM VasReview vr WHERE vr.vas.vasId = :vasId " +
            "AND vr.isModerated = true AND vr.score BETWEEN :minScore AND :maxScore")
    List<String> findContentsByVasIdAndScoreRange(@Param("vasId") Long vasId,
                                                  @Param("minScore") Integer minScore,
                                                  @Param("maxScore") Integer maxScore);

    @Query("SELECT vr.content FROM VasReview vr WHERE vr.vas.vasId = :vasId " +
            "AND vr.isModerated = true AND vr.score = :score")
    List<String> findContentsByVasIdAndScore(@Param("vasId") Long vasId,
                                             @Param("score") Integer score);
}

