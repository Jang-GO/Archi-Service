package com.archiservice.review.coupon.service.impl;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.exception.business.AlreadyReviewedException;
import com.archiservice.exception.business.ReviewNotFoundException;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.product.coupon.domain.Coupon;
import com.archiservice.product.coupon.repository.CouponRepository;
import com.archiservice.recommend.dto.response.ScoreResponseDto;
import com.archiservice.review.coupon.domain.CouponReview;
import com.archiservice.review.coupon.dto.request.CouponReviewRequestDto;
import com.archiservice.review.coupon.dto.response.CouponReviewResponseDto;
import com.archiservice.review.coupon.repository.CouponReviewRepository;
import com.archiservice.review.coupon.service.CouponReviewService;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponReviewServiceImpl implements CouponReviewService {

    private final CouponReviewRepository couponReviewRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public CouponReviewResponseDto createReview(Long userId, Long couponId, CouponReviewRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (couponReviewRepository.existsByUserAndCoupon(user, coupon)) {
            throw new AlreadyReviewedException(ErrorCode.ALREADY_REVIEWED.getMessage());
        }

        CouponReview review = CouponReview.builder()
                .user(user)
                .coupon(coupon)
                .score(requestDto.getScore())
                .content(requestDto.getContent())
                .isModerated(false)
                .build();

        CouponReview savedReview = couponReviewRepository.save(review);
        return CouponReviewResponseDto.from(savedReview);
    }

    @Transactional
    public CouponReviewResponseDto updateReview(Long userId, Long reviewId, CouponReviewRequestDto requestDto) {
        CouponReview review = couponReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND.getMessage()));

        review.validateOwner(userId);
        review.updateReview(requestDto.getScore(), requestDto.getContent());

        return CouponReviewResponseDto.from(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        CouponReview review = couponReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND.getMessage()));

        review.validateOwner(userId);
        couponReviewRepository.delete(review);
    }

    @Override
    public Page<CouponReviewResponseDto> getReviewsByCouponId(Long couponId, Pageable pageable) {
        Page<CouponReview> reviews = couponReviewRepository.findByCouponIdWithUser(couponId, pageable);
        return reviews.map(CouponReviewResponseDto::from);
    }

    @Override
    public Map<Long, ScoreResponseDto> getCouponScoreStatistics() {
        List<Object[]> results = couponReviewRepository.findAverageScoreAndCountByCoupon();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // planId
                        result -> new ScoreResponseDto(
                                (Double) result[1],              // avgScore
                                ((Long) result[2]).intValue()   // reviewCount
                        )
                ));
    }

    @Override
    public Integer getAverageReviewCountPerCouponAsInteger() {
        Double avgReviewCount = couponReviewRepository.findAverageReviewCountPerCouponNative();
        return avgReviewCount != null ? (int) Math.round(avgReviewCount) : 0;
    }

}
