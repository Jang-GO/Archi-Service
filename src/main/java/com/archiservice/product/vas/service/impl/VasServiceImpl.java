package com.archiservice.product.vas.service.impl;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasResponseDto;
import com.archiservice.product.vas.repository.VasRepository;
import com.archiservice.product.vas.service.VasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VasServiceImpl implements VasService {

    public static final String CATEGORY_GROUP_CODE = "G03"; // 부가 서비스
    private final VasRepository vasRepository;
    private final TagMetaService tagMetaService;
    private final CommonCodeService commonCodeService;

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

        return VasDetailResponseDto.from(vas, tags, category);
    }

    @Override
    public Vas getRandVasByCategoryCode(String categoryCode) {
        List<Vas> vasList = vasRepository.findVasByCategoryCode(categoryCode);

        if (vasList == null || vasList.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        int randomIndex = new Random().nextInt(vasList.size());

        return vasList.get(randomIndex);
    }


}
