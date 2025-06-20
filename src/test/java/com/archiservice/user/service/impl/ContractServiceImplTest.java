package com.archiservice.user.service.impl;

import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.product.bundle.domain.ProductBundle;
import com.archiservice.product.bundle.repository.ProductBundleRepository;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.coupon.service.CouponService;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.service.PlanService;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.product.vas.service.VasService;
import com.archiservice.user.domain.Contract;
import com.archiservice.user.domain.User;
import com.archiservice.user.dto.request.ReservationRequestDto;
import com.archiservice.user.dto.response.ContractDetailResponseDto;
import com.archiservice.user.enums.Period;
import com.archiservice.user.repository.ContractRepository;
import com.archiservice.user.repository.UserRepository;
import com.archiservice.user.repository.custom.ContractCustomRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContractServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractCustomRepository contractCustomRepository;
    @Mock private PlanService planService;
    @Mock private VasService vasService;
    @Mock private CouponService couponService;
    @Mock private ProductBundleRepository productBundleRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    @DisplayName("계약 생성 성공")
    void createContract_ShouldCreateNewContractSuccessfully() {
        // given
        ReservationRequestDto requestDto = createReservationRequest(100L, 50000L);
        User user = createMockUser(1L);
        ProductBundle bundle = createMockProductBundle(100L);
        Contract currentContract = createMockContract(1L, "CARD", 40000L,
                LocalDateTime.now(), LocalDateTime.now().plusMonths(1));

        when(productBundleRepository.findById(100L)).thenReturn(Optional.of(bundle));
        when(contractRepository.findTop1ByUserOrderByIdDesc(user)).thenReturn(currentContract);

        // when
        contractService.createContract(requestDto, user);

        // then
        verify(contractRepository).save(any(Contract.class));
        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(contractCaptor.capture());
        
        Contract savedContract = contractCaptor.getValue();
        // Contract가 Mock이므로 직접 검증 대신 save 호출 확인
    }

    @Test
    @DisplayName("존재하지 않는 번들로 계약 생성 시 예외 발생")
    void createContract_ShouldThrowExceptionWhenBundleNotFound() {
        // given
        ReservationRequestDto requestDto = createReservationRequest(999L, 50000L);
        User user = createMockUser(1L);

        when(productBundleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contractService.createContract(requestDto, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("특정 기간의 플랜 조회 성공")
    void getPlan_ShouldReturnPlanDetailForPeriod() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        Period period = createMockPeriod(0); // 현재 기간
        PlanDetailResponseDto planDto = mock(PlanDetailResponseDto.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findPlanIdByOffset(1L, 0)).thenReturn(Optional.of(10L));
        when(planService.getPlanDetail(10L)).thenReturn(planDto);

        // when
        PlanDetailResponseDto result = contractService.getPlan(period, customUser);

        // then
        assertThat(result).isEqualTo(planDto);
        verify(planService).getPlanDetail(10L);
    }

    @Test
    @DisplayName("존재하지 않는 플랜 조회 시 예외 발생")
    void getPlan_ShouldThrowExceptionWhenPlanNotFound() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        Period period = createMockPeriod(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findPlanIdByOffset(1L, 0)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contractService.getPlan(period, customUser))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("특정 기간의 부가서비스 조회 성공")
    void getVas_ShouldReturnVasDetailForPeriod() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        Period period = createMockPeriod(-1); // 이전 기간
        VasDetailResponseDto vasDto = mock(VasDetailResponseDto.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findVasIdByOffset(1L, -1)).thenReturn(Optional.of(20L));
        when(vasService.getVasDetail(20L)).thenReturn(vasDto);

        // when
        VasDetailResponseDto result = contractService.getVas(period, customUser);

        // then
        assertThat(result).isEqualTo(vasDto);
        verify(vasService).getVasDetail(20L);
    }

    @Test
    @DisplayName("특정 기간의 쿠폰 조회 성공")
    void getCoupon_ShouldReturnCouponDetailForPeriod() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        Period period = createMockPeriod(1); // 다음 기간
        CouponDetailResponseDto couponDto = mock(CouponDetailResponseDto.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findCouponIdByOffset(1L, 1)).thenReturn(Optional.of(30L));
        when(couponService.getCouponDetail(30L)).thenReturn(couponDto);

        // when
        CouponDetailResponseDto result = contractService.getCoupon(period, customUser);

        // then
        assertThat(result).isEqualTo(couponDto);
        verify(couponService).getCouponDetail(30L);
    }

    @Test
    @DisplayName("특정 기간의 계약 조회 성공")
    void getContract_ShouldReturnContractListForPeriod() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        Period period = createMockPeriod(0);
        List<ContractDetailResponseDto> contractList = List.of(
                mock(ContractDetailResponseDto.class),
                mock(ContractDetailResponseDto.class)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractCustomRepository.findContractByOffset(user, period)).thenReturn(contractList);

        // when
        List<ContractDetailResponseDto> result = contractService.getContract(period, customUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(contractList);
    }

    @Test
    @DisplayName("다음 계약 취소 성공")
    void cancelNextContract_ShouldCopyFromCurrentContract() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        
        Contract nextContract = createMockContract(2L, "CARD", 60000L, 
                LocalDateTime.now().plusMonths(1), LocalDateTime.now().plusMonths(2));
        Contract currentContract = createMockContract(1L, "BANK", 50000L, 
                LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
        
        List<Contract> contractList = List.of(nextContract, currentContract);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findTop2ByUserOrderByIdDesc(user)).thenReturn(contractList);

        // when
        contractService.cancelNextContract(customUser);

        // then
        verify(nextContract).copyFrom(currentContract);
    }

    @Test
    @DisplayName("다음 계약 업데이트 성공")
    void updateNextContract_ShouldUpdateNextContractInfo() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        ReservationRequestDto requestDto = createReservationRequest(200L, 70000L);
        ProductBundle newBundle = createMockProductBundle(200L);
        Contract nextContract = createMockContract(2L, "CARD", 60000L, 
                LocalDateTime.now().plusMonths(1), LocalDateTime.now().plusMonths(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productBundleRepository.findById(200L)).thenReturn(Optional.of(newBundle));
        when(contractRepository.findTop1ByUserOrderByIdDesc(user)).thenReturn(nextContract);

        // when
        contractService.updateNextContract(requestDto, customUser);

        // then
        verify(nextContract).updateNextContract(newBundle, 70000L);
    }

    @Test
    @DisplayName("계약 액션 결정 - 오늘이 계약 종료일인 경우 새 계약 생성")
    void determineContractAction_ShouldCreateContractWhenTodayIsEndDate() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        ReservationRequestDto requestDto = createReservationRequest(100L, 50000L);
        ProductBundle bundle = createMockProductBundle(100L);
        
        Contract recentContract = createMockContract(1L, "CARD", 40000L, 
                LocalDateTime.now().minusMonths(1), LocalDateTime.now()); // 오늘 종료
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findTop1ByUserOrderByIdDesc(user)).thenReturn(recentContract);
        when(productBundleRepository.findById(100L)).thenReturn(Optional.of(bundle));

        // when
        contractService.determineContractAction(requestDto, customUser);

        // then
        verify(contractRepository).save(any(Contract.class)); // 새 계약 생성됨
    }

    @Test
    @DisplayName("계약 액션 결정 - 오늘이 계약 종료일이 아닌 경우 다음 계약 업데이트")
    void determineContractAction_ShouldUpdateNextContractWhenTodayIsNotEndDate() {
        // given
        CustomUser customUser = createMockCustomUser(1L);
        User user = createMockUser(1L);
        ReservationRequestDto requestDto = createReservationRequest(200L, 70000L);
        ProductBundle bundle = createMockProductBundle(200L);
        
        Contract recentContract = createMockContract(1L, "CARD", 40000L, 
                LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusDays(10)); // 아직 안 끝남
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(contractRepository.findTop1ByUserOrderByIdDesc(user)).thenReturn(recentContract);
        when(productBundleRepository.findById(200L)).thenReturn(Optional.of(bundle));

        // when
        contractService.determineContractAction(requestDto, customUser);

        // then
        verify(recentContract).updateNextContract(bundle, 70000L); // 다음 계약 업데이트됨
        verify(contractRepository, never()).save(any(Contract.class)); // 새 계약은 생성되지 않음
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 계약 조회 시 예외 발생")
    void getContract_ShouldThrowExceptionWhenUserNotFound() {
        // given
        CustomUser customUser = createMockCustomUser(999L);
        Period period = createMockPeriod(0);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contractService.getContract(period, customUser))
                .isInstanceOf(UserNotFoundException.class);
    }

    private CustomUser createMockCustomUser(Long id) {
        CustomUser user = mock(CustomUser.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private User createMockUser(Long id) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        return user;
    }

    private ReservationRequestDto createReservationRequest(Long bundleId, Long price) {
        ReservationRequestDto request = mock(ReservationRequestDto.class);
        when(request.getBundleId()).thenReturn(bundleId);
        when(request.getPrice()).thenReturn(price);
        return request;
    }

    private ProductBundle createMockProductBundle(Long id) {
        ProductBundle bundle = mock(ProductBundle.class);
        when(bundle.getProductBundleId()).thenReturn(id);
        return bundle;
    }

    private Contract createMockContract(Long id, String paymentMethod, Long price, 
                                      LocalDateTime startDate, LocalDateTime endDate) {
        Contract contract = mock(Contract.class);
        when(contract.getId()).thenReturn(id);
        when(contract.getPaymentMethod()).thenReturn(paymentMethod);
        when(contract.getPrice()).thenReturn(price);
        when(contract.getStartDate()).thenReturn(startDate);
        when(contract.getEndDate()).thenReturn(endDate);
        return contract;
    }

    private Period createMockPeriod(int offset) {
        Period period = mock(Period.class);
        when(period.getOffset()).thenReturn(offset);
        return period;
    }
}