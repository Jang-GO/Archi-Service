package com.archiservice.review.ai.service;

import com.archiservice.review.ai.dto.ReviewAnalysisResult;
import com.archiservice.review.ai.service.SimpleModerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReviewAnalysisService {

    private final ChatClient chatClient;
    private final SimpleModerationService moderationService; // 추가
    private static final int BATCH_SIZE = 10;

    public ReviewAnalysisService(@Qualifier("reviewCleanBot") ChatClient chatClient,
                                 SimpleModerationService moderationService) {
        this.chatClient = chatClient;
        this.moderationService = moderationService;
    }

    public List<ReviewAnalysisResult> analyzeBatchReviews(List<String> reviewContents) {
        List<ReviewAnalysisResult> allResults = new ArrayList<>();

        for (int i = 0; i < reviewContents.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, reviewContents.size());
            List<String> batch = reviewContents.subList(i, endIndex);

            log.info("하이브리드 배치 처리 중: {}/{} (리뷰 {}-{})",
                    (i / BATCH_SIZE) + 1,
                    (reviewContents.size() + BATCH_SIZE - 1) / BATCH_SIZE,
                    i + 1, endIndex);

            List<ReviewAnalysisResult> batchResults = processHybridBatch(batch);
            allResults.addAll(batchResults);
        }

        return allResults;
    }

    private List<ReviewAnalysisResult> processHybridBatch(List<String> reviewContents) { // 새 메서드
        List<ReviewAnalysisResult> results = new ArrayList<>();
        List<String> needsGptAnalysis = new ArrayList<>();

        for (String content : reviewContents) {
            if (moderationService.isFlagged(content)) {
                results.add(ReviewAnalysisResult.builder()
                        .shouldDelete(true)
                        .reason(moderationService.getModerationReason(content))
                        .severity("HIGH")
                        .categories(List.of("OpenAI_Moderation"))
                        .confidenceScore(0.95)
                        .build());
            } else {
                needsGptAnalysis.add(content);
            }
        }

        if (!needsGptAnalysis.isEmpty()) {
            log.debug("GPT 분석 필요한 리뷰 수: {}/{}", needsGptAnalysis.size(), reviewContents.size());
            List<ReviewAnalysisResult> gptResults = processBatch(needsGptAnalysis);
            results.addAll(gptResults);
        }

        return results;
    }

    private List<ReviewAnalysisResult> processBatch(List<String> reviewContents) {
        try {
            String batchPrompt = createBatchPrompt(reviewContents);

            List<ReviewAnalysisResult> results = chatClient.prompt()
                    .user(batchPrompt)
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});

            log.debug("GPT 배치 분석 완료: {} 개 리뷰 처리", results.size());
            return results;

        } catch (Exception e) {
            log.error("GPT 배치 분석 중 오류 발생: {}", e.getMessage(), e);
            return createErrorResults(reviewContents.size(), e.getMessage());
        }
    }

    private String createBatchPrompt(List<String> reviewContents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following reviews and return results for each:\n\n");

        for (int i = 0; i < reviewContents.size(); i++) {
            prompt.append(String.format("Review %d: \"%s\"\n\n", i + 1, reviewContents.get(i)));
        }

        prompt.append(String.format("""
            Return an array of %d analysis results in the exact same order as the reviews above.
            Each result must follow the JSON schema format specified in the system prompt.
            
            Response format:
            [
                {"shouldDelete": false, "reason": "", "severity": "LOW", "categories": [], "confidenceScore": 0.9},
                {"shouldDelete": true, "reason": "Contains profanity", "severity": "HIGH", "categories": ["Profanity"], "confidenceScore": 0.95},
                ...
            ]
            """, reviewContents.size()));

        return prompt.toString();
    }

    private List<ReviewAnalysisResult> createErrorResults(int count, String errorMessage) {
        List<ReviewAnalysisResult> errorResults = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            errorResults.add(ReviewAnalysisResult.builder()
                    .shouldDelete(false)
                    .reason("분석 실패: " + errorMessage)
                    .severity("LOW")
                    .categories(List.of())
                    .confidenceScore(0.0)
                    .build());
        }
        return errorResults;
    }
}
