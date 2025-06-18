package com.archiservice.chatbot.dto.response;

/**
 * MessageDto + content(summary  -> Stirng, String -> tags -> 스트링, 스트링, 스트링, -> 여기서 파싱]
 */
public class TendencyImageResponseDto {

}

/**
 *
 * public class ChatMessageDto {
 *   private String messageId;
 *   private Long userId;
 *   private String content;
 *   private MessageType type;
 *   private Sender sender;
 *   private LocalDateTime timestamp;
 *
 *   public static ChatMessageDto fromChat(Chat chat) {
 *     return ChatMessageDto.builder()
 *         .messageId(chat.getChatId().toString()) // 저장 아이디
 *         .userId(chat.getUser().getUserId()) // 유저 아이디
 *         .content(chat.getMessage()) // 분석이 완료되었습니다. (_) (summary  -> Stirng,  //  Stirng, String -> tags -> 스트링, 스트링, 스트링, -> 여기서 파싱
 *         .type(chat.getMessageType()) // 타입 새로 만들기  -> 타입 -> 이걸로 프론트에서 -> 태그들 -> 꾸민다
 *         .sender(chat.getSender()) // BOT
 *         .timestamp(chat.getCreatedAt()) //
 *         .build();
 *   }
 * }
 *
 * 메시지 답변이 두 개
 *
 * 여기서는 1요청에 2답변
 *
 *
 * 채팅 MessageDto + content(summary  -> Stirng, String -> tags -> 스트링, 스트링, 스트링, -> 여기서 파싱]
 *
 *-> 나눠서
 */
