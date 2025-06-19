package com.archiservice.product.vas.repository;

import com.archiservice.product.vas.domain.Vas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VasRepository extends JpaRepository<Vas, Long> {
    List<Vas> findVasByCategoryCode(String categoryCode);
}
