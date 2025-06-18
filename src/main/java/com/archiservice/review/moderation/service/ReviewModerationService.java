package com.archiservice.review.moderation.service;

import com.archiservice.review.moderation.dto.ModerationResult;
import com.archiservice.review.moderation.dto.ReviewAnalysisResult;
import com.archiservice.review.coupon.domain.CouponReview;
import com.archiservice.review.coupon.repository.CouponReviewRepository;
import com.archiservice.review.plan.domain.PlanReview;
import com.archiservice.review.plan.repository.PlanReviewRepository;
import com.archiservice.review.vas.domain.VasReview;
import com.archiservice.review.vas.repository.VasReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewModerationService {

    private final PlanReviewRepository planReviewRepository;
    private final VasReviewRepository vasReviewRepository;
    private final CouponReviewRepository couponReviewRepository;
    private final ReviewAnalysisService analysisService;

    public ModerationResult moderateUnmoderatedPlanReviews() {
        log.info("=== 미모더레이션 요금제 리뷰 처리 시작 ===");

        List<PlanReview> unmoderatedReviews = planReviewRepository.findByIsModeratedFalse();
        log.info("모더레이션 대상 요금제 리뷰 수: {}", unmoderatedReviews.size());

        if (unmoderatedReviews.isEmpty()) {
            return createEmptyResult("PLAN");
        }

        return processAndDeleteReviews(unmoderatedReviews, "PLAN");
    }

    public ModerationResult moderateUnmoderatedVasReviews() {
        log.info("=== 미모더레이션 부가서비스 리뷰 처리 시작 ===");

        List<VasReview> unmoderatedReviews = vasReviewRepository.findByIsModeratedFalse();
        log.info("모더레이션 대상 부가서비스 리뷰 수: {}", unmoderatedReviews.size());

        if (unmoderatedReviews.isEmpty()) {
            return createEmptyResult("VAS");
        }

        return processAndDeleteReviews(unmoderatedReviews, "VAS");
    }

    public ModerationResult moderateUnmoderatedCouponReviews() {
        log.info("=== 미모더레이션 쿠폰 리뷰 처리 시작 ===");

        List<CouponReview> unmoderatedReviews = couponReviewRepository.findByIsModeratedFalse();
        log.info("모더레이션 대상 쿠폰 리뷰 수: {}", unmoderatedReviews.size());

        if (unmoderatedReviews.isEmpty()) {
            return createEmptyResult("COUPON");
        }

        return processAndDeleteReviews(unmoderatedReviews, "COUPON");
    }

    private <T> ModerationResult processAndDeleteReviews(List<T> reviews, String reviewType) {
        long startTime = System.currentTimeMillis();

        List<String> reviewContents = reviews.stream()
                .map(this::extractContent)
                .toList();

        List<ReviewAnalysisResult> analysisResults =
                analysisService.analyzeBatchReviews(reviewContents);

        int deletedCount = processReviewsWithModerationStatus(reviews, analysisResults, reviewType);

        long processingTime = System.currentTimeMillis() - startTime;

        return ModerationResult.builder()
                .totalProcessed(reviews.size())
                .deletedCount(deletedCount)
                .errorCount(0)
                .processingTimeMs(processingTime)
                .reviewType(reviewType)
                .build();
    }

    private <T> int processReviewsWithModerationStatus(
            List<T> reviews,
            List<ReviewAnalysisResult> results,
            String reviewType) {

        int deletedCount = 0;
        List<T> reviewsToDelete = new ArrayList<>();
        List<T> reviewsToKeep = new ArrayList<>();

        for (int i = 0; i < Math.min(reviews.size(), results.size()); i++) {
            T review = reviews.get(i);
            ReviewAnalysisResult result = results.get(i);

            if (result.isShouldDelete()) {
                reviewsToDelete.add(review);
                deletedCount++;

                log.info("{} 리뷰 삭제 예정 - ID: {}, 사유: {}, 확신도: {:.2f}",
                        reviewType, extractId(review), result.getReason(), result.getConfidenceScore());
            } else {
                reviewsToKeep.add(review);
                log.debug("{} 리뷰 유지 - ID: {}, 확신도: {:.2f}",
                        reviewType, extractId(review), result.getConfidenceScore());
            }
        }

        if (!reviewsToDelete.isEmpty()) {
            deleteReviewsByType(reviewsToDelete, reviewType);
            log.info("{} 타입 리뷰 {}개 삭제 완료", reviewType, reviewsToDelete.size());
        }

        if (!reviewsToKeep.isEmpty()) {
            markReviewsAsModerated(reviewsToKeep, reviewType);
            log.info("{} 타입 리뷰 {}개 모더레이션 완료", reviewType, reviewsToKeep.size());
        }

        return deletedCount;
    }

    private <T> void deleteReviewsByType(List<T> reviewsToDelete, String reviewType) {
        switch (reviewType) {
            case "PLAN" -> {
                List<PlanReview> planReviews = (List<PlanReview>) reviewsToDelete;
                planReviewRepository.deleteAll(planReviews);
            }
            case "VAS" -> {
                List<VasReview> vasReviews = (List<VasReview>) reviewsToDelete;
                vasReviewRepository.deleteAll(vasReviews);
            }
            case "COUPON" -> {
                List<CouponReview> couponReviews = (List<CouponReview>) reviewsToDelete;
                couponReviewRepository.deleteAll(couponReviews);
            }
        }
    }

    private <T> void markReviewsAsModerated(List<T> reviews, String reviewType) {
        switch (reviewType) {
            case "PLAN" -> {
                List<PlanReview> planReviews = (List<PlanReview>) reviews;
                planReviews.forEach(PlanReview::markAsModerated);
                planReviewRepository.saveAll(planReviews);
            }
            case "VAS" -> {
                List<VasReview> vasReviews = (List<VasReview>) reviews;
                vasReviews.forEach(VasReview::markAsModerated);
                vasReviewRepository.saveAll(vasReviews);
            }
            case "COUPON" -> {
                List<CouponReview> couponReviews = (List<CouponReview>) reviews;
                couponReviews.forEach(CouponReview::markAsModerated);
                couponReviewRepository.saveAll(couponReviews);
            }
        }
    }

    private <T> String extractContent(T review) {
        if (review instanceof PlanReview planReview) {
            return planReview.getContent();
        } else if (review instanceof VasReview vasReview) {
            return vasReview.getContent();
        } else if (review instanceof CouponReview couponReview) {
            return couponReview.getContent();
        }
        throw new IllegalArgumentException("지원하지 않는 리뷰 타입입니다.");
    }

    private <T> Long extractId(T review) {
        if (review instanceof PlanReview planReview) {
            return planReview.getPlanReviewId();
        } else if (review instanceof VasReview vasReview) {
            return vasReview.getVasReviewId();
        } else if (review instanceof CouponReview couponReview) {
            return couponReview.getCouponReviewId();
        }
        throw new IllegalArgumentException("지원하지 않는 리뷰 타입입니다.");
    }

    private ModerationResult createEmptyResult(String reviewType) {
        log.info("분석할 {}타입 리뷰가 없습니다.", reviewType);
        return ModerationResult.builder()
                .totalProcessed(0)
                .deletedCount(0)
                .errorCount(0)
                .processingTimeMs(0L)
                .reviewType(reviewType)
                .build();
    }
}
