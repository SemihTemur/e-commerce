package com.semih.productservice.repository;

import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT pcm.category_id,pcm.sub_category_id FROM product p " +
            "INNER JOIN product_category_mapping pcm ON pcm.product_id = p.id " +
            "WHERE p.id = :productId AND pcm.category_id = :categoryId",
            nativeQuery = true)
    List<ProductCategoryMapping> findByProductIdAndCategoryId(Long productId, Long categoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categoryMappings")
    List<Product> findAllWithCategories();

    List<Product> findByIdIn(List<Long> productIds);
}
