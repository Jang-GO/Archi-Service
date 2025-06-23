package com.archiservice.auth.service.impl;

import com.archiservice.auth.dto.request.LoginRequestDto;
import com.archiservice.auth.dto.response.LoginResponseDto;
import com.archiservice.auth.dto.response.LogoutResponseDto;
import com.archiservice.auth.dto.response.RefreshResponseDto;
import com.archiservice.auth.service.AuthService;
import com.archiservice.common.jwt.RefreshTokenService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.common.jwt.JwtUtil;
import com.archiservice.common.security.CustomUserDetailsService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.InvalidPasswordException;
import com.archiservice.exception.business.InvalidTokenException;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public ApiResponse<LoginResponseDto> login(LoginRequestDto loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 올바르지 않습니다.");
        }

        CustomUser customUser = new CustomUser(user);
        String accessToken = jwtUtil.generateAccessToken(customUser);
        String refreshToken = jwtUtil.generateRefreshToken(customUser);

        refreshTokenService.saveRefreshToken(user.getUserId(), refreshToken);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        LoginResponseDto dto = LoginResponseDto.of(user.getEmail(), accessToken);

        return ApiResponse.success("로그인 완료", dto);

    }

    @Override
    public ApiResponse<RefreshResponseDto> refresh(String refreshToken) {

        Long userId = jwtUtil.extractUserId(refreshToken);

        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        CustomUser customUser = customUserDetailsService.loadUserByUserId(userId);

        String newAccessToken = jwtUtil.generateAccessToken(customUser);

        RefreshResponseDto responseDto = new RefreshResponseDto(userId, newAccessToken);
        return ApiResponse.success("재발급 완료", responseDto);
    }

    @Override
    public ApiResponse<LogoutResponseDto> logout(String accessTokenHeader) {

        if (accessTokenHeader == null || !accessTokenHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("유효하지 않은 토큰 형식입니다");
        }

        String accessToken = accessTokenHeader.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(accessToken);

        refreshTokenService.deleteRefreshToken(userId);

        LogoutResponseDto dto = new LogoutResponseDto(userId);

        return ApiResponse.success("로그아웃이 완료되었습니다", dto);

    }
}
