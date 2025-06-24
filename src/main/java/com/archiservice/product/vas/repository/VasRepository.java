package com.archiservice.product.vas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.archiservice.product.vas.domain.Vas;

public interface VasRepository extends JpaRepository<Vas, Long> {
    List<Vas> findVasByCategoryCode(String categoryCode);
    Optional<Vas> findByVasName(String vasName);
}
