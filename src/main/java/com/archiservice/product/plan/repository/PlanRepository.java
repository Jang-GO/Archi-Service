package com.archiservice.product.plan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.archiservice.product.plan.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
	Optional<Plan> findByPlanName(String planName);
}
