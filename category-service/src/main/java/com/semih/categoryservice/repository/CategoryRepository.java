package com.semih.categoryservice.repository;


import com.semih.categoryservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.subCategory sc " +
            "WHERE c.id IN :categoryIds " +
            "AND sc.id IN :allSubCategoryIds")
    List<Category> findSpecificCategoriesAndSubCategories(
            @Param("categoryIds") Set<Long> categoryIds,
            @Param("allSubCategoryIds") Set<Long> allSubCategoryIds);

}
