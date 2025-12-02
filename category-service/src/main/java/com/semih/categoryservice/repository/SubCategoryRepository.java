package com.semih.categoryservice.repository;

import com.semih.categoryservice.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {

}
