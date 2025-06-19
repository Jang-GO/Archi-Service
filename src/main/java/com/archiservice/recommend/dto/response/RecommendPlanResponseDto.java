package com.archiservice.recommend.dto.response;

import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendPlanResponseDto {
    List<PlanDetailResponseDto> plans;
}
