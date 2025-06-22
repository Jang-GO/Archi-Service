package com.archiservice.advertisement.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.advertisement.dto.request.BannerRequestDto;
import com.archiservice.advertisement.dto.response.BannerResponseDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiBannerService {
    BannerResponseDto getBanner(CustomUser user);
    String getRandSubtag(List<String> myTags);
    BannerResponseDto generateBanner(BannerRequestDto request);
    CompletableFuture<BannerResponseDto> getBannerAsync(CustomUser customUser);
    void loadPromptTemplate(); // 추가
}
