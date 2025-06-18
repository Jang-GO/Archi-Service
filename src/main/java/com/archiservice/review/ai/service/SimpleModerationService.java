package com.archiservice.review.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.moderation.*;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SimpleModerationService {

    private final OpenAiModerationModel moderationModel;

    public SimpleModerationService(OpenAiModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    public boolean isFlagged(String content) {
        try {
            ModerationPrompt prompt = new ModerationPrompt(content);
            ModerationResponse response = moderationModel.call(prompt);

            Moderation moderation = response.getResult().getOutput();
            ModerationResult result = moderation.getResults().get(0);

            return result.isFlagged();

        } catch (Exception e) {
            log.error("Moderation API 호출 실패: {}", e.getMessage());
            return false;
        }
    }

    public String getModerationReason(String content) {
        try {
            ModerationPrompt prompt = new ModerationPrompt(content);
            ModerationResponse response = moderationModel.call(prompt);

            Moderation moderation = response.getResult().getOutput();
            ModerationResult result = moderation.getResults().get(0);

            if (result.isFlagged()) {
                List<String> flaggedCategories = getCategories(result);
                return "OpenAI Moderation 탐지: " + String.join(", ", flaggedCategories);
            }

            return "";

        } catch (Exception e) {
            log.error("Moderation API 호출 실패: {}", e.getMessage());
            return "";
        }
    }

    private static List<String> getCategories(ModerationResult result) {
        Categories categories = result.getCategories();
        List<String> flaggedCategories = new ArrayList<>();

        if (categories.isHate()) flaggedCategories.add("Hate");
        if (categories.isHarassment()) flaggedCategories.add("Harassment");
        if (categories.isViolence()) flaggedCategories.add("Violence");
        if (categories.isSexual()) flaggedCategories.add("Sexual");
        if (categories.isSelfHarm()) flaggedCategories.add("SelfHarm");
        return flaggedCategories;
    }
}

