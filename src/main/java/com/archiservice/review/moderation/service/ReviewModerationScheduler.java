package com.archiservice.review.moderation.service;

import com.archiservice.review.moderation.dto.ModerationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReviewModerationScheduler {

    private final ReviewModerationService moderationService;

    private final TaskExecutor taskExecutor;

    public ReviewModerationScheduler(ReviewModerationService moderationService,
                                     @Qualifier("moderationTaskExecutor") TaskExecutor taskExecutor) {
        this.moderationService = moderationService;
        this.taskExecutor = taskExecutor;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Async
    public void scheduledReviewDeletion() {
        log.info("=== 병렬 리뷰 삭제 작업 시작 ===");

        long startTime = System.currentTimeMillis();

        CompletableFuture<ModerationResult> planFuture = CompletableFuture
                .supplyAsync(moderationService::moderateUnmoderatedPlanReviews, taskExecutor);

        CompletableFuture<ModerationResult> vasFuture = CompletableFuture
                .supplyAsync(moderationService::moderateUnmoderatedVasReviews, taskExecutor);

        CompletableFuture<ModerationResult> couponFuture = CompletableFuture
                .supplyAsync(moderationService::moderateUnmoderatedCouponReviews, taskExecutor);

        // 모든 작업 완료 대기
        CompletableFuture.allOf(planFuture, vasFuture, couponFuture)
                .thenRun(() -> {
                    try {
                        ModerationResult planResult = planFuture.get();
                        ModerationResult vasResult = vasFuture.get();
                        ModerationResult couponResult = couponFuture.get();

                        int totalProcessed = planResult.getTotalProcessed() +
                                vasResult.getTotalProcessed() +
                                couponResult.getTotalProcessed();
                        int totalDeleted = planResult.getDeletedCount() +
                                vasResult.getDeletedCount() +
                                couponResult.getDeletedCount();

                        long totalTime = System.currentTimeMillis() - startTime;

                        log.info("병렬 처리 완료 - 총 처리: {}, 총 삭제: {}, 소요시간: {}ms",
                                totalProcessed, totalDeleted, totalTime);

                    } catch (Exception e) {
                        log.error("병렬 처리 결과 수집 중 오류", e);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("병렬 처리 중 오류 발생", throwable);
                    return null;
                });
    }
}


