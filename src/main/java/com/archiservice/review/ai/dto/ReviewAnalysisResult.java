package com.archiservice.review.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"shouldDelete", "reason", "severity", "categories", "confidenceScore"})
public class ReviewAnalysisResult {

    @JsonProperty("shouldDelete")
    @JsonPropertyDescription("Whether the review should be deleted")
    private boolean shouldDelete;

    @JsonProperty("reason")
    @JsonPropertyDescription("Detailed reason for deletion if shouldDelete is true")
    private String reason;

    @JsonProperty("severity")
    @JsonPropertyDescription("Severity level: LOW, MEDIUM, or HIGH")
    private String severity;

    @JsonProperty("categories")
    @JsonPropertyDescription("Array of violation categories")
    private List<String> categories;

    @JsonProperty("confidenceScore")
    @JsonPropertyDescription("Confidence score between 0.0 and 1.0")
    private double confidenceScore;

}