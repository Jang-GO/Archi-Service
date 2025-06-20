package com.archiservice.user.service.impl;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TagMetaService tagMetaService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_ShouldReturnProfileResponseDto() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // when
        ProfileResponseDto result = userService.getUserProfile(customUser);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 프로필 조회 시 예외 발생")
    void getUserProfile_ShouldThrowExceptionWhenUserNotFound() {
        // given
        CustomUser customUser = createMockCustomUser(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(customUser))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("올바른 사용자 정보를 가져오지 못했습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_ShouldUpdatePasswordSuccessfully() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        PasswordUpdateRequestDto request = createPasswordUpdateRequest("oldPassword", "newPassword");
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // when
        userService.updatePassword(request, customUser);

        // then
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(mockUser).setPassword("encodedNewPassword");
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("잘못된 기존 비밀번호로 변경 시 예외 발생")
    void updatePassword_ShouldThrowExceptionWhenWrongPassword() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        PasswordUpdateRequestDto request = createPasswordUpdateRequest("wrongPassword", "newPassword");
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(request, customUser))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
        
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자 성향 조회 성공")
    void getUserTendency_ShouldReturnTendencyList() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        when(mockUser.getTagCode()).thenReturn(7L); // 예시 태그 코드
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tagMetaService.extractTagsFromCode(7L)).thenReturn(List.of("음식", "여행", "스포츠"));

        // when
        List<TendencyResponseDto> result = userService.getUserTendency(customUser);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTagDescription()).isEqualTo("음식");
        assertThat(result.get(1).getTagDescription()).isEqualTo("여행");
        assertThat(result.get(2).getTagDescription()).isEqualTo("스포츠");
    }

    @Test
    @DisplayName("성향 업데이트 - tagCode로 업데이트")
    void updateTendency_ShouldUpdateWithTagCode() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        TendencyUpdateRequestDto request = createTendencyUpdateRequest(15L, null);
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(customUser)).thenReturn("new-access-token");

        // when
        String result = userService.updateTendency(request, customUser);

        // then
        verify(mockUser).setTagCode(15L);
        verify(userRepository).save(mockUser);
        assertThat(result).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("성향 업데이트 - tags 리스트로 업데이트")
    void updateTendency_ShouldUpdateWithTagsList() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        TendencyUpdateRequestDto request = createTendencyUpdateRequest(null, List.of("FOOD", "TRAVEL"));
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        when(mockUser.getTagCode()).thenReturn(0L);
        
        TagMeta foodMeta = createMockTagMeta("FOOD", 10);
        TagMeta travelMeta = createMockTagMeta("TRAVEL", 21);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tagMetaService.findTagMetaByKey("FOOD")).thenReturn(foodMeta);
        when(tagMetaService.findTagMetaByKey("TRAVEL")).thenReturn(travelMeta);
        when(jwtUtil.generateAccessToken(customUser)).thenReturn("new-access-token");

        // when
        String result = userService.updateTendency(request, customUser);

        // then
        verify(mockUser).setTagCode(anyLong()); // 비트 연산 결과
        verify(userRepository).save(mockUser);
        assertThat(result).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("성향 업데이트 - 유효하지 않은 태그 정보로 예외 발생")
    void updateTendency_ShouldThrowExceptionWhenInvalidTagInfo() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        TendencyUpdateRequestDto request = createTendencyUpdateRequest(null, null);
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userService.updateTendency(request, customUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("태그 정보가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("성향 업데이트 - 빈 태그 리스트로 예외 발생")
    void updateTendency_ShouldThrowExceptionWhenEmptyTagsList() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        TendencyUpdateRequestDto request = createTendencyUpdateRequest(null, List.of());
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userService.updateTendency(request, customUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("태그 정보가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 태그로 성향 업데이트")
    void updateTendency_ShouldHandleNonExistentTags() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        TendencyUpdateRequestDto request = createTendencyUpdateRequest(null, List.of("NONEXISTENT", "FOOD"));
        User mockUser = createMockUser(1L, "test@test.com", "홍길동");
        when(mockUser.getTagCode()).thenReturn(0L);
        
        TagMeta foodMeta = createMockTagMeta("FOOD", 10);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tagMetaService.findTagMetaByKey("NONEXISTENT")).thenReturn(null);
        when(tagMetaService.findTagMetaByKey("FOOD")).thenReturn(foodMeta);
        when(jwtUtil.generateAccessToken(customUser)).thenReturn("new-access-token");

        // when
        String result = userService.updateTendency(request, customUser);

        // then
        verify(mockUser).setTagCode(anyLong()); // 유효한 태그만 적용
        verify(userRepository).save(mockUser);
        assertThat(result).isEqualTo("new-access-token");
    }

    private CustomUser createMockCustomUser(Long id) {
        CustomUser user = mock(CustomUser.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private User createMockUser(Long id, String email, String username) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);
        when(user.getPassword()).thenReturn("encodedOldPassword");
        when(user.getTagCode()).thenReturn(0L);
        return user;
    }

    private PasswordUpdateRequestDto createPasswordUpdateRequest(String password, String newPassword) {
        PasswordUpdateRequestDto request = mock(PasswordUpdateRequestDto.class);
        when(request.getPassword()).thenReturn(password);
        when(request.getNewPassword()).thenReturn(newPassword);
        return request;
    }

    private TendencyUpdateRequestDto createTendencyUpdateRequest(Long tagCode, List<String> tags) {
        TendencyUpdateRequestDto request = mock(TendencyUpdateRequestDto.class);
        when(request.getTagCode()).thenReturn(tagCode);
        when(request.getTags()).thenReturn(tags);
        return request;
    }

    private TagMeta createMockTagMeta(String tagKey, int bitPosition) {
        TagMeta tagMeta = mock(TagMeta.class);
        when(tagMeta.getBitPosition()).thenReturn(bitPosition);
        return tagMeta;
    }
}