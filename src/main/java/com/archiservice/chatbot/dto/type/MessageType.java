package com.archiservice.chatbot.dto.type;

public enum MessageType {
  USER_MESSAGE,

  INFO,
  SUGGESTION,
  KEYWORD_RECOMMENDATION,
  PREFERENCE_UPDATE,
  PROACTIVE_SUGGESTION,
  GENERAL_RESPONSE,
  FILTERED_MESSAGE
}

/**
 *   [USER]
 *   USER_MESSAGE, >> 유저가 보낸 메시지
 *
 *   [AI SERVER]
 *   SUGGESTION, > "내 성향에 맞는 조합 추천해줘"
 *   KEYWORD_RECOMMENDATION >> "00와 00을 할꺼야 이거에 어울리는 멤버십 찾아줘" >> 키워드 기반 조합 추천
 *   PREFERENCE_UPDATE >> "000를 하고싶어" >> 성향 업데이트
 *   PROACTIVE_SUGGESTION >> 갱신 주기 맞춰 조합 제안
 *   GENERAL_RESPONSE >> 일반 응답
 *   FILTERED_MESSAGE >> "금칙어 감지"
 */