package com.semih.productservice.repository;

import com.semih.productservice.entity.ProductDelete;
import com.semih.productservice.entity.ProductUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductDeleteRepository extends JpaRepository<ProductDelete,Long> {

    @Query("""
    SELECT pu FROM ProductDelete pu
    WHERE pu.productId = :productId
    AND pu.operationStatus = 'PENDING'
""")
    Optional<ProductDelete> findPendingDeleteByProductId(Long productId);

}
