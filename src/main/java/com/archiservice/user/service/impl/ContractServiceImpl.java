package com.archiservice.user.service.impl;

import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.ContractNotFoundException;
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
import com.archiservice.user.service.ContractService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractCustomRepository contractCustomRepository;
    private final PlanService planService;
    private final VasService vasService;
    private final CouponService couponService;
    private final ProductBundleRepository productBundleRepository;

    @Override
    @Transactional
    // Issue : Scheduling Batch, User 를 받는 방식 고민 (시스템이 자동 추가해야하므로, 유저가 직접 추가하는 방식이 아님 -> 현재 컨트롤러가 아닌, 신규 번들 생성 이후 연계로 실행되는 형태)
    public void createContract(ReservationRequestDto requestDto, User user) {
        // 다음달 계약이 생성되는 시점에는 가장 최근건이 이번달 계약임. -> top1 사용

        ProductBundle bundle = productBundleRepository.findById(requestDto.getBundleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "조합 정보가 존재하지 않음"));

        Contract curContract = contractRepository.findTop1ByUserOrderByIdDesc(user);

        Contract newContract = Contract.builder()
                .productBundle(bundle)
                .user(user)
                .paymentMethod(curContract.getPaymentMethod())
                .price(requestDto.getPrice())
                .startDate(curContract.getEndDate())
                .endDate(curContract.getEndDate().plusMonths(1))
                .build();

        contractRepository.save(newContract);
    }

    @Override
    public PlanDetailResponseDto getPlan(Period period, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        Long planId = contractRepository.findPlanIdByOffset(user.getUserId(), period.getOffset())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        PlanDetailResponseDto planDetailResponseDto = planService.getPlanDetail(planId);

        return planDetailResponseDto;
    }

    @Override
    public VasDetailResponseDto getVas(Period period, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        Long vasId = contractRepository.findVasIdByOffset(user.getUserId(), period.getOffset())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        VasDetailResponseDto vasDetailResponseDto = vasService.getVasDetail(vasId);

        return vasDetailResponseDto;
    }

    @Override
    public CouponDetailResponseDto getCoupon(Period period, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        Long couponId = contractRepository.findCouponIdByOffset(user.getUserId(), period.getOffset())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        CouponDetailResponseDto couponDetailResponseDto = couponService.getCouponDetail(couponId);

        return couponDetailResponseDto;
    }

    @Override
    public List<ContractDetailResponseDto> getContract(Period period, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        List<ContractDetailResponseDto> contractDetailResponseListDto = contractCustomRepository.findContractByOffset(user, period);

        return contractDetailResponseListDto;
    }

    @Override
    @Transactional
    public void cancelNextContract(CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        List<Contract> contractList = contractRepository.findTop2ByUserOrderByIdDesc(user);
        Contract newContract = contractList.get(0);
        Contract curContract = contractList.get(1);

        newContract.copyFrom(curContract);
    }

    @Override
    @Transactional
    public void updateNextContract(ReservationRequestDto requestDto, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        ProductBundle bundle = productBundleRepository.findById(requestDto.getBundleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "조합 정보가 존재하지 않음"));

        Contract nextContract = contractRepository.findTop1ByUserOrderByIdDesc(user);
        nextContract.updateNextContract(bundle, requestDto.getPrice());
    }

    @Override
    public void determineContractAction(ReservationRequestDto requestDto, CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException());

        Contract recentContract = contractRepository.findTop1ByUserOrderByIdDesc(user);
        LocalDate today = LocalDate.now();
        LocalDate endDate = recentContract.getEndDate().toLocalDate();

        if(today.equals(endDate)) {
            createContract(requestDto, user);
        } else {
            updateNextContract(requestDto, customUser);
        }
    }

    @Override
    @Transactional
    public void renewContract(LocalDate today) {
        List<Contract> expiredContractList = contractRepository.findByEndDate(today)
                .orElseThrow(() -> new ContractNotFoundException(ErrorCode.CONTRACT_NOT_FOUND.getMessage()));

        for(Contract contract : expiredContractList) {
            User user = contract.getUser();

            Contract renewedContract = Contract.builder()
                    .productBundle(contract.getProductBundle())
                    .user(user)
                    .paymentMethod(contract.getPaymentMethod())
                    .price(contract.getPrice())
                    .startDate(today.atStartOfDay().plusDays(1))
                    .endDate((today.atStartOfDay().plusMonths(1)))
                    .build();

            contractRepository.save(renewedContract);
        }

    }
}
