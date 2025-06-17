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
import com.archiservice.recommend.dto.response.*;
import com.archiservice.recommend.service.RecommendService;
import com.archiservice.review.coupon.repository.CouponReviewRepository;
import com.archiservice.review.coupon.service.CouponReviewService;
import com.archiservice.review.plan.repository.PlanReviewRepository;
import com.archiservice.review.plan.service.PlanReviewService;
import com.archiservice.review.vas.repository.VasReviewRepository;
import com.archiservice.review.vas.service.VasReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

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

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RecommendResponseDto recommend(CustomUser user) {

        // 1. 요금제 추천
        RecommendPlanResponseDto recommendedPlans = recommendPlan(user);
        // 2. 부가서비스 추천 정렬
        RecommendVasResponseDto recommendedVass = recommendVas(user);
        // 3. 쿠폰 추천 정렬
        RecommendCouponResponseDto recommendedCoupons = recommendCoupon(user);

        // TODO : 성향태그가 아무것도 겹치지않는 경우 예외 고려
        return RecommendResponseDto.from( recommendedPlans, recommendedVass, recommendedCoupons );

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

    @Override
    public String uploadImage(MultipartFile file , String uploadDir) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            String imageUrl = "http://localhost:8080/images/" + fileName;
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    @Override
    public String analyzeImage(String imageUrl) {

        // ai 서버 호출 및 결과 반환

        String fileName = extractFileNameFromUrl(imageUrl);
        deleteFile(fileName);

        String result = "응답 결과";
        return result;
    }

    private String extractFileNameFromUrl(String fileUrl) {
        String regex = ".*/([^/?]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void deleteFile(String fileName) {
        // TODO : 디렉터리 경로 수정
        String uploadDir = "src/main/resources/images/";
        Path filePath = Paths.get(uploadDir + fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // TODO: 로그 처리
        }
    }

    @Override
    public String analyzeImage(MultipartFile file) {
        try {
            // 파일을 Stream으로 즉시 LLM 서버에 전달
            InputStream inputStream = file.getInputStream();

            String result = callAiServer(inputStream);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("AI 분석 실패", e);
        }
    }

    private String callAiServer(InputStream inputStream) {
        try {
            // 1헤더: multipart/form-data
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            //  바디: file 파트에 InputStreamResource 사용
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new InputStreamResource(inputStream) {
                @Override
                public String getFilename() {
                    return "temp.jpg"; // AI 서버 쪽에서 UploadFile로 받을 때 이름 필요
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 3요청: AI 서버의 /analyze-image 엔드포인트 호출
            String aiServerUrl = "http://archi-ai:8000/analyze-image"; // docker-compose service name 사용 예시

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServerUrl,
                    requestEntity,
                    String.class
            );

            // 4결과 반환
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("AI 서버 호출 실패", e);
        }
    }


}