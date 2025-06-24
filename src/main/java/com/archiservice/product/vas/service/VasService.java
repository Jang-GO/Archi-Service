package com.archiservice.product.vas.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasResponseDto;

public interface VasService {
    Page<VasResponseDto> getAllVas(Pageable pageable);
    VasDetailResponseDto getVasDetail(Long vasId);
    Vas getRandVasByCategoryCode(String categoryCode);
    VasDetailResponseDto findVasByName(String vasName);
}
