package com.archiservice.user.repository.custom;

import com.archiservice.user.domain.User;
import com.archiservice.user.dto.response.ContractDetailResponseDto;
import com.archiservice.user.enums.Period;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ContractCustomRepositoryImpl implements ContractCustomRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<ContractDetailResponseDto> findContractByOffset(User user, Period period) {
        String jpql = "SELECT new com.archiservice.user.dto.response.ContractDetailResponseDto(" +
                "c.paymentMethod, c.price, c.startDate, c.endDate, " +

                "pb.plan.planId, pb.plan.planName, pb.plan.price, pb.plan.categoryCode, " +
                "pb.plan.monthData, pb.plan.callUsage, pb.plan.messageUsage, " +

                "pb.vas.vasId , pb.vas.vasName, pb.vas.price, pb.vas.categoryCode, " +
                "pb.vas.vasDescription, pb.vas.saleRate, " +

                "pb.coupon.couponId , pb.coupon.couponName, pb.coupon.price, " +
                "pb.coupon.categoryCode " +
                ") " +
                "FROM Contract c " +
                "JOIN c.productBundle pb " +
                "WHERE c.user = :user " +
                "ORDER BY c.id DESC";

        return em.createQuery(jpql, ContractDetailResponseDto.class)
                .setParameter("user", user)
                .setFirstResult(period.getOffset())
                .setMaxResults(period.getLimit())
                .getResultList();
    }
}

