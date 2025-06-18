package com.archiservice.review.summary.repository;

import com.archiservice.review.summary.domain.ProductReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductReviewSummaryRepository extends JpaRepository<ProductReviewSummary, Long> {

    Optional<ProductReviewSummary> findByProductIdAndReviewType(Long productId, String reviewType);

    List<ProductReviewSummary> findByReviewType(String reviewType);

    List<ProductReviewSummary> findBySummaryDate(LocalDate summaryDate);

    @Query("SELECT COUNT(prs) FROM ProductReviewSummary prs WHERE prs.summaryDate = :date")
    long countBySummaryDate(@Param("date") LocalDate date);
}

