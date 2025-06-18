package com.archiservice.review.ai.service;

import com.archiservice.review.ai.dto.ModerationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReviewModerationScheduler {

    private final ReviewModerationService moderationService;

    public ReviewModerationScheduler(ReviewModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @Scheduled(cron = "0 0 2 * * *")  // 매일 오전 2시 실행
    public void scheduledReviewDeletion() {
        log.info("=== 스케줄링된 리뷰 삭제 작업 시작 ===");

        try {
            ModerationResult result = moderationService.moderateAllReviews();
            log.info("스케줄링 작업 완료 - 처리: {}, 삭제: {}, 소요시간: {}ms",
                    result.getTotalProcessed(),
                    result.getDeletedCount(),
                    result.getProcessingTimeMs());

        } catch (Exception e) {
            log.error("스케줄링된 리뷰 삭제 작업 중 오류 발생", e);
        }
    }
}

