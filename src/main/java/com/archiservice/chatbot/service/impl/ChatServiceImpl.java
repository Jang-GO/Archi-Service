package com.archiservice.chatbot.service.impl;


import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.chatbot.domain.Chat;
import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.dto.request.ChatMessageRequestDto;
import com.archiservice.chatbot.dto.type.MessageType;
import com.archiservice.chatbot.dto.type.Sender;
import com.archiservice.chatbot.repository.ChatRepository;
import com.archiservice.chatbot.service.AiService;
import com.archiservice.chatbot.service.ChatService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, ChatMessageDto> chatMessageRedisTemplate;
    private final AiService aiService;
    private final ObjectMapper objectMapper;


    @Override
    public void handleUserMessage(ChatMessageRequestDto request, AuthInfo authInfo) {
        User user = userRepository.findById(authInfo.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Chat chat = Chat.builder()
            .user(user)
            .sender(Sender.USER)
            .message(request.getContent())
            .messageType(MessageType.USER_MESSAGE)
            .build();
        Chat savedChat = chatRepository.save(chat);

        ChatMessageDto response = ChatMessageDto.fromChat(savedChat);

        String key = "chat:user:" + authInfo.getUserId();
        chatMessageRedisTemplate.opsForList().leftPush(key, response);
        chatMessageRedisTemplate.expire(key, Duration.ofHours(24));

        messagingTemplate.convertAndSendToUser(
            String.valueOf(authInfo.getUserId()),
            "/queue/chat",
            response
        );

        aiService.sendMessageToAI(savedChat, authInfo);
    }

    @Override
    public List<ChatMessageDto> loadChatHistory(Long userId, int page, int size) {
        String key = "chat:user:" + userId;

        List<Object> rawList = (List<Object>) (List<?>) chatMessageRedisTemplate.opsForList().range(key, 0, -1);

        List<ChatMessageDto> cached = rawList.stream()
            .map(obj -> objectMapper.convertValue(obj, ChatMessageDto.class))
            .toList();

        if (!cached.isEmpty()) {
            int total = cached.size();
            int from = Math.max(total - (page + 1) * size, 0);
            int to = total - page * size;

            if (from < to) {
                return cached.subList(from, to);
            }
        }

        // fallback: DB 조회 시도 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Chat> chatPage = chatRepository.findByUser_UserId(userId, pageable);

        return chatPage.stream()
            .map(ChatMessageDto::fromChat)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteChatByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        chatRepository.deleteByUser_UserId(userId);
    }
}