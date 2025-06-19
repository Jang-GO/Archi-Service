package com.archiservice.chatbot.dto.response;

import com.archiservice.chatbot.dto.ChatMessageDto;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ChatResponseDto extends ChatMessageDto {
  // TODO: 추후 BundleRecommendation 클래스로 변경
  private String bundleRecommendations; // SUGGESTION용
  
  // TODO: 추후 List<String>으로 변경
  private String extractedKeywords; // KEYWORD_RECOMMENDATION용

  // TODO: 추후 UserPreference 객체로 변경
  private String updatedPreference; // PREFERENCE_UPDATE용
  //필드 1: private String summary
  //필드 2: private String tags
}

/**
 * ChatResponseDto 목적:
 * AI 서버에서 받은 JSON 응답을 파싱하기 위한 DTO
 *
 * 공통 필드: ChatMessageDto 상속 (messageId, userId, content 등)
 * 타입별 추가 데이터:
 *
 * bundleRecommendations: 조합 추천 시 번들 정보
 * extractedKeywords: 키워드 기반 추천 시 추출된 키워드
 * updatedPreference: 성향 업데이트 시 새로운 성향값
 *
 * 이후 확정 후
 *
 * Redis Stream에서 받은 JSON을 이 DTO로 변환 후 타입별 분기 처리에 사용
 */