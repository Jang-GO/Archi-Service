package com.archiservice.recommend.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.recommend.dto.request.RecommendRequestDto;
import com.archiservice.recommend.dto.response.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface RecommendService {

    RecommendResponseDto recommend (CustomUser user);
    AIRecommendResponseDto evaluateRecommend(CustomUser user, RecommendRequestDto recommend);
    RecommendPlanResponseDto recommendPlan(CustomUser user);
    RecommendVasResponseDto recommendVas(CustomUser user);
    RecommendCouponResponseDto recommendCoupon(CustomUser user);

}
