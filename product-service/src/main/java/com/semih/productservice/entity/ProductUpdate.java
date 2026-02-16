package com.semih.productservice.entity;

import com.semih.productservice.constant.OperationStatus;
import com.semih.productservice.constant.ProductStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_update")
public class ProductUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String name;

    private String productDescription;

    private BigDecimal price;;

    @Enumerated(EnumType.STRING)
    private OperationStatus operationStatus;
    // PENDING → ACTIVE / REJECTED

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ProductUpdate() {
    }

    public ProductUpdate(Long productId, String name, String productDescription,BigDecimal price,
                         OperationStatus operationStatus) {
        this.productId = productId;
        this.name = name;
        this.productDescription = productDescription;
        this.price = price;
        this.operationStatus = operationStatus;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}

