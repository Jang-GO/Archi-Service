package com.archiservice.user.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.archiservice.code.tagmeta.domain.TagMeta;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.jwt.JwtUtil;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagMetaService tagMetaService;
    private final JwtUtil jwtUtil;
    private final TagMetaService metaService;


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
	public String updateTendency(TendencyUpdateRequestDto request, CustomUser customUser) {
		User user = userRepository.findById(customUser.getId())
				.orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));

		Long tagCode;

		if (request.getTagCode() != null) {
	        tagCode = request.getTagCode();
	    } else if (request.getTags() != null && !request.getTags().isEmpty()) {
	    	tagCode = user.getTagCode() != null ? user.getTagCode() : 0L;

	        int[][] categories = {
	            {9, 19},
	            {20, 26},
	            {27, 33},
	            {34, 42},
	            {43, 53}
	        };

	        Set<Integer> inputBitPositions = request.getTags().stream()
	            .map(tag -> {
	                TagMeta meta = tagMetaService.findTagMetaByDescription(tag);
	                return meta != null ? meta.getBitPosition() : null;
	            })
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	        Set<Integer> clearedCategories = new HashSet<>();
	        for (int bitPos : inputBitPositions) {
	            for (int i = 0; i < categories.length; i++) {
	                if (bitPos >= categories[i][0] && bitPos <= categories[i][1]) {
	                    if (clearedCategories.add(i)) {
	                        for (int j = categories[i][0]; j <= categories[i][1]; j++) {
	                            tagCode &= ~(1L << j);
	                        }
	                    }
	                    break;
	                }
	            }
	        }

	        for (int bitPos : inputBitPositions) {
	            tagCode |= (1L << bitPos);
	        }
	    } else {
	        throw new IllegalArgumentException("태그 정보가 유효하지 않습니다.");
	    }

		user.setTagCode(tagCode);
		userRepository.save(user);

		String accessToken = jwtUtil.generateAccessToken(customUser);
		return accessToken;
	}

}
