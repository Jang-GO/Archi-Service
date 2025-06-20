package com.archiservice.advertisement.controller;

import com.archiservice.advertisement.dto.request.BannerClickRequest;
import com.archiservice.advertisement.dto.response.BannerResponseDto;
import com.archiservice.advertisement.service.AiBannerService;
import com.archiservice.advertisement.service.impl.AdvertisementService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ad")
public class AdvertisementController {

  private final AdvertisementService advertisementService;
  private final AiBannerService aiBannerService;

  @PostMapping("/click")
  public ResponseEntity<ApiResponse<Void>> clickBanner (@AuthenticationPrincipal CustomUser customUser, @RequestBody BannerClickRequest request) {
    advertisementService.handleBannerClick(customUser, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/ad-banner")
  public ResponseEntity<ApiResponse<BannerResponseDto>> getAdBanner(@AuthenticationPrincipal CustomUser user) {
    return ResponseEntity.ok(ApiResponse.success(aiBannerService.getBanner(user)));
  }
}
