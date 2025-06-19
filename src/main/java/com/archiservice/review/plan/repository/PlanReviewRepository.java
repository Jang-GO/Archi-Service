package com.archiservice.review.plan.repository;

import com.archiservice.product.plan.domain.Plan;
import com.archiservice.review.coupon.domain.CouponReview;
import com.archiservice.review.plan.domain.PlanReview;
import com.archiservice.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanReviewRepository extends JpaRepository<PlanReview, Long> {

    @Query("SELECT pr FROM PlanReview pr JOIN FETCH pr.user WHERE pr.plan.planId = :planId ORDER BY pr.createdAt DESC")
    Page<PlanReview> findByPlanIdWithUser(@Param("planId") Long planId, Pageable pageable);

    boolean existsByUserAndPlan(User user, Plan plan);

    List<PlanReview> findByIsModeratedFalse();

    @Query("SELECT AVG(r.score) FROM PlanReview r WHERE r.plan IS NOT NULL")
    Double findAverageRatingByPlanIsNotNull();

    @Query("SELECT pr.plan.planId, AVG(pr.score), COUNT(pr.score) FROM PlanReview pr WHERE pr.score IS NOT NULL GROUP BY pr.plan.planId")
    List<Object[]> findAverageScoreAndCountByPlan();

    @Query(value = "SELECT AVG(review_count) FROM (" +
            "SELECT COUNT(*) as review_count FROM plan_reviews GROUP BY plan_id" +
            ") as plan_review_counts",
            nativeQuery = true)
    Double findAverageReviewCountPerPlanNative();

    @Query("SELECT p.planId, COUNT(pr) FROM PlanReview pr JOIN pr.plan p " +
            "WHERE pr.isModerated = true GROUP BY p.planId HAVING COUNT(pr) >= 5")
    List<Object[]> findReviewGroupsByPlan();

    @Query("SELECT pr.content FROM PlanReview pr WHERE pr.plan.planId = :planId " +
            "AND pr.isModerated = true AND pr.score BETWEEN :minScore AND :maxScore")
    List<String> findContentsByPlanIdAndScoreRange(@Param("planId") Long planId,
                                                   @Param("minScore") Integer minScore,
                                                   @Param("maxScore") Integer maxScore);

    @Query("SELECT pr.content FROM PlanReview pr WHERE pr.plan.planId = :planId " +
            "AND pr.isModerated = true AND pr.score = :score")
    List<String> findContentsByPlanIdAndScore(@Param("planId") Long planId,
                                              @Param("score") Integer score);
}
