package com.archiservice.review.ai.service;

import com.archiservice.review.ai.dto.ModerationResult;
import com.archiservice.review.ai.dto.ReviewAnalysisResult;
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

    public ModerationResult moderateAllReviews() {
        log.info("=== 전체 리뷰 자동 삭제 작업 시작 ===");

        long startTime = System.currentTimeMillis();

        ModerationResult planResult = moderatePlanReviews();
        ModerationResult vasResult = moderateVasReviews();
        ModerationResult couponResult = moderateCouponReviews();

        ModerationResult totalResult = combineModerationResults(
                planResult, vasResult, couponResult, startTime);

        log.info("=== 전체 리뷰 자동 삭제 작업 완료 ===");
        log.info("총 처리된 리뷰: {}, 삭제된 리뷰: {}, 소요시간: {}ms",
                totalResult.getTotalProcessed(),
                totalResult.getDeletedCount(),
                totalResult.getProcessingTimeMs());

        return totalResult;
    }

    public ModerationResult moderatePlanReviews() {
        log.info("=== 요금제 리뷰 모더레이션 시작 ===");

        List<PlanReview> allReviews = planReviewRepository.findAll();
        log.info("분석 대상 요금제 리뷰 수: {}", allReviews.size());

        if (allReviews.isEmpty()) {
            return createEmptyResult("PLAN");
        }

        return processAndDeleteReviews(allReviews, "PLAN");
    }

    public ModerationResult moderateVasReviews() {
        log.info("=== 부가서비스 리뷰 모더레이션 시작 ===");

        List<VasReview> allReviews = vasReviewRepository.findAll();
        log.info("분석 대상 부가서비스 리뷰 수: {}", allReviews.size());

        if (allReviews.isEmpty()) {
            return createEmptyResult("VAS");
        }

        return processAndDeleteReviews(allReviews, "VAS");
    }

    public ModerationResult moderateCouponReviews() {
        log.info("=== 쿠폰 리뷰 모더레이션 시작 ===");

        List<CouponReview> allReviews = couponReviewRepository.findAll();
        log.info("분석 대상 쿠폰 리뷰 수: {}", allReviews.size());

        if (allReviews.isEmpty()) {
            return createEmptyResult("COUPON");
        }

        return processAndDeleteReviews(allReviews, "COUPON");
    }

    private <T> ModerationResult processAndDeleteReviews(List<T> reviews, String reviewType) {
        long startTime = System.currentTimeMillis();

        List<String> reviewContents = reviews.stream()
                .map(this::extractContent)
                .toList();

        List<ReviewAnalysisResult> analysisResults =
                analysisService.analyzeBatchReviews(reviewContents);

        int deletedCount = deleteViolatingReviews(reviews, analysisResults, reviewType);

        long processingTime = System.currentTimeMillis() - startTime;

        return ModerationResult.builder()
                .totalProcessed(reviews.size())
                .deletedCount(deletedCount)
                .errorCount(0)
                .processingTimeMs(processingTime)
                .build();
    }

    private <T> int deleteViolatingReviews(
            List<T> reviews,
            List<ReviewAnalysisResult> results,
            String reviewType) {

        int deletedCount = 0;
        List<T> reviewsToDelete = new ArrayList<>();

        for (int i = 0; i < Math.min(reviews.size(), results.size()); i++) {
            T review = reviews.get(i);
            ReviewAnalysisResult result = results.get(i);

            if (result.isShouldDelete()) {
                reviewsToDelete.add(review);
                deletedCount++;

                log.info("{} 리뷰 삭제 예정 - ID: {}, 사유: {}, 확신도: {:.2f}",
                        reviewType, extractId(review), result.getReason(), result.getConfidenceScore());
            } else {
                log.debug("{} 리뷰 유지 - ID: {}, 확신도: {:.2f}",
                        reviewType, extractId(review), result.getConfidenceScore());
            }
        }

        // 실제 삭제 실행
        if (!reviewsToDelete.isEmpty()) {
            deleteReviewsByType(reviewsToDelete, reviewType);
            log.info("{} 타입 리뷰 {}개 삭제 완료", reviewType, reviewsToDelete.size());
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

    private ModerationResult combineModerationResults(
            ModerationResult planResult,
            ModerationResult vasResult,
            ModerationResult couponResult,
            long startTime) {

        long totalProcessingTime = System.currentTimeMillis() - startTime;

        return ModerationResult.builder()
                .totalProcessed(planResult.getTotalProcessed() +
                        vasResult.getTotalProcessed() +
                        couponResult.getTotalProcessed())
                .deletedCount(planResult.getDeletedCount() +
                        vasResult.getDeletedCount() +
                        couponResult.getDeletedCount())
                .errorCount(planResult.getErrorCount() +
                        vasResult.getErrorCount() +
                        couponResult.getErrorCount())
                .processingTimeMs(totalProcessingTime)
                .build();
    }

    private ModerationResult createEmptyResult(String reviewType) {
        log.info("분석할 {}타입 리뷰가 없습니다.", reviewType);
        return ModerationResult.builder()
                .totalProcessed(0)
                .deletedCount(0)
                .errorCount(0)
                .processingTimeMs(0L)
                .build();
    }
}
