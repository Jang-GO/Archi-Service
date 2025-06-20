package com.archiservice.user.controller;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.user.dto.request.PasswordUpdateRequestDto;
import com.archiservice.user.dto.request.ReservationRequestDto;
import com.archiservice.user.dto.request.TendencyUpdateRequestDto;
import com.archiservice.user.dto.response.BannerResponseDto;
import com.archiservice.user.dto.response.ContractDetailResponseDto;
import com.archiservice.user.dto.response.ProfileResponseDto;
import com.archiservice.user.dto.response.TendencyResponseDto;
import com.archiservice.user.service.AiBannerService;
import com.archiservice.user.service.ContractService;
import com.archiservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.archiservice.user.enums.Period.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ContractService contractService;
    private final AiBannerService aiBannerService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getUserProfile(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(user)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@Valid @RequestBody PasswordUpdateRequestDto request, @AuthenticationPrincipal CustomUser user) {
        userService.updatePassword(request,user);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공"));
    }

    @GetMapping("/tendency")
    public ResponseEntity<ApiResponse<List<TendencyResponseDto>>> getUserTendency(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserTendency(user)));
    }

    @PutMapping("/tendency/update")
    public ResponseEntity<ApiResponse<String>> updateUserTendency(@Valid @RequestBody TendencyUpdateRequestDto request, @AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateTendency(request, user)));
    }

    @GetMapping("/current/plans")
    public ResponseEntity<ApiResponse<PlanDetailResponseDto>> getCurrentPlan(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getPlan(CURRENT, user)));
    }

    @GetMapping("/current/vass")
    public ResponseEntity<ApiResponse<VasDetailResponseDto>> getCurrentService(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getVas(CURRENT, user)));
    }

    @GetMapping("/current/coupons")
    public ResponseEntity<ApiResponse<CouponDetailResponseDto>> getCurrentCoupon(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getCoupon(CURRENT, user)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<ContractDetailResponseDto>>> getCurrentContract(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContract(CURRENT, user)));
    }

    @GetMapping("/next/plans")
    public ResponseEntity<ApiResponse<PlanDetailResponseDto>> getNextPlan(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getPlan(NEXT, user)));
    }

    @GetMapping("/next/vass")
    public ResponseEntity<ApiResponse<VasDetailResponseDto>> getNextService(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getVas(NEXT, user)));
    }

    @GetMapping("/next/coupons")
    public ResponseEntity<ApiResponse<CouponDetailResponseDto>> getNextCoupon(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getCoupon(NEXT, user)));
    }

    @GetMapping("/next")
    public ResponseEntity<ApiResponse<List<ContractDetailResponseDto>>> getNextContract(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContract(NEXT, user)));
    }

    @PutMapping("/next/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelNextContract(@AuthenticationPrincipal CustomUser user) {
        contractService.cancelNextContract(user);
        return ResponseEntity.ok(ApiResponse.success("예약 취소 성공",null));
    }

    @PutMapping("/next/update")
    public ResponseEntity<ApiResponse<Void>> updateNextContract(@Valid @RequestBody ReservationRequestDto requestDto, @AuthenticationPrincipal CustomUser user) {
        contractService.updateNextContract(requestDto, user);
        return ResponseEntity.ok(ApiResponse.success("예약 갱신 성공", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ContractDetailResponseDto>>> getHistoryContract(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContract(HISTORY, user)));
    }

    @GetMapping("/ad-banner")
    public ResponseEntity<ApiResponse<BannerResponseDto>> getAdBanner(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(ApiResponse.success(aiBannerService.getBanner(user)));
    }

}
