package com.archiservice.product.vas.service.impl;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasResponseDto;
import com.archiservice.product.vas.repository.VasRepository;
import com.archiservice.product.vas.service.VasService;
import com.archiservice.review.summary.domain.ProductReviewSummary;
import com.archiservice.review.summary.dto.SimplifiedSummaryResult;
import com.archiservice.review.summary.repository.ProductReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VasServiceImpl implements VasService {

    public static final String CATEGORY_GROUP_CODE = "G03"; // 부가 서비스
    private final VasRepository vasRepository;
    private final TagMetaService tagMetaService;
    private final CommonCodeService commonCodeService;
    private final ProductReviewSummaryRepository reviewSummaryRepository;

    @Override
    public Page<VasResponseDto> getAllVas(Pageable pageable) {
        Page<Vas> vasPage = vasRepository.findAll(pageable);

        return vasPage.map(vas -> {
            List<String> tags = tagMetaService.extractTagsFromCode(vas.getTagCode());
            String category = commonCodeService.getCodeName(CATEGORY_GROUP_CODE, vas.getCategoryCode());
            return VasResponseDto.from(vas, tags, category);
        });
    }

    @Override
    public VasDetailResponseDto getVasDetail(Long vasId) {
        Vas vas = vasRepository.findById(vasId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        List<String> tags = tagMetaService.extractTagsFromCode(vas.getTagCode());
        String category = commonCodeService.getCodeName(CATEGORY_GROUP_CODE, vas.getCategoryCode());

        Optional<ProductReviewSummary> reviewSummaryOpt = reviewSummaryRepository.findByProductIdAndReviewType(vasId, "VAS");
        if(reviewSummaryOpt.isEmpty()) return VasDetailResponseDto.from(vas, tags, category);

        SimplifiedSummaryResult simplifiedSummaryResult = SimplifiedSummaryResult.from(reviewSummaryOpt.get());

        return VasDetailResponseDto.from(vas, tags, category, simplifiedSummaryResult);
    }
}
