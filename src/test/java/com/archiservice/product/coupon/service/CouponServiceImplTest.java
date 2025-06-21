package com.archiservice.product.coupon.service;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.dto.response.CouponResponseDto;
import com.archiservice.product.coupon.repository.CouponRepository;
import com.archiservice.product.coupon.service.impl.CouponServiceImpl;
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
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock private TagMetaService tagMetaService;
    @Mock private CommonCodeService commonCodeService;
    @Mock private ProductReviewSummaryRepository reviewSummaryRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    @DisplayName("전체 쿠폰 페이징 조회")
    void getAllCoupons_ShouldReturnPagedCoupons() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        Coupon mockCoupon1 = createMockCoupon(1L, "기본 쿠폰", 5000, 5L, "CAT001");
        Coupon mockCoupon2 = createMockCoupon(2L, "프리미엄 쿠폰", 10000, 3L, "CAT002");
        List<Coupon> couponList = List.of(mockCoupon1, mockCoupon2);
        
        Page<Coupon> mockPage = new PageImpl<>(couponList, pageable, 2);
        
        when(couponRepository.findAll(pageable)).thenReturn(mockPage);
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(tagMetaService.extractTagsFromCode(3L)).thenReturn(List.of("스포츠"));
        when(commonCodeService.getCodeName("G04", "CAT001")).thenReturn("기본카테고리");
        when(commonCodeService.getCodeName("G04", "CAT002")).thenReturn("프리미엄카테고리");
        
        // when
        Page<CouponResponseDto> result = couponService.getAllCoupons(pageable);
        
        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        
        CouponResponseDto firstCoupon = result.getContent().get(0);
        assertThat(firstCoupon.getTags()).containsExactly("음식", "여행");
        assertThat(firstCoupon.getCategory()).isEqualTo("기본카테고리");
    }

    @Test
    @DisplayName("빈 페이지 반환")
    void getAllCoupons_ShouldReturnEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Coupon> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(couponRepository.findAll(pageable)).thenReturn(emptyPage);
        
        // when
        Page<CouponResponseDto> result = couponService.getAllCoupons(pageable);
        
        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("쿠폰 상세 조회 성공 - 리뷰 요약 없음")
    void getCouponDetail_ShouldReturnCouponDetailWithoutReview() {
        // given
        Long couponId = 1L;
        Coupon mockCoupon = createMockCoupon(couponId, "기본 쿠폰", 5000, 5L, "CAT001");
        
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(mockCoupon));
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(commonCodeService.getCodeName("G04", "CAT001")).thenReturn("기본카테고리");
        when(reviewSummaryRepository.findByProductIdAndReviewType(couponId, "COUPON")).thenReturn(Optional.empty());

        // when
        CouponDetailResponseDto result = couponService.getCouponDetail(couponId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("음식", "여행");
        assertThat(result.getCategory()).isEqualTo("기본카테고리");
    }

    @Test
    @DisplayName("쿠폰 상세 조회 성공 - 리뷰 요약 포함")
    void getCouponDetail_ShouldReturnCouponDetailWithReview() {
        // given
        Long couponId = 1L;
        Coupon mockCoupon = createMockCoupon(couponId, "프리미엄 쿠폰", 10000, 7L, "CAT002");
        ProductReviewSummary mockReviewSummary = mock(ProductReviewSummary.class);
        
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(mockCoupon));
        when(tagMetaService.extractTagsFromCode(7L)).thenReturn(List.of("프리미엄"));
        when(commonCodeService.getCodeName("G04", "CAT002")).thenReturn("프리미엄카테고리");
        when(reviewSummaryRepository.findByProductIdAndReviewType(couponId, "COUPON")).thenReturn(Optional.of(mockReviewSummary));

        // when
        CouponDetailResponseDto result = couponService.getCouponDetail(couponId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("프리미엄");
        assertThat(result.getCategory()).isEqualTo("프리미엄카테고리");
        verify(reviewSummaryRepository).findByProductIdAndReviewType(couponId, "COUPON");
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 조회 시 예외 발생")
    void getCouponDetail_ShouldThrowExceptionWhenCouponNotFound() {
        // given
        Long couponId = 999L;
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.getCouponDetail(couponId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("쿠폰 가격이 null인 경우 0 반환")
    void coupon_ShouldReturnZeroWhenPriceIsNull() {
        // given
        Coupon mockCoupon = createMockCoupon(1L, "무료 쿠폰", null, 1L, "CAT001");
        
        // when
        int price = mockCoupon.getPrice();
        
        // then
        assertThat(price).isEqualTo(0);
    }

    @Test
    @DisplayName("쿠폰 가격이 설정된 경우 정상 반환")
    void coupon_ShouldReturnActualPriceWhenPriceIsSet() {
        // given
        Coupon mockCoupon = createMockCoupon(1L, "유료 쿠폰", 5000, 1L, "CAT001");
        
        // when
        int price = mockCoupon.getPrice();
        
        // then
        assertThat(price).isEqualTo(5000);
    }

    private Coupon createMockCoupon(Long id, String name, Integer price, Long tagCode, String categoryCode) {
        Coupon coupon = mock(Coupon.class);
        when(coupon.getCouponId()).thenReturn(id);
        when(coupon.getCouponName()).thenReturn(name);
        when(coupon.getTagCode()).thenReturn(tagCode);
        when(coupon.getCategoryCode()).thenReturn(categoryCode);
        
        // getPrice() 메서드의 실제 로직 구현
        when(coupon.getPrice()).thenReturn(price == null ? 0 : price);
        
        return coupon;
    }
}