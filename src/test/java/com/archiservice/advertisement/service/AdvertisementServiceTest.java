package com.archiservice.advertisement.service;

import com.archiservice.advertisement.dto.request.BannerClickRequest;
import com.archiservice.advertisement.service.impl.AdvertisementService;
import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.chatbot.dto.request.ChatMessageRequestDto;
import com.archiservice.chatbot.service.ChatService;
import com.archiservice.common.security.CustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private AdvertisementService advertisementService;

    @Test
    @DisplayName("배너 클릭 시 올바른 파라미터로 chatService 호출")
    void handleBannerClick_ShouldCallChatServiceWithCorrectParameters() {
        // given
        CustomUser customUser = createMockCustomUser(123L, "20", 1000L);
        BannerClickRequest request = new BannerClickRequest(1L,"프리미엄 플랜");

        // when
        advertisementService.handleBannerClick(customUser, request);

        // then - chatService.handleUserMessage 호출 검증
        ArgumentCaptor<ChatMessageRequestDto> dtoCaptor = ArgumentCaptor.forClass(ChatMessageRequestDto.class);
        ArgumentCaptor<AuthInfo> authInfoCaptor = ArgumentCaptor.forClass(AuthInfo.class);

        verify(chatService).handleUserMessage(dtoCaptor.capture(), authInfoCaptor.capture());

        // 전달된 DTO 검증
        ChatMessageRequestDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getContent()).isEqualTo("부가서비스 프리미엄 플랜 알려줘");

        // 전달된 AuthInfo 검증
        AuthInfo capturedAuthInfo = authInfoCaptor.getValue();
        assertThat(capturedAuthInfo.getUserId()).isEqualTo(123L);
        assertThat(capturedAuthInfo.getAgeCode()).isEqualTo("20");
        assertThat(capturedAuthInfo.getTagCode()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("다른 VAS 이름으로 배너 클릭 테스트")
    void handleBannerClick_WithDifferentVasName() {
        // given
        CustomUser customUser = createMockCustomUser(456L, "30", 2000L);
        BannerClickRequest request = new BannerClickRequest(1L, "데이터 충전");

        // when
        advertisementService.handleBannerClick(customUser, request);

        // then
        ArgumentCaptor<ChatMessageRequestDto> dtoCaptor = ArgumentCaptor.forClass(ChatMessageRequestDto.class);

        verify(chatService).handleUserMessage(dtoCaptor.capture(), any(AuthInfo.class));

        ChatMessageRequestDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getContent()).isEqualTo("부가서비스 데이터 충전 알려줘");
    }

    @Test
    @DisplayName("chatService 호출 횟수 검증")
    void handleBannerClick_ShouldCallChatServiceOnce() {
        // given
        CustomUser customUser = createMockCustomUser(789L, "40", 3000L);
        BannerClickRequest request = new BannerClickRequest(1L, "무제한 통화");

        // when
        advertisementService.handleBannerClick(customUser, request);

        // then - 정확히 1번만 호출되는지 검증
        verify(chatService, times(1)).handleUserMessage(any(), any());
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void handleBannerClick_WithNullVasName_ShouldHandleGracefully() {
        // given
        CustomUser customUser = createMockCustomUser(999L, "50", 4000L);
        BannerClickRequest request = new BannerClickRequest(null, null);

        // when & then - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            advertisementService.handleBannerClick(customUser, request);
        });

        // chatService는 여전히 호출되어야 함
        verify(chatService).handleUserMessage(any(), any());
    }

    private CustomUser createMockCustomUser(Long id, String ageCode, Long tagCode) {
        CustomUser mockUser = mock(CustomUser.class);
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getAgeCode()).thenReturn(ageCode);
        when(mockUser.getTagCode()).thenReturn(tagCode);
        return mockUser;
    }
}
