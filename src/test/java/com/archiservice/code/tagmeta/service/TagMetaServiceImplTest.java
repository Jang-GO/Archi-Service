package com.archiservice.code.tagmeta.service;

import com.archiservice.code.tagmeta.domain.TagMeta;
import com.archiservice.code.tagmeta.domain.id.TagMetaId;
import com.archiservice.code.tagmeta.repository.TagMetaRepository;
import com.archiservice.code.tagmeta.service.impl.TagMetaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TagMetaServiceImplTest {

    @Mock
    private TagMetaRepository tagMetaRepository;

    @InjectMocks
    private TagMetaServiceImpl tagMetaService;

    @Test
    @DisplayName("어플리케이션 시작 시 태그 메타 캐시 로드")
    void loadAllTagMetas_ShouldLoadCacheFromDatabase() {
        // given
        List<TagMeta> mockTagMetas = List.of(
                createTagMeta("FOOD", "음식", 0),
                createTagMeta("TRAVEL", "여행", 1),
                createTagMeta("SPORTS", "스포츠", 2)
        );
        when(tagMetaRepository.findAll()).thenReturn(mockTagMetas);

        // when
        tagMetaService.loadAllTagMetas();

        // then
        TagMeta result = tagMetaService.findTagMetaByKey("food");
        assertThat(result).isNotNull();
        assertThat(result.getTagDescription()).isEqualTo("음식");
    }

    @Test
    @DisplayName("태그 코드에서 태그 목록 추출 성공")
    void extractTagsFromCode_ShouldReturnTagDescriptions() {
        // given
        setupCache();
        Long tagCode = 5L; // 101 (이진수) = 0번째, 2번째 비트

        // when
        List<String> result = tagMetaService.extractTagsFromCode(tagCode);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("음식", "스포츠");
    }

    @Test
    @DisplayName("태그 코드가 null이면 빈 리스트 반환")
    void extractTagsFromCode_ShouldReturnEmptyListWhenTagCodeIsNull() {
        // when
        List<String> result = tagMetaService.extractTagsFromCode(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("태그 코드가 0이면 빈 리스트 반환")
    void extractTagsFromCode_ShouldReturnEmptyListWhenTagCodeIsZero() {
        // when
        List<String> result = tagMetaService.extractTagsFromCode(0L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("캐시에 없는 비트 포지션은 필터링")
    void extractTagsFromCode_ShouldFilterNonExistentBitPositions() {
        // given
        setupCache();
        Long tagCode = 8L; // 1000 (이진수) = 3번째 비트 (캐시에 없음)

        // when
        List<String> result = tagMetaService.extractTagsFromCode(tagCode);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("태그 키로 태그 코드 계산 성공")
    void calculateTagCodeFromKey_ShouldReturnCorrectTagCode() {
        // given
        setupCache();
        List<String> tagKeys = List.of("FOOD", "SPORTS");

        // when
        Long result = tagMetaService.calculateTagCodeFromKey(tagKeys);

        // then
        assertThat(result).isEqualTo(5L); // 2^0 + 2^2 = 1 + 4 = 5
    }

    @Test
    @DisplayName("단일 태그 키로 태그 코드 계산")
    void calculateTagCodeFromKey_ShouldWorkWithSingleTag() {
        // given
        setupCache();
        List<String> tagKeys = List.of("TRAVEL");

        // when
        Long result = tagMetaService.calculateTagCodeFromKey(tagKeys);

        // then
        assertThat(result).isEqualTo(2L); // 2^1 = 2
    }

    @Test
    @DisplayName("태그 키 리스트가 null이면 예외 발생")
    void calculateTagCodeFromKey_ShouldThrowExceptionWhenTagKeysIsNull() {
        // when & then
        assertThatThrownBy(() -> tagMetaService.calculateTagCodeFromKey(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("태그 리스트가 비어 있습니다.");
    }

    @Test
    @DisplayName("태그 키 리스트가 비어있으면 예외 발생")
    void calculateTagCodeFromKey_ShouldThrowExceptionWhenTagKeysIsEmpty() {
        // when & then
        assertThatThrownBy(() -> tagMetaService.calculateTagCodeFromKey(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("태그 리스트가 비어 있습니다.");
    }

    @Test
    @DisplayName("유효한 태그가 없으면 예외 발생")
    void calculateTagCodeFromKey_ShouldThrowExceptionWhenNoValidTags() {
        // given
        setupCache();
        List<String> tagKeys = List.of("NONEXISTENT1", "NONEXISTENT2");

        // when & then
        assertThatThrownBy(() -> tagMetaService.calculateTagCodeFromKey(tagKeys))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효한 태그가 하나도 없습니다.");
    }

    @Test
    @DisplayName("일부만 유효한 태그가 있어도 정상 계산")
    void calculateTagCodeFromKey_ShouldWorkWithPartialValidTags() {
        // given
        setupCache();
        List<String> tagKeys = List.of("FOOD", "NONEXISTENT", "TRAVEL");

        // when
        Long result = tagMetaService.calculateTagCodeFromKey(tagKeys);

        // then
        assertThat(result).isEqualTo(3L); // 2^0 + 2^1 = 1 + 2 = 3
    }

    @Test
    @DisplayName("태그 키로 태그 메타 조회 성공 (대소문자 무관)")
    void findTagMetaByKey_ShouldReturnTagMetaCaseInsensitive() {
        // given
        setupCache();

        // when
        TagMeta result1 = tagMetaService.findTagMetaByKey("food");
        TagMeta result2 = tagMetaService.findTagMetaByKey("FOOD");
        TagMeta result3 = tagMetaService.findTagMetaByKey("Food");

        // then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result1.getTagDescription()).isEqualTo("음식");
        assertThat(result2.getTagDescription()).isEqualTo("음식");
        assertThat(result3.getTagDescription()).isEqualTo("음식");
    }

    @Test
    @DisplayName("존재하지 않는 태그 키 조회 시 null 반환")
    void findTagMetaByKey_ShouldReturnNullWhenKeyNotFound() {
        // given
        setupCache();

        // when
        TagMeta result = tagMetaService.findTagMetaByKey("NONEXISTENT");

        // then
        assertThat(result).isNull();
    }

    private void setupCache() {
        List<TagMeta> mockTagMetas = List.of(
                createTagMeta("FOOD", "음식", 0),
                createTagMeta("TRAVEL", "여행", 1),
                createTagMeta("SPORTS", "스포츠", 2)
        );
        when(tagMetaRepository.findAll()).thenReturn(mockTagMetas);
        tagMetaService.loadAllTagMetas();
    }

    private TagMeta createTagMeta(String tagKey, String description, int bitPosition) {
        TagMeta tagMeta = mock(TagMeta.class);
        TagMetaId id = mock(TagMetaId.class);

        when(id.getTagType()).thenReturn("DEFAULT_TYPE"); // tagType 추가
        when(id.getTagKey()).thenReturn(tagKey);
        when(tagMeta.getId()).thenReturn(id);
        when(tagMeta.getTagDescription()).thenReturn(description);
        when(tagMeta.getBitPosition()).thenReturn(bitPosition);

        return tagMeta;
    }
}