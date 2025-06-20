package com.archiservice.auth.service;

import com.archiservice.auth.dto.request.LoginRequestDto;
import com.archiservice.auth.dto.response.LoginResponseDto;
import com.archiservice.auth.dto.response.LogoutResponseDto;
import com.archiservice.auth.service.impl.AuthServiceImpl;
import com.archiservice.common.jwt.JwtUtil;
import com.archiservice.common.jwt.RefreshTokenService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.exception.business.InvalidPasswordException;
import com.archiservice.exception.business.InvalidTokenException;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;  // JWT는 Mock 처리
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("로그인 성공 - 사용자 존재하고 비밀번호 일치")
    void login_Success() {
        // given
        String email = "test@test.com";
        String password = "password123";
        LoginRequestDto request = new LoginRequestDto(email, password);

        User mockUser = createMockUser(1L, email, "encodedPassword");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(any())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("mock-refresh-token");

        // when
        ApiResponse<LoginResponseDto> result = authService.login(request, response);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("로그인 완료");
        assertThat(result.getData().getEmail()).isEqualTo(email);

        // 호출 검증
        verify(refreshTokenService).saveRefreshToken(1L, "mock-refresh-token");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_UserNotFound() {
        // given
        LoginRequestDto request = new LoginRequestDto("nonexist@test.com", "password");
        when(userRepository.findByEmail("nonexist@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request, response))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_InvalidPassword() {
        // given
        String email = "test@test.com";
        LoginRequestDto request = new LoginRequestDto(email, "wrongPassword");
        User mockUser = createMockUser(1L, email, "encodedPassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request, response))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그아웃 - 토큰 형식 검증")
    void logout_InvalidTokenFormat() {
        // when & then
        assertThatThrownBy(() -> authService.logout("InvalidToken"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 토큰 형식입니다");

        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String accessTokenHeader = "Bearer valid-token";
        when(jwtUtil.extractUserId("valid-token")).thenReturn(123L);

        // when
        ApiResponse<LogoutResponseDto> result = authService.logout(accessTokenHeader);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("로그아웃이 완료되었습니다");
        assertThat(result.getData().getUserId()).isEqualTo(123L);

        verify(refreshTokenService).deleteRefreshToken(123L);
    }

    private User createMockUser(Long id, String email, String password) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        when(user.getPassword()).thenReturn(password);
        return user;
    }
}
