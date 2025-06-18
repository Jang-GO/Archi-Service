package com.archiservice.user.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.jwt.JwtUtil;
import com.archiservice.common.jwt.RefreshTokenService;
import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.business.InvalidPasswordException;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.user.domain.User;
import com.archiservice.user.dto.request.PasswordUpdateRequestDto;
import com.archiservice.user.dto.request.TendencyUpdateRequestDto;
import com.archiservice.user.dto.response.ProfileResponseDto;
import com.archiservice.user.dto.response.TendencyResponseDto;
import com.archiservice.user.repository.UserRepository;
import com.archiservice.user.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagMetaService tagMetaService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public ProfileResponseDto getUserProfile(CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));

        ProfileResponseDto profileResponseDto = ProfileResponseDto.from(user);

        return profileResponseDto;
    }

    @Override
    public void updatePassword(PasswordUpdateRequestDto request, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<TendencyResponseDto> getUserTendency(CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));

        long tagCode = user.getTagCode();
        List<String> tags = new ArrayList<>();
        tags = tagMetaService.extractTagsFromCode(tagCode);

        List<TendencyResponseDto> tendencies = new ArrayList<>();
        for(int i =0; i < tags.size(); i++){
            TendencyResponseDto tendencyResponseDto = TendencyResponseDto.builder()
                    .tagDescription(tags.get(i))
                    .build();

            tendencies.add(tendencyResponseDto);
        }

        return tendencies;
    }

	@Override
	public void updateTendency(TendencyUpdateRequestDto request, CustomUser customUser, HttpServletResponse response) {
		User user = userRepository.findById(customUser.getId())
				.orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));
		
		Long tagCode = request.getNewTagCode();
		user.setTagCode(tagCode);
		userRepository.save(user);
		
		String accessToken = jwtUtil.generateAccessToken(customUser);
        String refreshToken = jwtUtil.generateRefreshToken(customUser);
        
        refreshTokenService.saveRefreshToken(user.getUserId(), refreshToken);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
	}
    
    
}
