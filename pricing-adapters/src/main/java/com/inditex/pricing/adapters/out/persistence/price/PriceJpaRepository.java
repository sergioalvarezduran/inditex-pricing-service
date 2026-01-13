package com.inditex.pricing.adapters.out.persistence.price;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
  SELECT p FROM PriceEntity p
  WHERE p.brandId = :brandId
    AND p.productId = :productId
    AND :applicationDate BETWEEN p.startDate AND p.endDate
  ORDER BY p.priority DESC, p.startDate DESC
""")
    List<PriceEntity> findApplicable(
            @Param("brandId") long brandId,
            @Param("productId") long productId,
            @Param("applicationDate") LocalDateTime applicationDate,
            Pageable pageable
    );
}