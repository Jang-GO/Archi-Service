package com.archiservice.recommend.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.recommend.dto.response.RecommendCouponResponseDto;
import com.archiservice.recommend.dto.response.RecommendPlanResponseDto;
import com.archiservice.recommend.dto.response.RecommendResponseDto;
import com.archiservice.recommend.dto.response.RecommendVasResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface RecommendService {

    // 태그기반 리스트 반환
    RecommendResponseDto recommend(CustomUser user);
    RecommendPlanResponseDto recommendPlan(CustomUser user);
    RecommendVasResponseDto recommendVas(CustomUser user);
    RecommendCouponResponseDto recommendCoupon(CustomUser user);

    // 이미지 업로드
    String uploadImage(MultipartFile file , String uploadDir);
    // 이미지 분석
    String analyzeImage(String imageUrl);

    //
    String analyzeImage(MultipartFile file);
}
