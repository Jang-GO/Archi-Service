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

    // 번들이 저장된다는 의미는 기존에 없었던 번들이라는 의미
    // -> 사용자의 계약이 이루어졌다는 의미이기 때문에 @AuthenticationPrincipal CustomUser customUser 를 통해 사용자를 식별하고
    // 해당 사용자 id 로 계약 테이블에 등록까지 수행하는 메서드
    // 챗봇은 사용자 태그를 기반으로 3종을 개별 추천 ( 조합 자체를 추천하는 것이 아님! ), 사용자는 추천받은 각각의 후보 리스트에서 선택하여 최종 조합 생성
    // -> 사용자가 최종 결정자이므로 계약이 이루어졌다는 의미를 가짐 : 챗봇이 추천해준 경우에도 최종 결정자는 사용자이므로 해당 메서드로 처리 가능
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBundle(@Valid @RequestBody CreateBundleRequestDto requestDto, @AuthenticationPrincipal CustomUser customUser) {
        bundleService.createBundle(requestDto, customUser);
        return ResponseEntity.ok(ApiResponse.success("선택하신 조합으로 예약이 갱신되었습니다.", null));
    }

    // ai 가 추천해준 조합이 최초의 조합이라면, bundle 테이블에 존재하지 않기 때문에 Session 에서 각 id 를 받아 각각 처리
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
