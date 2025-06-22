package com.archiservice.auth.controller;

import com.archiservice.auth.dto.response.LoginResponseDto;
import com.archiservice.auth.dto.response.LogoutResponseDto;
import com.archiservice.auth.dto.response.RefreshResponseDto;
import com.archiservice.auth.service.AuthService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.auth.dto.request.LoginRequestDto;
import com.archiservice.common.security.CustomUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refresh(@CookieValue("refreshToken") String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponseDto>> logout(@RequestHeader("Authorization") String accessToken) {
        return ResponseEntity.ok(authService.logout(accessToken));
    }

    @GetMapping("/hash/{password}")
    public String generateHash(@PathVariable String password) {
        return passwordEncoder.encode(password);
    }
}
