package com.archiservice.review.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResult {
    private int totalProducts;
    private int summarizedProducts;
    private long processingTimeMs;
    private String productType;
}

