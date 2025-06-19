package com.archiservice.chatbot.service.impl;

import com.archiservice.chatbot.domain.Chat;
import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.dto.request.TendencyImageRequestDto;
import com.archiservice.chatbot.dto.response.TendencyImageResultDto;
import com.archiservice.chatbot.dto.type.MessageType;
import com.archiservice.chatbot.dto.type.Sender;
import com.archiservice.chatbot.redis.AiImageRequestProducer;
import com.archiservice.chatbot.repository.ChatRepository;
import com.archiservice.chatbot.service.TendencyImageService;
import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.FileProcessingException;
import com.archiservice.exception.business.FileTooLargeException;
import com.archiservice.exception.business.InvalidFileExtensionException;
import com.archiservice.user.domain.User;
import com.archiservice.user.dto.request.TendencyUpdateRequestDto;
import com.archiservice.user.repository.UserRepository;
import com.archiservice.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TendencyImageServiceImpl implements TendencyImageService {

  private final SimpMessagingTemplate messagingTemplate;
  private final AiImageRequestProducer aiImageRequestProducer;
  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final RedisTemplate<String, ChatMessageDto> chatMessageRedisTemplate;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Override
  public void sendImageForAnalysis(CustomUser customUser, MultipartFile image) {

    Long userId = customUser.getId();

    String originalFilename = image.getOriginalFilename();
    String ext = StringUtils.getFilenameExtension(originalFilename);

    if (ext == null || !List.of("png", "jpg", "jpeg", "webp").contains(ext.toLowerCase())) {
      throw new InvalidFileExtensionException();
    }

    if(image.getSize() > 5 *1024 *1024) {
      throw new FileTooLargeException();
    }

    String base64Image;
    try {
      base64Image = Base64.getEncoder().encodeToString(image.getBytes());
    } catch (IOException e) {
      throw new FileProcessingException();
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Chat userChat = Chat.builder()
            .user(user)
            .sender(Sender.USER)
            .message("이미지 분석을 요청했습니다.")
            .messageType(MessageType.USER_MESSAGE)
            .build();

    Chat savedUserChat = chatRepository.save(userChat);

    String key = "chat:user:" + userId;
    chatMessageRedisTemplate.opsForList().rightPush(key, ChatMessageDto.fromChat(savedUserChat));
    chatMessageRedisTemplate.expire(key, Duration.ofHours(24));

    messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            "/queue/chat",
            ChatMessageDto.fromChat(savedUserChat)
    );

    TendencyImageRequestDto dto = TendencyImageRequestDto.of(userId.toString(), base64Image);
    aiImageRequestProducer.sendToAI(dto);
  }

  @Override
  public void handleTendencyImageResult(TendencyImageResultDto dto) {
    Long userId = Long.parseLong(dto.getUser_id());

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    String key = "chat:user:" + userId;

    ChatMessageDto summaryMsg = ChatMessageDto.ofSummary(userId, dto.getSummary());

    Chat summaryChat = Chat.builder()
            .user(user)
            .sender(Sender.BOT)
            .message(summaryMsg.getContent())
            .messageType(MessageType.IMAGE_ANALYSIS)
            .build();
    Chat savedSummary = chatRepository.save(summaryChat);

    chatMessageRedisTemplate.opsForList().rightPush(key, ChatMessageDto.fromChat(savedSummary));
    messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            "/queue/chat",
            ChatMessageDto.fromChat(savedSummary)
    );

    String rawTags = dto.getTags();
    boolean hasValidTags = rawTags != null
            && !rawTags.isBlank()
            && !rawTags.trim().equals("[]");

    if (hasValidTags) {
      List<String> tagList;
      try {
        tagList = objectMapper.readValue(rawTags, new TypeReference<List<String>>() {});
      } catch (Exception e) {
        tagList = List.of();  // fallback
      }

      if (!tagList.isEmpty()) {
        ChatMessageDto tagsMsg = ChatMessageDto.ofTags(userId, rawTags);

        Chat tagsChat = Chat.builder()
                .user(user)
                .sender(Sender.BOT)
                .message(tagsMsg.getContent())
                .messageType(MessageType.IMAGE_ANALYSIS)
                .build();
        Chat savedTags = chatRepository.save(tagsChat);

        chatMessageRedisTemplate.opsForList().rightPush(key, ChatMessageDto.fromChat(savedTags));
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/chat",
                ChatMessageDto.fromChat(savedTags)
        );

        TendencyUpdateRequestDto tendencyUpdateRequestDto = new TendencyUpdateRequestDto();
        tendencyUpdateRequestDto.setTagCodes(tagList);

        userService.updateTendency(tendencyUpdateRequestDto, new CustomUser(user));
      }
    }
    chatMessageRedisTemplate.expire(key, Duration.ofHours(24));
  }



}
