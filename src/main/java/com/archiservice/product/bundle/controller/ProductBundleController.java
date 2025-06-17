package com.archiservice.product.bundle.controller;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.product.bundle.dto.request.CreateBundleRequestDto;
import com.archiservice.product.bundle.dto.response.BundleCombinationResponseDto;
import com.archiservice.product.bundle.service.ProductBundleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class ProductBundleController {
    private final ProductBundleService bundleService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBundle(@Valid @RequestBody CreateBundleRequestDto requestDto, @AuthenticationPrincipal CustomUser customUser) {
        bundleService.createBundle(requestDto, customUser);
        return ResponseEntity.ok(ApiResponse.success("선택하신 조합으로 예약이 갱신되었습니다.", null));
    }

    @PostMapping("/{planId}/{vasId}/{couponId}")
    public ResponseEntity<ApiResponse<BundleCombinationResponseDto>> getBundleByIds(@PathVariable Long planId, @PathVariable Long vasId, @PathVariable Long couponId) {
        return ResponseEntity.ok(ApiResponse.success(bundleService.getBundleByIds(planId, vasId, couponId)));
    }

    @PutMapping("/{bundleId}/likeCount")
    public ResponseEntity<ApiResponse<Void>> updateLikeOrDislikeCount(@PathVariable Long bundleId, @RequestParam boolean isLike) {

        bundleService.updateLikeOrDislikeCount(bundleId, isLike);

        if(isLike){
            return ResponseEntity.ok(ApiResponse.success("좋아요", null));
        } else{
            return ResponseEntity.ok(ApiResponse.success("싫어요", null));
        }
    }


    @GetMapping("/sum-tag-code/{planId}/{vasId}/{couponId}")
    public ResponseEntity<ApiResponse<Long>> getCombinedTagCode(
            @RequestBody CreateBundleRequestDto requestDto) {

        long combinedTagCode = bundleService.getCombinedTagCode(requestDto.getPlanId(), requestDto.getVasId(), requestDto.getCouponId());
        return ResponseEntity.ok(ApiResponse.success("tagCode 합 생성완료" ,combinedTagCode));
    }


}
