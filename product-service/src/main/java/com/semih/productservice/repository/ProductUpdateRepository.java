package com.semih.productservice.repository;

import com.semih.productservice.entity.ProductUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductUpdateRepository extends JpaRepository<ProductUpdate,Long> {

    Optional<ProductUpdate> findByProductId(Long productId);

    @Query("""
    SELECT pu FROM ProductUpdate pu
    WHERE pu.productId = :productId
    AND pu.operationStatus = 'PENDING'
""")
    Optional<ProductUpdate> findPendingUpdateByProductId(Long productId);


}
