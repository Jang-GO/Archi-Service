package com.archiservice.advertisement.controller;

import com.archiservice.advertisement.dto.request.BannerClickRequest;
import com.archiservice.advertisement.dto.response.BannerResponseDto;
import com.archiservice.advertisement.service.AiBannerService;
import com.archiservice.advertisement.service.impl.AdvertisementService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ad")
@Slf4j
public class AdvertisementController {

  private final AdvertisementService advertisementService;
  private final AiBannerService aiBannerService;

  private final Map<String, BannerResponseDto> bannerCache = new ConcurrentHashMap<>();
  private final Map<String, String> bannerStatus = new ConcurrentHashMap<>();

  @GetMapping("/ad-banner")
  public ResponseEntity<ApiResponse<String>> getAdBanner(@AuthenticationPrincipal CustomUser user) {
    String taskId = UUID.randomUUID().toString();

    // 상태를 PROCESSING으로 설정
    bannerStatus.put(taskId, "PROCESSING");

    // 비동기로 배너 생성 시작
    aiBannerService.getBannerAsync(user)
            .orTimeout(10, TimeUnit.DAYS.SECONDS) // 10초 타임아웃
            .thenAccept(banner -> {
              bannerCache.put(taskId, banner);
              bannerStatus.put(taskId, "COMPLETED");
              log.info("배너 생성 완료 - TaskId: {}", taskId);
            })
            .exceptionally(throwable -> {
              log.error("배너 생성 실패 - TaskId: {}", taskId, throwable);
              bannerStatus.put(taskId, "FAILED");
              // 실패 시 기본 배너 제공
              bannerCache.put(taskId, createDefaultBanner(user));
              return null;
            });

    log.info("배너 요청 접수 - TaskId: {}, 사용자: {}", taskId, user.getId());
    return ResponseEntity.ok(ApiResponse.success(taskId));
  }

  @GetMapping("/ad-banner/result/{taskId}")
  public ResponseEntity<ApiResponse<BannerResponseDto>> getBannerResult(@PathVariable String taskId) {
    String status = bannerStatus.get(taskId);
    BannerResponseDto banner = bannerCache.get(taskId);

    if ("COMPLETED".equals(status) && banner != null) {
      // 결과 반환 후 캐시에서 제거
      bannerCache.remove(taskId);
      bannerStatus.remove(taskId);
      return ResponseEntity.ok(ApiResponse.success(banner));
    } else if ("FAILED".equals(status)) {
      // 실패한 경우에도 기본 배너 반환
      bannerCache.remove(taskId);
      bannerStatus.remove(taskId);
      return ResponseEntity.ok(ApiResponse.success(banner != null ? banner : createDefaultBanner(null)));
    } else if ("PROCESSING".equals(status)) {
      // 아직 처리 중
      return ResponseEntity.accepted().body(ApiResponse.success(null));
    } else {
      // TaskId가 존재하지 않음
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/ad-banner/status/{taskId}")
  public ResponseEntity<ApiResponse<String>> getBannerStatus(@PathVariable String taskId) {
    String status = bannerStatus.getOrDefault(taskId, "NOT_FOUND");
    return ResponseEntity.ok(ApiResponse.success(status));
  }

  private BannerResponseDto createDefaultBanner(CustomUser user) {
    return BannerResponseDto.builder()
            .vasId(0L)
            .vasName("추천 상품")
            .description("맞춤 상품을 준비 중입니다")
            .build();
  }

  @PostMapping("/click")
  public ResponseEntity<ApiResponse<Void>> clickBanner (@AuthenticationPrincipal CustomUser customUser, @RequestBody BannerClickRequest request) {
    advertisementService.handleBannerClick(customUser, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

}
