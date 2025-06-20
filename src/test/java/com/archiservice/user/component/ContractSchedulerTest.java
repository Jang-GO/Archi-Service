//package com.archiservice.user.component;
//
//import com.archiservice.user.service.ContractService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ContractSchedulerTest {
//
//    @Mock
//    private ContractService contractService;
//
//    @InjectMocks
//    private ContractScheduler contractScheduler;
//
//    @Test
//    @DisplayName("스케줄러가 정상적으로 계약 갱신 서비스를 호출하는지 테스트")
//    void renewContractAction_CallsContractService() {
//        // Given
//        LocalDate fixedDate = LocalDate.of(2024, 1, 31);
//
//        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
//            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
//
//            // When
//            contractScheduler.renewContractAction();
//
//            // Then
//            verify(contractService, times(1)).renewContract(fixedDate);
//        }
//    }
//
//    @Test
//    @DisplayName("스케줄러가 현재 날짜를 올바르게 전달하는지 테스트")
//    void renewContractAction_PassesCurrentDate() {
//        // Given - 실제 현재 날짜를 사용
//
//        // When
//        contractScheduler.renewContractAction();
//
//        // Then
//        verify(contractService, times(1)).renewContract(any(LocalDate.class));
//    }
//
//    @Test
//    @DisplayName("스케줄러 실행 중 예외 발생 시 처리 테스트")
//    void renewContractAction_ExceptionHandling() {
//        // Given
//        doThrow(new RuntimeException("Test Exception"))
//                .when(contractService).renewContract(any(LocalDate.class));
//
//        // When & Then
//        // 스케줄러는 예외를 처리하지 않으므로 예외가 발생해야 함
//        try {
//            contractScheduler.renewContractAction();
//        } catch (RuntimeException e) {
//            // 예외가 발생하는 것이 정상
//        }
//
//        verify(contractService, times(1)).renewContract(any(LocalDate.class));
//    }
//
//    @Test
//    @DisplayName("여러 번 호출 시 각각 독립적으로 실행되는지 테스트")
//    void renewContractAction_MultipleCallsAreIndependent() {
//        // Given
//        LocalDate date1 = LocalDate.of(2024, 1, 31);
//        LocalDate date2 = LocalDate.of(2024, 2, 29);
//
//        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
//            // 첫 번째 호출
//            mockedLocalDate.when(LocalDate::now).thenReturn(date1);
//            contractScheduler.renewContractAction();
//
//            // 두 번째 호출
//            mockedLocalDate.when(LocalDate::now).thenReturn(date2);
//            contractScheduler.renewContractAction();
//
//            // Then
//            verify(contractService, times(1)).renewContract(date1);
//            verify(contractService, times(1)).renewContract(date2);
//            verify(contractService, times(2)).renewContract(any(LocalDate.class));
//        }
//    }
//}