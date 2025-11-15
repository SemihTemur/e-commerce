package com.semih.categoryservice.repository;

import com.semih.categoryservice.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {
}
