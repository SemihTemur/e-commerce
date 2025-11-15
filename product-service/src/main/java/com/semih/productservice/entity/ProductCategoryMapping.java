package com.semih.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductCategoryMapping {

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "sub_category_id",nullable = false)
    private Long subCategoryId;

    public ProductCategoryMapping() {}
    public ProductCategoryMapping(Long categoryId, Long subCategoryId) {
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
}
