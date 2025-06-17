package com.archiservice.auth.dto.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RefreshResponseDto {
    private Long userId;
    private String newAccessToken;
}
