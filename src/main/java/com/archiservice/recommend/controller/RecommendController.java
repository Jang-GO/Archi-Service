package com.archiservice.recommend.controller;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.recommend.dto.request.RecommendRequestDto;
import com.archiservice.recommend.dto.response.*;
import com.archiservice.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping()
    public ResponseEntity<ApiResponse<RecommendResponseDto>> recommend(@AuthenticationPrincipal CustomUser customUser) {
        return ResponseEntity.ok(ApiResponse.success("조합 추천 성공", recommendService.recommend(customUser)));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<AIRecommendResponseDto>> evaluateRecommend(@AuthenticationPrincipal CustomUser customUser,
                                                                                 @RequestBody RecommendRequestDto recommend) {
        return ResponseEntity.ok(ApiResponse.success("추천 평가 성공", recommendService.evaluateRecommend(customUser, recommend)));
    }

    @GetMapping("/plan")
    public ResponseEntity<ApiResponse<RecommendPlanResponseDto>> recommendPlan(@AuthenticationPrincipal CustomUser customUser){
        return ResponseEntity.ok(ApiResponse.success("요금제 추천 성공", recommendService.recommendPlan(customUser)));
    }

    @GetMapping("/vas")
    public ResponseEntity<ApiResponse<RecommendVasResponseDto>> recommendVas(@AuthenticationPrincipal CustomUser customUser){
        return ResponseEntity.ok(ApiResponse.success("부가서비스 추천 성공", recommendService.recommendVas(customUser)));
    }

    @GetMapping("/coupon")
    public ResponseEntity<ApiResponse<RecommendCouponResponseDto>> recommendCoupon(@AuthenticationPrincipal CustomUser customUser){
        return ResponseEntity.ok(ApiResponse.success("쿠폰 추천 성공", recommendService.recommendCoupon(customUser)));
    }

}
