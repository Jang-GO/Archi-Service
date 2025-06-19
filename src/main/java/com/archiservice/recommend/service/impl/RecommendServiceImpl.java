package com.archiservice.recommend.service.impl;


import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.security.CustomUser;
import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.repository.CouponRepository;
import com.archiservice.product.plan.domain.Plan;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.repository.PlanRepository;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.repository.VasRepository;
import com.archiservice.recommend.dto.request.RecommendRequestDto;
import com.archiservice.recommend.dto.response.*;
import com.archiservice.recommend.service.RecommendService;
import com.archiservice.review.coupon.repository.CouponReviewRepository;
import com.archiservice.review.coupon.service.CouponReviewService;
import com.archiservice.review.plan.repository.PlanReviewRepository;
import com.archiservice.review.plan.service.PlanReviewService;
import com.archiservice.review.vas.repository.VasReviewRepository;
import com.archiservice.review.vas.service.VasReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

    private final ChatClient recommendChatClient;

    private final TagMetaService tagMetaService;
    private final PlanRepository planRepository;
    private final VasRepository vasRepository;

    private final PlanReviewRepository planReviewRepository;
    private final VasReviewRepository vasReviewRepository;
    private final CouponReviewRepository couponReviewRepository;

    private final CouponRepository couponRepository;
    private final CommonCodeService commonCodeService;

    private final PlanReviewService planReviewService;
    private final VasReviewService vasReviewService;
    private final CouponReviewService couponReviewService;



    public RecommendServiceImpl(@Qualifier("recommendChatClient") ChatClient recommendChatClient, TagMetaService tagMetaService,
			PlanRepository planRepository, VasRepository vasRepository, PlanReviewRepository planReviewRepository,
			VasReviewRepository vasReviewRepository, CouponReviewRepository couponReviewRepository,
			CouponRepository couponRepository, CommonCodeService commonCodeService, PlanReviewService planReviewService,
			VasReviewService vasReviewService, CouponReviewService couponReviewService) {
		super();
		this.recommendChatClient = recommendChatClient;
		this.tagMetaService = tagMetaService;
		this.planRepository = planRepository;
		this.vasRepository = vasRepository;
		this.planReviewRepository = planReviewRepository;
		this.vasReviewRepository = vasReviewRepository;
		this.couponReviewRepository = couponReviewRepository;
		this.couponRepository = couponRepository;
		this.commonCodeService = commonCodeService;
		this.planReviewService = planReviewService;
		this.vasReviewService = vasReviewService;
		this.couponReviewService = couponReviewService;
	}

	@Override
    public RecommendResponseDto recommend (CustomUser user) {
        return RecommendResponseDto.from(recommendPlan(user), recommendVas(user), recommendCoupon(user));
    }

    @Override
    public AIRecommendResponseDto evaluateRecommend(CustomUser user, RecommendRequestDto recommend) {

        List<String> tagList = tagMetaService.extractTagsFromCode(user.getTagCode());

        return recommendChatClient.prompt()
                .system(sp -> sp
                        .param("tagList", tagList)
                        .param("recommend", recommend)
                )
                .templateRenderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .call()
                .entity(AIRecommendResponseDto.class);
    }

    @Override
    public RecommendPlanResponseDto recommendPlan(CustomUser user) {
        long userTagCode = user.getUser().getTagCode();
        double globalPlanAvg = getGlobalAvg("plan");
        Integer planMinReviews = planReviewService.getAverageReviewCountPerPlanAsInteger();

        Map<Long, ScoreResponseDto> scoreMap = planReviewService.getPlanScoreStatistics();
        List<Map.Entry<Plan, Integer>> bitCountSorted = planRepository.findAll().stream()
                .map(plan -> Map.entry(plan, Long.bitCount(userTagCode & plan.getTagCode())))
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .toList();

        List<ProductWithScore<Plan>> topPlans = bitCountSorted.stream()
                .map(entry -> {
                    Plan plan = entry.getKey();
                    int bitCount = entry.getValue();

                    ScoreResponseDto score = scoreMap.get(plan.getPlanId());
                    int reviewCount = score != null ? score.getReviewCount() : 0;
                    double avgRating = score != null ? score.getAverageScore() : 0.0;
                    double bayesScore = computeBayesianAverage(avgRating, reviewCount, globalPlanAvg, planMinReviews);

                    return new ProductWithScore<>(plan, bitCount, bayesScore);
                })
                .sorted(Comparator
                        .comparingInt(ProductWithScore<Plan>::getBitCount).reversed()
                        .thenComparing(Comparator.comparingDouble(ProductWithScore<Plan>::getBayesScore).reversed()))
                .limit(5)
                .toList();

        // TODO : TagCode , CommonCode 쿼리 최적화 필요
        List<PlanDetailResponseDto> recommendedPlans = topPlans.stream()
                .map(p -> {
                    List<String> tags = tagMetaService.extractTagsFromCode(p.product.getTagCode());
                    String category = commonCodeService.getCodeName("G02", p.product.getCategoryCode());
                    String targetAge = commonCodeService.getCodeName("G01", p.product.getAgeCode());
                    return PlanDetailResponseDto.from(p.product, tags, category, targetAge);
                })
                .toList();

        return new RecommendPlanResponseDto(recommendedPlans);
    }


    @Override
    public RecommendVasResponseDto recommendVas(CustomUser user) {
        long userTagCode = user.getUser().getTagCode();
        double globalVasAvg = getGlobalAvg("vas");

        Map<Long, ScoreResponseDto> scoreMap = vasReviewService.getVasScoreStatistics();
        Integer vasMinReviews = vasReviewService.getAverageReviewCountPerVasAsInteger();

        List<Map.Entry<Vas, Integer>> bitCountSorted = vasRepository.findAll().stream()
                .map(vas -> Map.entry(vas, Long.bitCount(userTagCode & vas.getTagCode())))
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .toList();

        List<ProductWithScore<Vas>> topVass = bitCountSorted.stream()
                .map(entry -> {
                    Vas vas = entry.getKey();
                    int bitCount = entry.getValue();

                    ScoreResponseDto score = scoreMap.get(vas.getVasId());
                    int reviewCount = score != null ? score.getReviewCount() : 0;
                    double avgRating = score != null ? score.getAverageScore() : 0.0;
                    double bayesScore = computeBayesianAverage(avgRating, reviewCount, globalVasAvg, vasMinReviews);

                    return new ProductWithScore<>(vas, bitCount, bayesScore);
                })
                .sorted(Comparator
                        .comparingInt(ProductWithScore<Vas>::getBitCount).reversed()
                        .thenComparing(Comparator.comparingDouble(ProductWithScore<Vas>::getBayesScore).reversed()))
                .limit(5)
                .toList();

        List<VasDetailResponseDto> recommendedVass = topVass.stream()
                .map(p -> {
                    List<String> tags = tagMetaService.extractTagsFromCode(p.product.getTagCode());
                    String category = commonCodeService.getCodeName("G03", p.product.getCategoryCode());
                    return VasDetailResponseDto.from(p.product, tags, category);
                })
                .toList();

        return new RecommendVasResponseDto(recommendedVass);
    }


    @Override
    public RecommendCouponResponseDto recommendCoupon(CustomUser user) {
        long userTagCode = user.getUser().getTagCode();
        double globalCouponAvg = getGlobalAvg("coupon");

        Map<Long, ScoreResponseDto> scoreMap = couponReviewService.getCouponScoreStatistics();
        Integer couponMinReviews = couponReviewService.getAverageReviewCountPerCouponAsInteger();

        List<Map.Entry<Coupon, Integer>> bitCountSorted = couponRepository.findAll().stream()
                .map(coupon -> Map.entry(coupon, Long.bitCount(userTagCode & coupon.getTagCode())))
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .toList();

        List<ProductWithScore<Coupon>> topCoupons = bitCountSorted.stream()
                .map(entry -> {
                    Coupon coupon = entry.getKey();
                    int bitCount = entry.getValue();

                    ScoreResponseDto score = scoreMap.get(coupon.getCouponId());
                    int reviewCount = score != null ? score.getReviewCount() : 0;
                    double avgRating = score != null ? score.getAverageScore() : 0.0;
                    double bayesScore = computeBayesianAverage(avgRating, reviewCount, globalCouponAvg, couponMinReviews);

                    return new ProductWithScore<>(coupon, bitCount, bayesScore);
                })
                .sorted(Comparator
                        .comparingInt(ProductWithScore<Coupon>::getBitCount).reversed()
                        .thenComparing(Comparator.comparingDouble(ProductWithScore<Coupon>::getBayesScore).reversed()))
                .limit(5)
                .toList();

        List<CouponDetailResponseDto> recommendedCoupons = topCoupons.stream()
                .map(p -> {
                    List<String> tags = tagMetaService.extractTagsFromCode(p.product.getTagCode());
                    String category = commonCodeService.getCodeName("G04", p.product.getCategoryCode());
                    return CouponDetailResponseDto.from(p.product, tags, category);
                })
                .toList();

        return new RecommendCouponResponseDto(recommendedCoupons);
    }


    public double getGlobalAvg(String type) {
        return switch (type.toLowerCase()) {
            case "plan" -> Optional.ofNullable(planReviewRepository.findAverageRatingByPlanIsNotNull()).orElse(0.0);
            case "vas" -> Optional.ofNullable(vasReviewRepository.findAverageRatingByVasIsNotNull()).orElse(0.0);
            case "coupon" -> Optional.ofNullable(couponReviewRepository.findAverageRatingByCouponIsNotNull()).orElse(0.0);
            default -> 0.0;
        };
    }

    // 베이지안 평균 계산
    //                            해당 상품의 평균 평점 , 해당 상품에 달린 총 리뷰 수 , 전체 상품의 전역 평균 평점 , 신뢰도 기준으로 삼을 최소 리뷰 수
    private double computeBayesianAverage(double avgRating, int reviewCount, double globalAvg, int minReviews) {
        return (reviewCount / (double)(reviewCount + minReviews)) * avgRating +
                (minReviews / (double)(reviewCount + minReviews)) * globalAvg;
    }

    private static class ProductWithScore<T> {
        T product;
        int bitCount;
        double bayesScore;

        public ProductWithScore(T product, int bitCount, double bayesScore) {
            this.product = product;
            this.bitCount = bitCount;
            this.bayesScore = bayesScore;
        }

        public T getProduct() {
            return product;
        }

        public int getBitCount() {
            return bitCount;
        }

        public double getBayesScore() {
            return bayesScore;
        }
    }

}