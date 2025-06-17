package com.archiservice.user.service;

import com.archiservice.common.security.CustomUser;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.user.domain.User;
import com.archiservice.user.dto.request.ReservationRequestDto;
import com.archiservice.user.dto.response.ContractDetailResponseDto;
import com.archiservice.user.enums.Period;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    void createContract(ReservationRequestDto requestDto, User user);

    PlanDetailResponseDto getPlan(Period period, CustomUser customUser);
    VasDetailResponseDto getVas(Period period, CustomUser customUser);
    CouponDetailResponseDto getCoupon(Period period, CustomUser customUser);
    List<ContractDetailResponseDto> getContract (Period period, CustomUser customUser);

    void cancelNextContract(CustomUser customUser);
    void updateNextContract(ReservationRequestDto requestDto, CustomUser customUser);

    void determineContractAction(ReservationRequestDto requestDto, CustomUser customUser);

    void renewContract(LocalDate today);
}
