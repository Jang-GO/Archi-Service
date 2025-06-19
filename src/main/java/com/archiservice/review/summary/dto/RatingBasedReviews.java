package com.archiservice.review.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RatingBasedReviews {
    private List<String> highRatingReviews;
    private List<String> lowRatingReviews;
    private List<String> mediumRatingReviews;

    public int getTotalCount() {
        return highRatingReviews.size() + lowRatingReviews.size() + mediumRatingReviews.size();
    }
}

