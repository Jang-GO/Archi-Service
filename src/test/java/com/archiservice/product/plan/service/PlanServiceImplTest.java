package com.archiservice.product.plan.service;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.plan.domain.Plan;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanResponseDto;
import com.archiservice.product.plan.repository.PlanRepository;
import com.archiservice.product.plan.service.impl.PlanServiceImpl;
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
class PlanServiceImplTest {

    @Mock private PlanRepository planRepository;
    @Mock private TagMetaService tagMetaService;
    @Mock private CommonCodeService commonCodeService;
    @Mock private ProductReviewSummaryRepository reviewSummaryRepository;

    @InjectMocks private PlanServiceImpl planService;

    @Test
    @DisplayName("전체 플랜 페이징 조회")
    void getAllPlans_ShouldReturnPagedPlans() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Plan mockPlan1 = createMockPlan(1L, "기본플랜", "PLAN001", "AGE20", 5L);
        Plan mockPlan2 = createMockPlan(2L, "프리미엄플랜", "PLAN002", "AGE30", 3L);
        List<Plan> planList = List.of(mockPlan1, mockPlan2);

        Page<Plan> mockPage = new PageImpl<>(planList, pageable, 2);

        when(planRepository.findAll(pageable)).thenReturn(mockPage);
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(tagMetaService.extractTagsFromCode(3L)).thenReturn(List.of("스포츠"));
        when(commonCodeService.getCodeName("G02", "PLAN001")).thenReturn("기본플랜");
        when(commonCodeService.getCodeName("G02", "PLAN002")).thenReturn("프리미엄플랜");
        when(commonCodeService.getCodeName("G01", "AGE20")).thenReturn("20대");
        when(commonCodeService.getCodeName("G01", "AGE30")).thenReturn("30대");

        // when
        Page<PlanResponseDto> result = planService.getAllPlans(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);

        PlanResponseDto firstPlan = result.getContent().get(0);
        assertThat(firstPlan.getTags()).containsExactly("음식", "여행");
        assertThat(firstPlan.getCategory()).isEqualTo("기본플랜");
        assertThat(firstPlan.getTargetAge()).isEqualTo("20대");
    }

    @Test
    @DisplayName("빈 페이지 반환")
    void getAllPlans_ShouldReturnEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plan> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(planRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        Page<PlanResponseDto> result = planService.getAllPlans(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("정확한 플랜명으로 조회 성공")
    void findPlanByName_ShouldReturnPlanWhenExactMatch() {
        // given
        Plan mockPlan = createMockPlan(1L, "프리미엄 플랜", "PREMIUM", "AGE20", 5L);
        when(planRepository.findByPlanName("프리미엄 플랜")).thenReturn(Optional.of(mockPlan));
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(commonCodeService.getCodeName("G02", "PREMIUM")).thenReturn("프리미엄");
        when(commonCodeService.getCodeName("G01", "AGE20")).thenReturn("20대");

        // when
        PlanDetailResponseDto result = planService.findPlanByName("프리미엄 플랜");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("음식", "여행");
        assertThat(result.getCategory()).isEqualTo("프리미엄");
        verify(planRepository, never()).findAll(); // 전체 조회는 호출되지 않아야 함
    }

    @Test
    @DisplayName("유사한 플랜명으로 조회 성공 - 공백 제거 후 매칭")
    void findPlanByName_ShouldReturnPlanWhenSimilarMatch() {
        // given
        when(planRepository.findByPlanName("베이직플랜")).thenReturn(Optional.empty());

        Plan mockPlan = createMockPlan(1L, "베이직 플랜", "BASIC", "AGE30", 3L);
        when(planRepository.findAll()).thenReturn(List.of(mockPlan));
        when(tagMetaService.extractTagsFromCode(3L)).thenReturn(List.of("기본"));
        when(commonCodeService.getCodeName("CATEGORY", "BASIC")).thenReturn("기본");
        when(commonCodeService.getCodeName("AGE", "AGE30")).thenReturn("30대");

        // when
        PlanDetailResponseDto result = planService.findPlanByName("베이직플랜");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("기본");
        assertThat(result.getPlanName()).isEqualTo("베이직 플랜");
    }

    @Test
    @DisplayName("부분 매칭으로 조회 성공")
    void findPlanByName_ShouldReturnPlanWhenPartialMatch() {
        // given
        when(planRepository.findByPlanName("프리미엄")).thenReturn(Optional.empty());

        Plan mockPlan = createMockPlan(1L, "프리미엄 무제한 플랜", "PREMIUM", "AGE20", 7L);
        when(planRepository.findAll()).thenReturn(List.of(mockPlan));
        when(tagMetaService.extractTagsFromCode(7L)).thenReturn(List.of("무제한"));
        when(commonCodeService.getCodeName("CATEGORY", "PREMIUM")).thenReturn("프리미엄");
        when(commonCodeService.getCodeName("AGE", "AGE20")).thenReturn("20대");

        // when
        PlanDetailResponseDto result = planService.findPlanByName("프리미엄");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPlanName()).isEqualTo("프리미엄 무제한 플랜");
    }

    @Test
    @DisplayName("존재하지 않는 플랜명 조회 시 null 반환")
    void findPlanByName_ShouldReturnNullWhenNotFound() {
        // given
        when(planRepository.findByPlanName("존재하지않는플랜")).thenReturn(Optional.empty());
        when(planRepository.findAll()).thenReturn(List.of());

        // when
        PlanDetailResponseDto result = planService.findPlanByName("존재하지않는플랜");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("플랜 상세 조회 성공 - 리뷰 요약 없음")
    void getPlanDetail_ShouldReturnPlanDetailWithoutReview() {
        // given
        Long planId = 1L;
        Plan mockPlan = createMockPlan(planId, "기본 플랜", "BASIC", "AGE20", 5L);

        when(planRepository.findById(planId)).thenReturn(Optional.of(mockPlan));
        when(tagMetaService.extractTagsFromCode(5L)).thenReturn(List.of("음식", "여행"));
        when(commonCodeService.getCodeName("G02", "BASIC")).thenReturn("기본");
        when(commonCodeService.getCodeName("G01", "AGE20")).thenReturn("20대");
        when(reviewSummaryRepository.findByProductIdAndReviewType(planId, "PLAN")).thenReturn(Optional.empty());

        // when
        PlanDetailResponseDto result = planService.getPlanDetail(planId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("음식", "여행");
        assertThat(result.getCategory()).isEqualTo("기본");
        assertThat(result.getTargetAge()).isEqualTo("20대");
    }

    @Test
    @DisplayName("플랜 상세 조회 성공 - 리뷰 요약 포함")
    void getPlanDetail_ShouldReturnPlanDetailWithReview() {
        // given
        Long planId = 1L;
        Plan mockPlan = createMockPlan(planId, "프리미엄 플랜", "PREMIUM", "AGE30", 7L);
        ProductReviewSummary mockReviewSummary = mock(ProductReviewSummary.class);

        when(planRepository.findById(planId)).thenReturn(Optional.of(mockPlan));
        when(tagMetaService.extractTagsFromCode(7L)).thenReturn(List.of("프리미엄"));
        when(commonCodeService.getCodeName("G02", "PREMIUM")).thenReturn("프리미엄");
        when(commonCodeService.getCodeName("G01", "AGE30")).thenReturn("30대");
        when(reviewSummaryRepository.findByProductIdAndReviewType(planId, "PLAN")).thenReturn(Optional.of(mockReviewSummary));

        // when
        PlanDetailResponseDto result = planService.getPlanDetail(planId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).containsExactly("프리미엄");
        assertThat(result.getCategory()).isEqualTo("프리미엄");
        assertThat(result.getTargetAge()).isEqualTo("30대");
        verify(reviewSummaryRepository).findByProductIdAndReviewType(planId, "PLAN");
    }

    @Test
    @DisplayName("존재하지 않는 플랜 조회 시 예외 발생")
    void getPlanDetail_ShouldThrowExceptionWhenPlanNotFound() {
        // given
        Long planId = 999L;
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> planService.getPlanDetail(planId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    private Plan createMockPlan(Long id, String planName, String categoryCode, String ageCode, Long tagCode) {
        Plan plan = mock(Plan.class);
        when(plan.getPlanId()).thenReturn(id);
        when(plan.getPlanName()).thenReturn(planName);
        when(plan.getCategoryCode()).thenReturn(categoryCode);
        when(plan.getAgeCode()).thenReturn(ageCode);
        when(plan.getTagCode()).thenReturn(tagCode);
        return plan;
    }
}

