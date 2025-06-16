package com.archiservice.product.bundle.service.impl;

import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.product.bundle.domain.ProductBundle;
import com.archiservice.product.bundle.dto.request.CreateBundleRequestDto;
import com.archiservice.product.bundle.dto.response.BundleCombinationResponseDto;
import com.archiservice.product.bundle.dto.response.BundleDetailResponseDto;
import com.archiservice.product.bundle.repository.ProductBundleRepository;
import com.archiservice.product.bundle.service.ProductBundleService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductBundleServiceImpl implements ProductBundleService {
    private final ProductBundleRepository productBundleRepository;
    private final PlanRepository planRepository;
    private final VasRepository vasRepository;
    private final CouponRepository couponRepository;
    private final PlanService planService;
    private final VasService vasService;
    private final CouponService couponService;
    private final ContractService contractService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createBundle(CreateBundleRequestDto requestDto, CustomUser customUser) {
        // 1. 번들에 추가하게되면, 사용자의 계약건에도 추가되어야함.
        // 2. 조합이 기존에 존재하는 조합이라면 Bundle insert 건너뛰고, 사용자 계약건에 추가 ( 3번 )
        // 3. 등록 가능기간, 불가능 기간 모두 예약건으로 계약 테이블에 추가
        Plan plan = planRepository.findById(requestDto.getPlanId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "요금제를 찾을 수 없음"));

        Vas vas = vasRepository.findById(requestDto.getVasId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "부가서비스를 찾을 수 없음"));

        Coupon coupon = couponRepository.findById(requestDto.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "라이프혜택을 찾을 수 없음"));

        long finalPrice = plan.getPrice() + vas.getDiscountedPrice() + coupon.getPrice();

        Optional<ProductBundle> optBundle = productBundleRepository.findByPlanAndVasAndCoupon(plan, vas, coupon);

        if(optBundle.isPresent()) {
            long bundleId = optBundle.get().getProductBundleId();

            ReservationRequestDto reservationRequestDto = ReservationRequestDto.builder()
                    .bundleId(bundleId)
                    .price(finalPrice)
                    .build();

            contractService.determineContractAction(reservationRequestDto, customUser);

        } else{
            ProductBundle newProductBundle = ProductBundle.builder()
                    .plan(plan)
                    .vas(vas)
                    .coupon(coupon)
                    // TODO :  tagCode 는 추천 조합팀 완성되면 추가
                    .build();

            productBundleRepository.save(newProductBundle);

            ReservationRequestDto reservationRequestDto = ReservationRequestDto.builder()
                    .bundleId(newProductBundle.getProductBundleId())
                    .price(finalPrice)
                    .build();

            contractService.determineContractAction(reservationRequestDto, customUser);
        }


    }

    @Override
    public BundleCombinationResponseDto getBundleByIds(long planId, long vasId, long couponId) {
        // 추천 조합 보기 - 조합 상세보기 : Session 에서 받아온 planId, vasId, couponId 로 상세 정보 구성
        // 좋아요, 싫어요, 생성일, 업데이트일 : bundle 이 존재하면 가져오고, 없으면 0, 0, null, null 로 갖고와야함.
        PlanDetailResponseDto planResponseDto = planService.getPlanDetail(planId);
        VasDetailResponseDto vasDetailResponseDto = vasService.getVasDetail(vasId);
        CouponDetailResponseDto couponDetailResponseDto = couponService.getCouponDetail(couponId);
        BundleDetailResponseDto bundleDetailResponseDto = null;
        Optional<ProductBundle> optBundle = productBundleRepository.findProductBundleByPlan_PlanIdAndVas_VasIdAndCoupon_CouponId(planId, vasId, couponId);

        if(optBundle.isPresent()) {
            ProductBundle bundle = optBundle.get();

            bundleDetailResponseDto = BundleDetailResponseDto.builder()
                    .bundleId(bundle.getProductBundleId())
                    .likeCount(bundle.getLikeCount())
                    .dislikeCount(bundle.getDislikeCount())
                    .tagCode(bundle.getTagCode())
                    .createAt(bundle.getCreatedAt())
                    .updateAt(bundle.getUpdatedAt())
                    .build();
        }

        BundleCombinationResponseDto bundleCombinationResponseDto = BundleCombinationResponseDto.builder()
                .planDetail(planResponseDto)
                .vasDetail(vasDetailResponseDto)
                .couponDetail(couponDetailResponseDto)
                .bundleDetail(bundleDetailResponseDto)
                .build();

        return bundleCombinationResponseDto;
    }

    @Override
    @Transactional
    public void updateLikeOrDislikeCount(long bundleId, boolean isLike) {
        // 사용자는 좋아요 / 싫어요 로 반응, 무반응 시 해당 메서드는 실행되지 않음
        // 사용자가 해당 조합에 이미 좋아요를 눌렀는지 체크필요 -> 현재 DB 구조로는 불가능 -> 좋아요 계속 누르면 계속 올라감, 프론트에서 한번 누르면 창 닫히도록 해야함.
        // 번들이 무조건 테이블에 존재한다는 가정하에 이루어지는 로직 ( 사용자가 조합을 사용하다보면 며칠 뒤에 챗봇이 선제안으로 평가 물어보는 방식 )
        int updatedRows;

        if(isLike){
            updatedRows = productBundleRepository.incrementLikeCount(bundleId);
        }else{
            updatedRows = productBundleRepository.incrementDislikeCount(bundleId);
        }

        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

    }

    @Override
    public long getCombinedTagCode(long planId, long vasId, long couponId) {

        long planTagCode = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"))
                .getTagCode();

        long vasTagCode = vasRepository.findById(vasId)
                .orElseThrow(() -> new EntityNotFoundException("VAS not found"))
                .getTagCode();

        long couponTagCode = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found"))
                .getTagCode();

        return planTagCode | vasTagCode | couponTagCode;
    }


}
