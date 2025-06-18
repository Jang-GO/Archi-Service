package com.archiservice.user.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.user.dto.request.PasswordUpdateRequestDto;
import com.archiservice.user.dto.request.TendencyUpdateRequestDto;
import com.archiservice.user.dto.response.ProfileResponseDto;
import com.archiservice.user.dto.response.TendencyResponseDto;

import java.util.List;

public interface UserService {
    ProfileResponseDto getUserProfile(CustomUser user);
    void updatePassword(PasswordUpdateRequestDto request, CustomUser user);
    List<TendencyResponseDto> getUserTendency(CustomUser user);
    TendencyResponseDto updateTendency(TendencyUpdateRequestDto request, CustomUser user);
}
