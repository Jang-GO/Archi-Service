package com.archiservice.product.bundle.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.product.bundle.dto.request.CreateBundleRequestDto;
import com.archiservice.product.bundle.dto.response.BundleCombinationResponseDto;

public interface ProductBundleService {
    void createBundle(CreateBundleRequestDto createBundleRequestDto, CustomUser customUser);

    BundleCombinationResponseDto getBundleByIds(long planId, long vasId, long couponId);

    void updateLikeOrDislikeCount(long bundleId, boolean isLike);

    long getCombinedTagCode(long planId, long vasId, long couponId);
}
