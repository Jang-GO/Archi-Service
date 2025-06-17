package com.archiservice.recommend.controller;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.recommend.dto.response.*;
import com.archiservice.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping()
    public ResponseEntity<ApiResponse<RecommendResponseDto>> recommend(@AuthenticationPrincipal CustomUser customUser){
        return ResponseEntity.ok(ApiResponse.success("조합 추천 성공", recommendService.recommend(customUser)));
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

    // 이미지 -> url
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("uploadDir") String uploadDir
    ) {

        String imageUrl = recommendService.uploadImage(file ,uploadDir );
        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", imageUrl));
    }

    // url -> 분석결과
    @PostMapping("/analyze-image")
    public ResponseEntity<ApiResponse<String>> analyzeImage(@RequestBody ImageUrlRequest request) {
        String tags = recommendService.analyzeImage(request.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success("이미지 분석 성공", tags));
    }

    /*****************************/

    // 이미지 저장 없이 바로 넘겨줌
    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> analyzeImage(
            @RequestPart("file") MultipartFile file) {

        // Service에서 Stream으로 LLM 분석 후 결과만 반환
        String result = recommendService.analyzeImage(file);

        return ResponseEntity.ok(ApiResponse.success("이미지 분석 성공", result));
    }



}
