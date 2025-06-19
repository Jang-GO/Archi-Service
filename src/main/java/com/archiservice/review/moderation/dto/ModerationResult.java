package com.archiservice.review.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResult {
    private int totalProcessed;
    private int deletedCount;
    private int errorCount;
    private long processingTimeMs;
    private String reviewType;

    public double getDeletionRate() {
        return totalProcessed > 0 ? (double) deletedCount / totalProcessed * 100 : 0.0;
    }
}
