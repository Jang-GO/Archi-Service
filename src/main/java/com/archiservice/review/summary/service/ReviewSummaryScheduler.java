package com.archiservice.review.summary.service;

import com.archiservice.review.summary.dto.SummaryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReviewSummaryScheduler {

    private final ProductReviewSummaryService summaryService;

    public ReviewSummaryScheduler(ProductReviewSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledReviewSummary() {
        log.info("=== 스케줄링된 리뷰 요약 작업 시작 ===");

        try {
            CompletableFuture<SummaryResult> futureResult  = summaryService.summarizeAllProductReviews();

            // 비동기 작업 완료까지 대기
            SummaryResult result = futureResult.get();

            log.info("리뷰 요약 작업 완료 - 총 상품: {}, 요약 완료: {}, 소요시간: {}ms",
                    result.getTotalProducts(),
                    result.getSummarizedProducts(),
                    result.getProcessingTimeMs());

        } catch (Exception e) {
            log.error("스케줄링된 리뷰 요약 작업 중 오류 발생", e);
        }
    }
}

