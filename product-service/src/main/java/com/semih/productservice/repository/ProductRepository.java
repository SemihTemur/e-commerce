package com.semih.productservice.repository;

import com.semih.common.constant.EntityStatus;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categoryMappings")
    List<Product> findAllWithCategories();

    List<Product> findByIdIn(List<Long> productIds);
}
