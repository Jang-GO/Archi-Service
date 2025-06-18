package com.archiservice.chatbot.service.impl;

import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.dto.request.TendencyImageRequestDto;
import com.archiservice.chatbot.redis.AiImageRequestProducer;
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
    TendencyImageRequestDto dto = TendencyImageRequestDto.of(userId,base64Image);

    aiImageRequestProducer.sendToAI(dto);

    // 프론트에 전달(디비 저장은 아닌데 레디스에는 일단 저장)

    ChatMessageDto infoMessage = ChatMessageDto.infoMessage(
        userId,
        "사진 분석이 시작되었습니다"
    );

    messagingTemplate.convertAndSendToUser(
        String.valueOf(userId),
        "/queue/chat",
        infoMessage
    );



  }

  //메시지 dto 에 맞게 변환?
  // ChatMessageDto
}
