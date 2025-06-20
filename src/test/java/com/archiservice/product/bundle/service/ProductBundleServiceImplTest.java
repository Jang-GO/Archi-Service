package com.archiservice.product.bundle.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.bundle.domain.ProductBundle;
import com.archiservice.product.bundle.dto.request.CreateBundleRequestDto;
import com.archiservice.product.bundle.dto.response.BundleCombinationResponseDto;
import com.archiservice.product.bundle.repository.ProductBundleRepository;
import com.archiservice.product.bundle.service.impl.ProductBundleServiceImpl;
import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.repository.CouponRepository;
import com.archiservice.product.coupon.service.CouponService;
import com.archiservice.product.plan.domain.Plan;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.repository.PlanRepository;
import com.archiservice.product.plan.service.PlanService;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.repository.VasRepository;
import com.archiservice.product.vas.service.VasService;
import com.archiservice.user.dto.request.ReservationRequestDto;
import com.archiservice.user.repository.UserRepository;
import com.archiservice.user.service.ContractService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductBundleServiceImplTest {

    @Mock
    private ProductBundleRepository productBundleRepository;
    @Mock private PlanRepository planRepository;
    @Mock private VasRepository vasRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private PlanService planService;
    @Mock private VasService vasService;
    @Mock private CouponService couponService;
    @Mock private ContractService contractService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProductBundleServiceImpl productBundleService;

    @Test
    @DisplayName("번들 생성 - 기존 번들이 존재하는 경우")
    void createBundle_ShouldUseExistingBundle() {
        // given
        CustomUser customUser = createMockCustomUser();
        CreateBundleRequestDto requestDto = mock(CreateBundleRequestDto.class);
        when(requestDto.getPlanId()).thenReturn(1L);
        when(requestDto.getVasId()).thenReturn(2L);
        when(requestDto.getCouponId()).thenReturn(3L);

        Plan mockPlan = createMockPlan(1L, 50000);
        Vas mockVas = createMockVas(2L, 10000);
        Coupon mockCoupon = createMockCoupon(3L, 5000);
        ProductBundle existingBundle = createMockProductBundle(100L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(vasRepository.findById(2L)).thenReturn(Optional.of(mockVas));
        when(couponRepository.findById(3L)).thenReturn(Optional.of(mockCoupon));
        when(productBundleRepository.findByPlanAndVasAndCoupon(mockPlan, mockVas, mockCoupon))
                .thenReturn(Optional.of(existingBundle));

        // when
        productBundleService.createBundle(requestDto, customUser);

        // then
        verify(productBundleRepository, never()).save(any(ProductBundle.class)); // 새로운 번들 저장 안 함
        verify(contractService).determineContractAction(any(ReservationRequestDto.class), eq(customUser));
        
        ArgumentCaptor<ReservationRequestDto> captor = ArgumentCaptor.forClass(ReservationRequestDto.class);
        verify(contractService).determineContractAction(captor.capture(), eq(customUser));
        
        ReservationRequestDto captured = captor.getValue();
        assertThat(captured.getBundleId()).isEqualTo(100L);
        assertThat(captured.getPrice()).isEqualTo(65000L); // 50000 + 10000 + 5000
    }

    @Test
    @DisplayName("번들 생성 - 새로운 번들 생성")
    void createBundle_ShouldCreateNewBundle() {
        // given
        CustomUser customUser = createMockCustomUser();
        CreateBundleRequestDto requestDto = mock(CreateBundleRequestDto.class);
        when(requestDto.getPlanId()).thenReturn(1L);
        when(requestDto.getVasId()).thenReturn(2L);
        when(requestDto.getCouponId()).thenReturn(3L);

        Plan mockPlan = createMockPlan(1L, 30000);
        Vas mockVas = createMockVas(2L, 8000);
        Coupon mockCoupon = createMockCoupon(3L, 2000);
        ProductBundle newBundle = createMockProductBundle(200L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(vasRepository.findById(2L)).thenReturn(Optional.of(mockVas));
        when(couponRepository.findById(3L)).thenReturn(Optional.of(mockCoupon));
        when(productBundleRepository.findByPlanAndVasAndCoupon(mockPlan, mockVas, mockCoupon))
                .thenReturn(Optional.empty());

        when(productBundleRepository.save(any(ProductBundle.class))).thenAnswer(invocation -> {
            ProductBundle savedBundle = mock(ProductBundle.class);
            when(savedBundle.getProductBundleId()).thenReturn(200L);
            return savedBundle;
        });

        // when
        productBundleService.createBundle(requestDto, customUser);

        // then
        verify(productBundleRepository).save(any(ProductBundle.class)); // 새로운 번들 저장
        verify(contractService).determineContractAction(any(ReservationRequestDto.class), eq(customUser));
    }

    @Test
    @DisplayName("번들 생성 - 존재하지 않는 요금제")
    void createBundle_ShouldThrowExceptionWhenPlanNotFound() {
        // given
        CustomUser customUser = createMockCustomUser();
        CreateBundleRequestDto requestDto = mock(CreateBundleRequestDto.class);
        when(requestDto.getPlanId()).thenReturn(1L);
        when(requestDto.getVasId()).thenReturn(2L);
        when(requestDto.getCouponId()).thenReturn(3L);

        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productBundleService.createBundle(requestDto, customUser))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("번들 생성 - 존재하지 않는 부가서비스")
    void createBundle_ShouldThrowExceptionWhenVasNotFound() {
        // given
        CustomUser customUser = createMockCustomUser();
        CreateBundleRequestDto requestDto = mock(CreateBundleRequestDto.class);
        when(requestDto.getPlanId()).thenReturn(1L);
        when(requestDto.getVasId()).thenReturn(2L);
        when(requestDto.getCouponId()).thenReturn(3L);

        Plan mockPlan = createMockPlan(1L, 50000);
        when(planRepository.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(vasRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productBundleService.createBundle(requestDto, customUser))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("번들 상세 조회 - 기존 번들 존재")
    void getBundleByIds_ShouldReturnBundleWithExistingData() {
        // given
        long planId = 1L, vasId = 2L, couponId = 3L;
        
        PlanDetailResponseDto planDto = mock(PlanDetailResponseDto.class);
        VasDetailResponseDto vasDto = mock(VasDetailResponseDto.class);
        CouponDetailResponseDto couponDto = mock(CouponDetailResponseDto.class);
        ProductBundle existingBundle = createMockProductBundle(100L);
        
        when(planService.getPlanDetail(planId)).thenReturn(planDto);
        when(vasService.getVasDetail(vasId)).thenReturn(vasDto);
        when(couponService.getCouponDetail(couponId)).thenReturn(couponDto);
        when(productBundleRepository.findProductBundleByPlan_PlanIdAndVas_VasIdAndCoupon_CouponId(planId, vasId, couponId))
                .thenReturn(Optional.of(existingBundle));

        // when
        BundleCombinationResponseDto result = productBundleService.getBundleByIds(planId, vasId, couponId);

        // then
        assertThat(result.getPlanDetail()).isEqualTo(planDto);
        assertThat(result.getVasDetail()).isEqualTo(vasDto);
        assertThat(result.getCouponDetail()).isEqualTo(couponDto);
        assertThat(result.getBundleDetail()).isNotNull();
        assertThat(result.getBundleDetail().getBundleId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("번들 상세 조회 - 번들이 존재하지 않음")
    void getBundleByIds_ShouldReturnBundleWithoutBundleData() {
        // given
        long planId = 1L, vasId = 2L, couponId = 3L;
        
        PlanDetailResponseDto planDto = mock(PlanDetailResponseDto.class);
        VasDetailResponseDto vasDto = mock(VasDetailResponseDto.class);
        CouponDetailResponseDto couponDto = mock(CouponDetailResponseDto.class);
        
        when(planService.getPlanDetail(planId)).thenReturn(planDto);
        when(vasService.getVasDetail(vasId)).thenReturn(vasDto);
        when(couponService.getCouponDetail(couponId)).thenReturn(couponDto);
        when(productBundleRepository.findProductBundleByPlan_PlanIdAndVas_VasIdAndCoupon_CouponId(planId, vasId, couponId))
                .thenReturn(Optional.empty());

        // when
        BundleCombinationResponseDto result = productBundleService.getBundleByIds(planId, vasId, couponId);

        // then
        assertThat(result.getPlanDetail()).isEqualTo(planDto);
        assertThat(result.getVasDetail()).isEqualTo(vasDto);
        assertThat(result.getCouponDetail()).isEqualTo(couponDto);
        assertThat(result.getBundleDetail()).isNull(); // 번들 정보 없음
    }

    @Test
    @DisplayName("좋아요 수 증가")
    void updateLikeOrDislikeCount_ShouldIncrementLikeCount() {
        // given
        long bundleId = 100L;
        when(productBundleRepository.incrementLikeCount(bundleId)).thenReturn(1);

        // when
        productBundleService.updateLikeOrDislikeCount(bundleId, true);

        // then
        verify(productBundleRepository).incrementLikeCount(bundleId);
        verify(productBundleRepository, never()).incrementDislikeCount(anyLong());
    }

    @Test
    @DisplayName("싫어요 수 증가")
    void updateLikeOrDislikeCount_ShouldIncrementDislikeCount() {
        // given
        long bundleId = 100L;
        when(productBundleRepository.incrementDislikeCount(bundleId)).thenReturn(1);

        // when
        productBundleService.updateLikeOrDislikeCount(bundleId, false);

        // then
        verify(productBundleRepository).incrementDislikeCount(bundleId);
        verify(productBundleRepository, never()).incrementLikeCount(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 번들 좋아요/싫어요 시 예외 발생")
    void updateLikeOrDislikeCount_ShouldThrowExceptionWhenBundleNotFound() {
        // given
        long bundleId = 999L;
        when(productBundleRepository.incrementLikeCount(bundleId)).thenReturn(0); // 업데이트된 행 없음

        // when & then
        assertThatThrownBy(() -> productBundleService.updateLikeOrDislikeCount(bundleId, true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    private CustomUser createMockCustomUser() {
        CustomUser user = mock(CustomUser.class);
        when(user.getId()).thenReturn(1L);
        return user;
    }

    private Plan createMockPlan(Long id, Integer price) {
        Plan plan = mock(Plan.class);
        when(plan.getPlanId()).thenReturn(id);
        when(plan.getPrice()).thenReturn(price);
        when(plan.getTagCode()).thenReturn(4L);
        return plan;
    }

    private Vas createMockVas(Long id, Integer discountedPrice) {
        Vas vas = mock(Vas.class);
        when(vas.getVasId()).thenReturn(id);
        when(vas.getDiscountedPrice()).thenReturn(discountedPrice);
        when(vas.getTagCode()).thenReturn(2L);
        return vas;
    }

    private Coupon createMockCoupon(Long id, Integer price) {
        Coupon coupon = mock(Coupon.class);
        when(coupon.getCouponId()).thenReturn(id);
        when(coupon.getPrice()).thenReturn(price);
        when(coupon.getTagCode()).thenReturn(1L);
        return coupon;
    }

    private ProductBundle createMockProductBundle(Long id) {
        ProductBundle bundle = mock(ProductBundle.class);
        when(bundle.getProductBundleId()).thenReturn(id);
        when(bundle.getLikeCount()).thenReturn(10L);
        when(bundle.getDislikeCount()).thenReturn(3L);
        when(bundle.getTagCode()).thenReturn(7L);
        when(bundle.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(bundle.getUpdatedAt()).thenReturn(LocalDateTime.now());
        return bundle;
    }
}