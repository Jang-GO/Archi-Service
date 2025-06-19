package com.archiservice.chatbot.service.impl;

import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.dto.request.TendencyImageRequestDto;
import com.archiservice.chatbot.dto.response.TendencyImageResultDto;
import com.archiservice.chatbot.redis.AiImageRequestProducer;
import com.archiservice.chatbot.repository.ChatRepository;
import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.FileProcessingException;
import com.archiservice.exception.business.FileTooLargeException;
import com.archiservice.exception.business.InvalidFileExtensionException;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TendencyImageService {
  private final SimpMessagingTemplate messagingTemplate;
  private final AiImageRequestProducer aiImageRequestProducer;
  private ChatRepository chatRepository;

  public void sendImageForAnalysis(CustomUser customUser, MultipartFile image) {
    // Id 뽑아내기
    Long userId = customUser.getId();

    /**
     * 로직 : 파일 검토, 메시지 프로듀서 전달
     */

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

    // 인공지능 서버에 전달
    TendencyImageRequestDto dto = TendencyImageRequestDto.of(userId.toString(),base64Image);

    aiImageRequestProducer.sendToAI(dto);

    // 프론트에 전달(디비 저장은 아닌데 레디스에는 일단 저장)

    ChatMessageDto infoMessage = ChatMessageDto.infoMessage(
        userId,
        "사진 분석이 시작되었습니다"
    );

//    messagingTemplate.convertAndSendToUser(
//        String.valueOf(userId),
//        "/queue/chat",
//        infoMessage
//    );
  }

  // TODO: 메시지 2개 생성 (summary + tags)
  // Ex : ChatMessageDto summaryMsg = ChatMessageDto.ofSummary(userId, summary);
  //      ChatMessageDto tagsMsg = ChatMessageDto.ofTags(userId, tags);
  // TODO: ChatMessageDto 변환 및 Redis 저장 (history)
  // repo.save
  // redis.save
  // TODO: WebSocket 전송
  //messageTemplate~

  public void handleTendencyImageResult(TendencyImageResultDto dto) {
    Long userId = Long.parseLong(dto.getUser_id());

    // 1) ChatMessageDto 두 개 생성 (요약, 태그)
    ChatMessageDto summaryMsg = ChatMessageDto.ofSummary(userId, dto.getSummary());

    System.out.println("[TendencyImageResult] 수신된 tags: {}" + dto.getTags());

    ChatMessageDto tagsMsg = ChatMessageDto.ofTags(userId, dto.getTags());

    // 2) DB 저장 (선택)
//    chatRepository.save(summaryMsg.toEntity());
//    chatRepository.save(tagsMsg.toEntity());

    // 3) Redis 캐시 저장 (선택)
//    chatRedisService.cacheChat(userId, summaryMsg);
//    chatRedisService.cacheChat(userId, tagsMsg);

    // 4) WebSocket 전송
    messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            "/queue/chat",
            summaryMsg
    );
    messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            "/queue/chat",
            tagsMsg
    );

  }

  //메시지 dto 에 맞게 변환?
  // ChatMessageDto
}
