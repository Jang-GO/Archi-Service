package com.archiservice.code.commoncode.service;

import com.archiservice.code.commoncode.domain.CommonCode;
import com.archiservice.code.commoncode.repository.CommonCodeRepository;
import com.archiservice.code.commoncode.service.impl.CommonCodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommonCodeServiceImplTest {

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @InjectMocks
    private CommonCodeServiceImpl commonCodeService;

    @Test
    @DisplayName("어플리케이션 시작 시 공통코드 캐시 로드")
    void loadAllCommonCodes_ShouldLoadCacheFromDatabase() {
        // given
        List<CommonCode> mockCommonCodes = List.of(
                createCommonCode("GROUP1", "CODE1", "이름1"),
                createCommonCode("GROUP1", "CODE2", "이름2"),
                createCommonCode("GROUP2", "CODE3", "이름3")
        );
        when(commonCodeRepository.findAll()).thenReturn(mockCommonCodes);

        // when
        commonCodeService.loadAllCommonCodes();

        // then
        String result1 = commonCodeService.getCodeName("GROUP1", "CODE1");
        String result2 = commonCodeService.getCodeName("GROUP2", "CODE3");
        
        assertThat(result1).isEqualTo("이름1");
        assertThat(result2).isEqualTo("이름3");
    }

    @Test
    @DisplayName("캐시에서 코드명 조회 성공")
    void getCodeName_ShouldReturnNameFromCache() {
        // given
        setupCache();

        // when
        String result = commonCodeService.getCodeName("USER_TYPE", "ADMIN");

        // then
        assertThat(result).isEqualTo("관리자");
    }

    @Test
    @DisplayName("캐시에 없는 그룹코드로 조회 시 원본 코드 반환")
    void getCodeName_ShouldReturnOriginalCodeWhenGroupNotFound() {
        // given
        setupCache();

        // when
        String result = commonCodeService.getCodeName("NONEXISTENT_GROUP", "SOME_CODE");

        // then
        assertThat(result).isEqualTo("SOME_CODE");
    }

    @Test
    @DisplayName("캐시에 없는 공통코드로 조회 시 원본 코드 반환")
    void getCodeName_ShouldReturnOriginalCodeWhenCodeNotFound() {
        // given
        setupCache();

        // when
        String result = commonCodeService.getCodeName("USER_TYPE", "NONEXISTENT_CODE");

        // then
        assertThat(result).isEqualTo("NONEXISTENT_CODE");
    }

    @Test
    @DisplayName("캐시에 없으면 DB에서 조회")
    void getCode_ShouldFallbackToDatabase() {
        // given
        setupCache();
        when(commonCodeRepository.findCommonCodeByGroupCodeAndCommonName("STATUS", "비활성"))
                .thenReturn(Optional.of("INACTIVE"));

        // when
        String result = commonCodeService.getCode("STATUS", "비활성");

        // then
        assertThat(result).isEqualTo("INACTIVE");
        verify(commonCodeRepository).findCommonCodeByGroupCodeAndCommonName("STATUS", "비활성");
    }

    @Test
    @DisplayName("캐시와 DB 모두에 없으면 null 반환")
    void getCode_ShouldReturnNullWhenNotFoundAnywhere() {
        // given
        setupCache();
        when(commonCodeRepository.findCommonCodeByGroupCodeAndCommonName("STATUS", "존재하지않음"))
                .thenReturn(Optional.empty());

        // when
        String result = commonCodeService.getCode("STATUS", "존재하지않음");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 캐시 상태에서 DB 조회")
    void getCode_ShouldQueryDatabaseWhenCacheEmpty() {
        // given
        when(commonCodeRepository.findCommonCodeByGroupCodeAndCommonName("GROUP1", "이름1"))
                .thenReturn(Optional.of("CODE1"));

        // when
        String result = commonCodeService.getCode("GROUP1", "이름1");

        // then
        assertThat(result).isEqualTo("CODE1");
    }

    private void setupCache() {
        List<CommonCode> mockCommonCodes = List.of(
                createCommonCode("USER_TYPE", "ADMIN", "관리자"),
                createCommonCode("USER_TYPE", "USER", "일반사용자"),
                createCommonCode("STATUS", "Y", "사용"),
                createCommonCode("STATUS", "N", "미사용")
        );
        when(commonCodeRepository.findAll()).thenReturn(mockCommonCodes);
        commonCodeService.loadAllCommonCodes();
    }

    private void setupReverseCache() {
        List<CommonCode> mockCommonCodes = List.of(
                createCommonCode("STATUS", "ACTIVE", "활성"),
                createCommonCode("STATUS", "INACTIVE", "비활성")
        );
        when(commonCodeRepository.findAll()).thenReturn(mockCommonCodes);
        commonCodeService.loadAllCommonCodes();
    }

    private CommonCode createCommonCode(String groupCode, String commonCode, String commonName) {
        CommonCode code = mock(CommonCode.class);
        when(code.getGroupCode()).thenReturn(groupCode);
        when(code.getCommonCode()).thenReturn(commonCode);
        when(code.getCommonName()).thenReturn(commonName);
        return code;
    }
}