package com.semih.productservice.entity;

import com.semih.common.constant.EntityStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productDescription;

    @Column(nullable = false)
    private BigDecimal productPrice;

    @Enumerated(EnumType.STRING)
    private EntityStatus status = EntityStatus.PENDING;

    private String statusReason = "Product registration created, processing...";

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "product_category_mapping_unique",
                    columnNames = {"product_id","category_id", "sub_category_id"}
            )
    )
    private List<ProductCategoryMapping> categoryMappings = new ArrayList<>();


    public Product() {
    }

    public Product(String sellerId, String productName, String productDescription,
                   BigDecimal productPrice, String statusReason) {
        this.sellerId = sellerId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.statusReason = statusReason;
    }

    public Product(String sellerId, String productName, String productDescription, BigDecimal productPrice,
                   String statusReason, List<ProductCategoryMapping> categoryMappings) {
        this.sellerId = sellerId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.statusReason = statusReason;
        this.categoryMappings = categoryMappings;
    }

    public Long getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ProductCategoryMapping> getCategoryMappings() {
        return categoryMappings;
    }

    public void setCategoryMappings(List<ProductCategoryMapping> categoryMappings) {
        this.categoryMappings = categoryMappings;
    }
}
