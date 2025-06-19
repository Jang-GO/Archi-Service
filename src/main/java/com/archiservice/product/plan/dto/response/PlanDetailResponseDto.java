package com.archiservice.product.plan.dto.response;

import com.archiservice.product.plan.domain.Plan;
import com.archiservice.review.summary.dto.SimplifiedSummaryResult;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanDetailResponseDto {
    private Long planId;
    private String planName;
    private Integer price;
    private Integer monthData;
    private String callUsage;
    private String messageUsage;
    private String benefit;
    private List<String> tags;
    private String category;
    private String targetAge;

    private SimplifiedSummaryResult reviewSummary;

    public static PlanDetailResponseDto from(Plan plan, List<String> tags, String category, String targetAge, SimplifiedSummaryResult simplifiedSummaryResult) {
        return PlanDetailResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .monthData(plan.getMonthData())
                .callUsage(plan.getCallUsage())
                .messageUsage(plan.getMessageUsage())
                .benefit(plan.getBenefit())
                .tags(tags)
                .category(category)
                .targetAge(targetAge)
                .reviewSummary(simplifiedSummaryResult)
                .build();
    }

    public static PlanDetailResponseDto from(Plan plan, List<String> tags, String category, String targetAge) {
        return PlanDetailResponseDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .monthData(plan.getMonthData())
                .callUsage(plan.getCallUsage())
                .messageUsage(plan.getMessageUsage())
                .benefit(plan.getBenefit())
                .tags(tags)
                .category(category)
                .targetAge(targetAge)
                .build();
    }
}
