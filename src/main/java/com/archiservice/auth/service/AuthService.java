package com.archiservice.auth.service;

import com.archiservice.auth.dto.request.LoginRequestDto;
import com.archiservice.auth.dto.response.LoginResponseDto;
import com.archiservice.auth.dto.response.LogoutResponseDto;
import com.archiservice.auth.dto.response.RefreshResponseDto;
import com.archiservice.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    ApiResponse<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response);

    ApiResponse<RefreshResponseDto> refresh(String refreshTokenHeader);

    ApiResponse<LogoutResponseDto> logout(String accessTokenHeader, HttpServletResponse response);
}
