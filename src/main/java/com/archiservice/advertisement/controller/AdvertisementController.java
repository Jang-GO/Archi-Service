package com.archiservice.advertisement.controller;

import com.archiservice.advertisement.dto.BannerClickRequest;
import com.archiservice.advertisement.service.AdvertisementService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ad")
public class AdvertisementController {

  private final AdvertisementService advertisementService;

  @PostMapping("/click")
  public ResponseEntity<ApiResponse<Void>> clickBanner (@AuthenticationPrincipal CustomUser customUser, @RequestBody BannerClickRequest request) {
    advertisementService.handleBannerClick(customUser, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
