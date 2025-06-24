package com.archiservice.product.plan.service;

import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlanService {
    Page<PlanResponseDto> getAllPlans(Pageable pageable);
    PlanDetailResponseDto getPlanDetail(Long planId);
    PlanDetailResponseDto findPlanByName(String planName);
}
