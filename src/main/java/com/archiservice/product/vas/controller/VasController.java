package com.archiservice.product.vas.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasResponseDto;
import com.archiservice.product.vas.service.VasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vass")
@RequiredArgsConstructor
public class VasController {

    private final VasService vasService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VasResponseDto>>> getAllVas(@PageableDefault(size = 20, sort = "vasId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vasService.getAllVas(pageable)));
    }

    @GetMapping("/{vasId}")
    public ResponseEntity<ApiResponse<VasDetailResponseDto>> getVasDetail(@PathVariable("vasId") Long vasId) {
        return ResponseEntity.ok(ApiResponse.success(vasService.getVasDetail(vasId)));
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<VasDetailResponseDto>> searchCouponByName(@RequestParam("name") String vasName) {
       return ResponseEntity.ok(ApiResponse.success(vasService.findVasByName(vasName)));
    }
}
