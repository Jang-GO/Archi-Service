package com.archiservice.review.summary.service;

import com.archiservice.review.coupon.repository.CouponReviewRepository;
import com.archiservice.review.plan.repository.PlanReviewRepository;
import com.archiservice.review.summary.domain.ProductReviewSummary;
import com.archiservice.review.summary.dto.RatingBasedReviews;
import com.archiservice.review.summary.dto.SimplifiedSummaryResult;
import com.archiservice.review.summary.dto.SummaryResult;
import com.archiservice.review.summary.repository.ProductReviewSummaryRepository;
import com.archiservice.review.vas.repository.VasReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class ProductReviewSummaryService {

    private final ChatClient summaryChatClient;
    private final PlanReviewRepository planReviewRepository;
    private final VasReviewRepository vasReviewRepository;
    private final CouponReviewRepository couponReviewRepository;
    private final ProductReviewSummaryRepository summaryRepository;

    public ProductReviewSummaryService(CouponReviewRepository couponReviewRepository,
                                       @Qualifier("reviewSummaryBot") ChatClient summaryChatClient,
                                       PlanReviewRepository planReviewRepository,
                                       VasReviewRepository vasReviewRepository,
                                       ProductReviewSummaryRepository summaryRepository) {
        this.couponReviewRepository = couponReviewRepository;
        this.summaryChatClient = summaryChatClient;
        this.planReviewRepository = planReviewRepository;
        this.vasReviewRepository = vasReviewRepository;
        this.summaryRepository = summaryRepository;
    }

    public SummaryResult summarizeAllProductReviews() {
        log.info("=== 전체 상품 리뷰 요약 작업 시작 ===");

        long startTime = System.currentTimeMillis();

        SummaryResult planResult = summarizePlanReviews();
        SummaryResult vasResult = summarizeVasReviews();
        SummaryResult couponResult = summarizeCouponReviews();

        long processingTime = System.currentTimeMillis() - startTime;

        SummaryResult totalResult = SummaryResult.builder()
                .totalProducts(planResult.getTotalProducts() +
                        vasResult.getTotalProducts() +
                        couponResult.getTotalProducts())
                .summarizedProducts(planResult.getSummarizedProducts() +
                        vasResult.getSummarizedProducts() +
                        couponResult.getSummarizedProducts())
                .processingTimeMs(processingTime)
                .build();

        log.info("=== 전체 상품 리뷰 요약 작업 완료 ===");
        log.info("총 상품: {}, 요약 완료: {}, 소요시간: {}ms",
                totalResult.getTotalProducts(),
                totalResult.getSummarizedProducts(),
                totalResult.getProcessingTimeMs());

        return totalResult;
    }

    private SummaryResult summarizePlanReviews() {
        log.info("=== 요금제 리뷰 요약 시작 ===");

        List<Object[]> planReviewGroups = planReviewRepository.findReviewGroupsByPlan();
        return processReviewGroups(planReviewGroups, "PLAN");
    }

    private SummaryResult summarizeVasReviews() {
        log.info("=== 부가서비스 리뷰 요약 시작 ===");

        List<Object[]> vasReviewGroups = vasReviewRepository.findReviewGroupsByVas();
        return processReviewGroups(vasReviewGroups, "VAS");
    }

    private SummaryResult summarizeCouponReviews() {
        log.info("=== 쿠폰 리뷰 요약 시작 ===");

        List<Object[]> couponReviewGroups = couponReviewRepository.findReviewGroupsByCoupon();
        return processReviewGroups(couponReviewGroups, "COUPON");
    }

    private SummaryResult processReviewGroups(List<Object[]> reviewGroups, String reviewType) {
        int summarizedCount = 0;

        for (Object[] group : reviewGroups) {
            Long productId = (Long) group[0];
            Long reviewCount = (Long) group[1];

            log.debug("{} 상품 ID {} 처리 시작 (리뷰 {}개)", reviewType, productId, reviewCount);

            RatingBasedReviews ratingReviews = getReviewsByRating(productId, reviewType);

            if (ratingReviews.getTotalCount() >= 5) {
                SimplifiedSummaryResult summaryResult = generateSummary(ratingReviews, reviewType);
                saveOrUpdateSummary(productId, reviewType, summaryResult, ratingReviews);
                summarizedCount++;

                log.info("{} 상품 ID {} 요약 완료", reviewType, productId);
            } else {
                log.debug("{} 상품 ID {} 리뷰 수 부족 ({}개)", reviewType, productId, ratingReviews.getTotalCount());
            }
        }

        return SummaryResult.builder()
                .totalProducts(reviewGroups.size())
                .summarizedProducts(summarizedCount)
                .productType(reviewType)
                .build();
    }

    private RatingBasedReviews getReviewsByRating(Long productId, String reviewType) {
        return switch (reviewType) {
            case "PLAN" -> {
                List<String> highRating = planReviewRepository.findContentsByPlanIdAndScoreRange(productId, 4, 5);
                List<String> lowRating = planReviewRepository.findContentsByPlanIdAndScoreRange(productId, 1, 2);
                List<String> mediumRating = planReviewRepository.findContentsByPlanIdAndScore(productId, 3);
                yield new RatingBasedReviews(highRating, lowRating, mediumRating);
            }
            case "VAS" -> {
                List<String> highRating = vasReviewRepository.findContentsByVasIdAndScoreRange(productId, 4, 5);
                List<String> lowRating = vasReviewRepository.findContentsByVasIdAndScoreRange(productId, 1, 2);
                List<String> mediumRating = vasReviewRepository.findContentsByVasIdAndScore(productId, 3);
                yield new RatingBasedReviews(highRating, lowRating, mediumRating);
            }
            case "COUPON" -> {
                List<String> highRating = couponReviewRepository.findContentsByCouponIdAndScoreRange(productId, 4, 5);
                List<String> lowRating = couponReviewRepository.findContentsByCouponIdAndScoreRange(productId, 1, 2);
                List<String> mediumRating = couponReviewRepository.findContentsByCouponIdAndScore(productId, 3);
                yield new RatingBasedReviews(highRating, lowRating, mediumRating);
            }
            default -> new RatingBasedReviews(List.of(), List.of(), List.of());
        };
    }

    private SimplifiedSummaryResult generateSummary(RatingBasedReviews ratingReviews, String reviewType) {
        String prompt = createSummaryPrompt(ratingReviews, reviewType);

        try {
            return summaryChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(SimplifiedSummaryResult.class);
        } catch (Exception e) {
            log.error("AI 요약 생성 실패: {}", e.getMessage());
            return SimplifiedSummaryResult.builder()
                    .highRatingSummary("요약 생성 실패")
                    .lowRatingSummary("요약 생성 실패")
                    .averageScore(calculateAverageScore(ratingReviews))
                    .build();
        }
    }

    private String createSummaryPrompt(RatingBasedReviews ratingReviews, String reviewType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("Analyze the following %s reviews categorized by ratings:\n\n",
                getReviewTypeName(reviewType)));

        if (!ratingReviews.getHighRatingReviews().isEmpty()) {
            prompt.append("=== High Rating Reviews (4-5 stars) ===\n");
            for (int i = 0; i < ratingReviews.getHighRatingReviews().size(); i++) {
                prompt.append(String.format("%d. %s\n", i + 1, ratingReviews.getHighRatingReviews().get(i)));
            }
            prompt.append("\n");
        }

        if (!ratingReviews.getLowRatingReviews().isEmpty()) {
            prompt.append("=== Low Rating Reviews (1-2 stars) ===\n");
            for (int i = 0; i < ratingReviews.getLowRatingReviews().size(); i++) {
                prompt.append(String.format("%d. %s\n", i + 1, ratingReviews.getLowRatingReviews().get(i)));
            }
            prompt.append("\n");
        }

        prompt.append("""
        Provide a summary in the following JSON format:
        {
            "highRatingSummary": "Summarize common positive aspects from high-rating reviews in 2-3 sentences",
            "lowRatingSummary": "Summarize common negative aspects from low-rating reviews in 2-3 sentences",
            "averageScore": Calculate overall satisfaction as a decimal between 1.0-5.0
        }
        
        Guidelines:
        - If no reviews exist for a rating category, write "No reviews available for this rating range"
        - Focus on specific service features rather than general emotions
        - Use natural sentences, not keywords or bullet points
        """);

        return prompt.toString();
    }

    private String getReviewTypeName(String reviewType) {
        return switch (reviewType) {
            case "PLAN" -> "통신 요금제";
            case "VAS" -> "부가서비스";
            case "COUPON" -> "쿠폰/할인 혜택";
            default -> "상품";
        };
    }

    private void saveOrUpdateSummary(Long productId, String reviewType,
                                     SimplifiedSummaryResult summaryResult,
                                     RatingBasedReviews ratingReviews) {
        Optional<ProductReviewSummary> existingSummary =
                summaryRepository.findByProductIdAndReviewType(productId, reviewType);

        double averageScore = summaryResult.getAverageScore() != null ?
                summaryResult.getAverageScore() : calculateAverageScore(ratingReviews);

        if (existingSummary.isPresent()) {
            ProductReviewSummary summary = existingSummary.get();
            summary.updateSummary(
                    ratingReviews.getTotalCount(),
                    averageScore,
                    summaryResult.getHighRatingSummary(),
                    ratingReviews.getHighRatingReviews().size(),
                    summaryResult.getLowRatingSummary(),
                    ratingReviews.getLowRatingReviews().size(),
                    ratingReviews.getMediumRatingReviews().size()
            );
            summaryRepository.save(summary);
            log.debug("기존 요약 업데이트: {} 상품 ID {}", reviewType, productId);
        } else {
            ProductReviewSummary newSummary = ProductReviewSummary.builder()
                    .productId(productId)
                    .reviewType(reviewType)
                    .totalReviewCount(ratingReviews.getTotalCount())
                    .averageScore(averageScore)
                    .highRatingSummary(summaryResult.getHighRatingSummary())
                    .highRatingCount(ratingReviews.getHighRatingReviews().size())
                    .lowRatingSummary(summaryResult.getLowRatingSummary())
                    .lowRatingCount(ratingReviews.getLowRatingReviews().size())
                    .mediumRatingCount(ratingReviews.getMediumRatingReviews().size())
                    .build();
            summaryRepository.save(newSummary);
            log.debug("새 요약 생성: {} 상품 ID {}", reviewType, productId);
        }
    }

    private double calculateAverageScore(RatingBasedReviews ratingReviews) {
        int totalReviews = ratingReviews.getTotalCount();
        if (totalReviews == 0) return 0.0;

        double totalScore = (ratingReviews.getHighRatingReviews().size() * 4.5) +
                (ratingReviews.getMediumRatingReviews().size() * 3.0) +
                (ratingReviews.getLowRatingReviews().size() * 1.5);

        return Math.round((totalScore / totalReviews) * 10.0) / 10.0;
    }
}
