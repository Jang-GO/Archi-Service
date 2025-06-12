package com.archiservice.user.service;

import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.product.coupon.dto.response.CouponDetailResponseDto;
import com.archiservice.product.plan.dto.response.PlanDetailResponseDto;
import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import com.archiservice.user.domain.User;
import com.archiservice.user.dto.request.ReservationRequestDto;
import com.archiservice.user.dto.response.ContractDetailResponseDto;
import com.archiservice.user.dto.response.ReservationResponseDto;
import com.archiservice.user.enums.Period;

import java.util.List;

public interface ContractService {
    void createContract(ReservationRequestDto requestDto, User user);

    ApiResponse<PlanDetailResponseDto> getPlan(Period period, CustomUser customUser);
    ApiResponse<VasDetailResponseDto> getVas(Period period, CustomUser customUser);
    ApiResponse<CouponDetailResponseDto> getCoupon(Period period, CustomUser customUser);
    List<ContractDetailResponseDto> getContract (Period period, CustomUser customUser);

    ReservationResponseDto cancelNextContract(CustomUser customUser); // 예약 취소 버튼을 누르면 예약을 이번달과 같게 하는 서비스
    ReservationResponseDto updateNextContract(ReservationRequestDto requestDto, CustomUser customUser); // 예약 변경 버튼을 누르면 예약을 해당 조합으로 바꾸는 서비스

    void determineContractAction(ReservationRequestDto requestDto, CustomUser customUser);
}
