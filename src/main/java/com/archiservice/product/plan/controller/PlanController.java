package com.archiservice.product.plan.controller;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanResponseDto;
import com.archiservice.product.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
public class PlanController {
    private final PlanService planService;

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<PlanResponseDto>>> getAllPlans(@PageableDefault(size = 20, sort = "planId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(planService.getAllPlans(pageable)));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanDetailResponseDto>> getPlanDetail(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanDetail(planId)));
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PlanDetailResponseDto>> searchPlanByName(@RequestParam("name") String planName) {
       return ResponseEntity.ok(ApiResponse.success(planService.findPlanByName(planName)));
    }

}
