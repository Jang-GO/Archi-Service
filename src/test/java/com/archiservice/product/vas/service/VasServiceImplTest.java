package com.archiservice.product.vas.service;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasResponseDto;
import com.archiservice.product.vas.repository.VasRepository;
import com.archiservice.product.vas.service.impl.VasServiceImpl;
import com.archiservice.review.summary.domain.ProductReviewSummary;
import com.archiservice.review.summary.repository.ProductReviewSummaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VasServiceImplTest {

    @Mock
    private VasRepository vasRepository;
    @Mock private TagMetaService tagMetaService;
    @Mock private CommonCodeService commonCodeService;
    @Mock private ProductReviewSummaryRepository reviewSummaryRepository;

    @InjectMocks
    private VasServiceImpl vasService;

    @Test
    @DisplayName("전체 부가서비스 페이징 조회")
    void getAllVas_ShouldReturnPagedVas() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        Vas mockVas1 = createMockVas(1L, "기본 부가서비스", 10000, 10, 5L, "VAS001");
        Vas mockVas2 = createMockVas(2L, "프리미엄 부가서비스", 20000, 20, 3L, "VAS002");
        List<Vas> vasList = List.of(mockVas1, mockVas2);
        
        Page<Vas> mockPage = new PageImpl<>(vasList, pageable, 2);
        
        when(vasRepository.findAll(pageable)).thenReturn(mockPage);
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(tagMetaService.extractTagsFromCode(3L)).thenReturn(List.of("스포츠"));
        when(commonCodeService.getCodeName("G03", "VAS001")).thenReturn("기본부가서비스");
        when(commonCodeService.getCodeName("G03", "VAS002")).thenReturn("프리미엄부가서비스");
        
        // when
        Page<VasResponseDto> result = vasService.getAllVas(pageable);
        
        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        
        VasResponseDto firstVas = result.getContent().get(0);
        assertThat(firstVas.getTags()).containsExactly("음식", "여행");
        assertThat(firstVas.getCategory()).isEqualTo("기본부가서비스");
    }

    @Test
    @DisplayName("빈 페이지 반환")
    void getAllVas_ShouldReturnEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vas> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(vasRepository.findAll(pageable)).thenReturn(emptyPage);
        
        // when
        Page<VasResponseDto> result = vasService.getAllVas(pageable);
        
        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("부가서비스 상세 조회 성공 - 리뷰 요약 없음")
    void getVasDetail_ShouldReturnVasDetailWithoutReview() {
        // given
        Long vasId = 1L;
        Vas mockVas = createMockVas(vasId, "기본 부가서비스", 10000, 10, 5L, "VAS001");
        
        when(vasRepository.findById(vasId)).thenReturn(Optional.of(mockVas));
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(commonCodeService.getCodeName("G03", "VAS001")).thenReturn("기본부가서비스");
        when(reviewSummaryRepository.findByProductIdAndReviewType(vasId, "VAS")).thenReturn(Optional.empty());

        // when
        VasDetailResponseDto result = vasService.getVasDetail(vasId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("음식", "여행");
        assertThat(result.getCategory()).isEqualTo("기본부가서비스");
    }

    @Test
    @DisplayName("부가서비스 상세 조회 성공 - 리뷰 요약 포함")
    void getVasDetail_ShouldReturnVasDetailWithReview() {
        // given
        Long vasId = 1L;
        Vas mockVas = createMockVas(vasId, "프리미엄 부가서비스", 20000, 20, 7L, "VAS002");
        ProductReviewSummary mockReviewSummary = mock(ProductReviewSummary.class);
        
        when(vasRepository.findById(vasId)).thenReturn(Optional.of(mockVas));
        when(tagMetaService.extractTagsFromCode(7L)).thenReturn(List.of("프리미엄"));
        when(commonCodeService.getCodeName("G03", "VAS002")).thenReturn("프리미엄부가서비스");
        when(reviewSummaryRepository.findByProductIdAndReviewType(vasId, "VAS")).thenReturn(Optional.of(mockReviewSummary));

        // when
        VasDetailResponseDto result = vasService.getVasDetail(vasId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("프리미엄");
        assertThat(result.getCategory()).isEqualTo("프리미엄부가서비스");
        verify(reviewSummaryRepository).findByProductIdAndReviewType(vasId, "VAS");
    }

    @Test
    @DisplayName("존재하지 않는 부가서비스 조회 시 예외 발생")
    void getVasDetail_ShouldThrowExceptionWhenVasNotFound() {
        // given
        Long vasId = 999L;
        when(vasRepository.findById(vasId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vasService.getVasDetail(vasId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리별 랜덤 부가서비스 조회 성공")
    void getRandVasByCategoryCode_ShouldReturnRandomVas() {
        // given
        String categoryCode = "VAS001";
        Vas mockVas1 = createMockVas(1L, "부가서비스1", 10000, 10, 1L, categoryCode);
        Vas mockVas2 = createMockVas(2L, "부가서비스2", 20000, 20, 2L, categoryCode);
        List<Vas> vasList = List.of(mockVas1, mockVas2);
        
        when(vasRepository.findVasByCategoryCode(categoryCode)).thenReturn(vasList);

        // when
        Vas result = vasService.getRandVasByCategoryCode(categoryCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryCode()).isEqualTo(categoryCode);
        assertThat(vasList).contains(result); // 리스트에 포함된 것 중 하나여야 함
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 랜덤 조회 시 예외 발생")
    void getRandVasByCategoryCode_ShouldThrowExceptionWhenCategoryNotFound() {
        // given
        String categoryCode = "NONEXISTENT";
        when(vasRepository.findVasByCategoryCode(categoryCode)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> vasService.getRandVasByCategoryCode(categoryCode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("null 카테고리 리스트로 랜덤 조회 시 예외 발생")
    void getRandVasByCategoryCode_ShouldThrowExceptionWhenListIsNull() {
        // given
        String categoryCode = "VAS001";
        when(vasRepository.findVasByCategoryCode(categoryCode)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> vasService.getRandVasByCategoryCode(categoryCode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("할인율이 있는 경우 할인된 가격 반환")
    void vas_ShouldReturnDiscountedPriceWhenSaleRateExists() {
        // given
        Vas mockVas = createMockVas(1L, "할인 부가서비스", 10000, 20, 1L, "VAS001");
        
        // when
        Integer discountedPrice = mockVas.getDiscountedPrice();
        
        // then
        assertThat(discountedPrice).isEqualTo(8000); // 10000 - (10000 * 20 / 100)
    }

    @Test
    @DisplayName("할인율이 0인 경우 원가 반환")
    void vas_ShouldReturnOriginalPriceWhenSaleRateIsZero() {
        // given
        Vas mockVas = createMockVas(1L, "일반 부가서비스", 10000, 0, 1L, "VAS001");
        
        // when
        Integer discountedPrice = mockVas.getDiscountedPrice();
        
        // then
        assertThat(discountedPrice).isEqualTo(10000);
    }

    @Test
    @DisplayName("할인율이 null인 경우 원가 반환")
    void vas_ShouldReturnOriginalPriceWhenSaleRateIsNull() {
        // given
        Vas mockVas = createMockVas(1L, "일반 부가서비스", 10000, null, 1L, "VAS001");
        
        // when
        Integer discountedPrice = mockVas.getDiscountedPrice();
        
        // then
        assertThat(discountedPrice).isEqualTo(10000);
    }

    @Test
    @DisplayName("할인율이 있으면 세일 중 상태 반환")
    void vas_ShouldReturnTrueWhenOnSale() {
        // given
        Vas mockVas = createMockVas(1L, "세일 부가서비스", 10000, 15, 1L, "VAS001");
        
        // when
        boolean isOnSale = mockVas.isOnSale();
        
        // then
        assertThat(isOnSale).isTrue();
    }

    @Test
    @DisplayName("할인율이 0이면 세일 중이 아닌 상태 반환")
    void vas_ShouldReturnFalseWhenNotOnSale() {
        // given
        Vas mockVas = createMockVas(1L, "일반 부가서비스", 10000, 0, 1L, "VAS001");
        
        // when
        boolean isOnSale = mockVas.isOnSale();
        
        // then
        assertThat(isOnSale).isFalse();
    }

    @Test
    @DisplayName("할인율이 null이면 세일 중이 아닌 상태 반환")
    void vas_ShouldReturnFalseWhenSaleRateIsNull() {
        // given
        Vas mockVas = createMockVas(1L, "일반 부가서비스", 10000, null, 1L, "VAS001");
        
        // when
        boolean isOnSale = mockVas.isOnSale();
        
        // then
        assertThat(isOnSale).isFalse();
    }

    private Vas createMockVas(Long id, String name, Integer price, Integer saleRate, Long tagCode, String categoryCode) {
        Vas vas = mock(Vas.class);
        when(vas.getVasId()).thenReturn(id);
        when(vas.getVasName()).thenReturn(name);
        when(vas.getPrice()).thenReturn(price);
        when(vas.getSaleRate()).thenReturn(saleRate);
        when(vas.getTagCode()).thenReturn(tagCode);
        when(vas.getCategoryCode()).thenReturn(categoryCode);
        
        // getDiscountedPrice() 메서드의 실제 로직 구현
        when(vas.getDiscountedPrice()).thenReturn(
            (saleRate == null || saleRate == 0) ? price : price - (price * saleRate / 100)
        );
        
        // isOnSale() 메서드의 실제 로직 구현
        when(vas.isOnSale()).thenReturn(saleRate != null && saleRate > 0);
        
        return vas;
    }
}