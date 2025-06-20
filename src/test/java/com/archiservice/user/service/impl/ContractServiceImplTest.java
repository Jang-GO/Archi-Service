//package com.archiservice.user.service.impl;
//
//import com.archiservice.exception.ErrorCode;
//import com.archiservice.exception.business.ContractNotFoundException;
//import com.archiservice.product.bundle.domain.ProductBundle;
//import com.archiservice.product.bundle.repository.ProductBundleRepository;
//import com.archiservice.product.coupon.domain.Coupon;
//import com.archiservice.product.coupon.service.CouponService;
//import com.archiservice.product.plan.domain.Plan;
//import com.archiservice.product.plan.service.PlanService;
//import com.archiservice.product.vas.domain.Vas;
//import com.archiservice.product.vas.service.VasService;
//import com.archiservice.user.domain.Contract;
//import com.archiservice.user.domain.User;
//import com.archiservice.user.repository.ContractRepository;
//import com.archiservice.user.repository.UserRepository;
//import com.archiservice.user.repository.custom.ContractCustomRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ContractServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @Mock
//    private ContractCustomRepository contractCustomRepository;
//
//    @Mock
//    private PlanService planService;
//
//    @Mock
//    private VasService vasService;
//
//    @Mock
//    private CouponService couponService;
//
//    @Mock
//    private ProductBundleRepository productBundleRepository;
//
//    @InjectMocks
//    private ContractServiceImpl contractService;
//
//    private User testUser;
//    private ProductBundle testProductBundle;
//    private Contract testContract;
//    private LocalDate testDate;
//
//    @BeforeEach
//    void setUp() {
//        testDate = LocalDate.of(2024, 1, 31);
//
//        Plan mockPlan = mock(Plan.class);
//        Vas mockVas = mock(Vas.class);
//        Coupon mockCoupon = mock(Coupon.class);
//
//        testUser = User.builder()
//                .userId(1L)
//                .email("test@example.com")
//                .username("Test User")
//                .build();
//
//        testProductBundle = ProductBundle.builder()
//                .productBundleId(1L)
//                .plan(mockPlan)
//                .vas(mockVas)
//                .coupon(mockCoupon)
//                .likeCount(0L)
//                .dislikeCount(0L)
//                .tagCode(0L)
//                .build();
//
//        testContract = Contract.builder()
//                .id(1L)
//                .user(testUser)
//                .productBundle(testProductBundle)
//                .paymentMethod("CARD")
//                .price(10000L)
//                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
//                .endDate(LocalDateTime.of(2024, 1, 31, 0, 0))
//                .build();
//    }
//
//    @Test
//    @DisplayName("계약 갱신 성공 테스트")
//    void renewContract_Success() {
//        // Given
//        List<Contract> expiredContracts = Arrays.asList(testContract);
//        when(contractRepository.findByEndDate(testDate))
//                .thenReturn(Optional.of(expiredContracts));
//
//        // When
//        contractService.renewContract(testDate);
//
//        // Then
//        verify(contractRepository, times(1)).findByEndDate(testDate);
//        verify(contractRepository, times(1)).save(any(Contract.class));
//
//        // 저장된 계약 내용 검증
//        verify(contractRepository).save(argThat(contract ->
//                contract.getUser().equals(testUser) &&
//                        contract.getProductBundle().equals(testProductBundle) &&
//                        contract.getPaymentMethod().equals("CARD") &&
//                        contract.getPrice() == 10000 &&
//                        contract.getStartDate().equals(testDate.atStartOfDay().plusDays(1)) &&
//                        contract.getEndDate().equals(testDate.atStartOfDay().plusMonths(1))
//        ));
//    }
//
//    @Test
//    @DisplayName("만료된 계약이 없을 때 예외 발생 테스트")
//    void renewContract_ContractNotFound() {
//        // Given
//        when(contractRepository.findByEndDate(testDate))
//                .thenReturn(Optional.empty());
//
//        // When & Then
//        ContractNotFoundException exception = assertThrows(
//                ContractNotFoundException.class,
//                () -> contractService.renewContract(testDate)
//        );
//
//        assertEquals(ErrorCode.CONTRACT_NOT_FOUND.getMessage(), exception.getMessage());
//        verify(contractRepository, times(1)).findByEndDate(testDate);
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    @DisplayName("여러 계약 갱신 성공 테스트")
//    void renewContract_MultipleContracts() {
//        // Given
//        User user2 = User.builder()
//                .userId(2L)
//                .email("test2@example.com")
//                .username("Test User 2")
//                .build();
//
//        Contract contract2 = Contract.builder()
//                .id(2L)
//                .user(user2)
//                .productBundle(testProductBundle)
//                .paymentMethod("BANK")
//                .price(15000L)
//                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
//                .endDate(LocalDateTime.of(2024, 1, 31, 0, 0))
//                .build();
//
//        List<Contract> expiredContracts = Arrays.asList(testContract, contract2);
//        when(contractRepository.findByEndDate(testDate))
//                .thenReturn(Optional.of(expiredContracts));
//
//        // When
//        contractService.renewContract(testDate);
//
//        // Then
//        verify(contractRepository, times(1)).findByEndDate(testDate);
//        verify(contractRepository, times(2)).save(any(Contract.class));
//    }
//
//    @Test
//    @DisplayName("갱신 날짜 계산 정확성 테스트")
//    void renewContract_DateCalculation() {
//        // Given
//        LocalDate specificDate = LocalDate.of(2024, 2, 29); // 윤년 테스트
//        Contract leapYearContract = Contract.builder()
//                .id(1L)
//                .user(testUser)
//                .productBundle(testProductBundle)
//                .paymentMethod("CARD")
//                .price(10000L)
//                .startDate(LocalDateTime.of(2024, 1, 29, 0, 0))
//                .endDate(LocalDateTime.of(2024, 2, 29, 0, 0))
//                .build();
//
//        when(contractRepository.findByEndDate(specificDate))
//                .thenReturn(Optional.of(Arrays.asList(leapYearContract)));
//
//        // When
//        contractService.renewContract(specificDate);
//
//        // Then
//        verify(contractRepository).save(argThat(contract ->
//                contract.getStartDate().equals(LocalDateTime.of(2024, 3, 1, 0, 0)) &&
//                        contract.getEndDate().equals(LocalDateTime.of(2024, 3, 29, 0, 0))
//        ));
//    }
//}