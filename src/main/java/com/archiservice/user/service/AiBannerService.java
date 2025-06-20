package com.archiservice.user.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.user.dto.request.BannerRequestDto;
import com.archiservice.user.dto.response.BannerResponseDto;

import java.util.List;

public interface AiBannerService {
    BannerResponseDto getBanner(CustomUser user);
    String getRandSubtag(List<String> myTags);
    BannerResponseDto generateBanner(BannerRequestDto request);
}
