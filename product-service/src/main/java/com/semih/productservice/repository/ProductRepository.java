package com.semih.productservice.repository;

import com.semih.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("""
       SELECT DISTINCT p
       FROM Product p
       LEFT JOIN FETCH p.categoryMappings
       WHERE p.status = 'ACTIVE'
       """)
    List<Product> findAllWithCategories();

    @Query("""
       SELECT p FROM Product p
       WHERE p.id = :productId
       AND p.status = 'REJECTED'
       """)
    Optional<Product> findRejectedProductById(Long productId);

    List<Product> findByIdIn(List<Long> productIds);
}
