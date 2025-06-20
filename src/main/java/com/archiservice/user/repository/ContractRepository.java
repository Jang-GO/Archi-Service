package com.archiservice.user.repository;

import com.archiservice.user.domain.Contract;
import com.archiservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query(value = "SELECT pb.plan_id " +
            "FROM contracts c " +
            "JOIN product_bundles pb ON c.product_bundle_id = pb.product_bundle_id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.contract_id DESC " +
            "LIMIT 1 OFFSET :offset", nativeQuery = true)
    Optional<Long> findPlanIdByOffset(@Param("userId") Long userId, @Param("offset") int offset);

    @Query(value = "SELECT pb.vas_id " +
            "FROM contracts c " +
            "JOIN product_bundles pb ON c.product_bundle_id = pb.product_bundle_id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.contract_id DESC " +
            "LIMIT 1 OFFSET :offset", nativeQuery = true)
    Optional<Long> findVasIdByOffset(@Param("userId") Long userId, @Param("offset") int offset);

    @Query(value = "SELECT pb.coupon_id " +
            "FROM contracts c " +
            "JOIN product_bundles pb ON c.product_bundle_id = pb.product_bundle_id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.contract_id DESC " +
            "LIMIT 1 OFFSET :offset", nativeQuery = true)
    Optional<Long> findCouponIdByOffset(@Param("userId") Long userId, @Param("offset") int offset);

    List<Contract> findTop2ByUserOrderByIdDesc(User user);
    Contract findTop1ByUserOrderByIdDesc(User user);
}
