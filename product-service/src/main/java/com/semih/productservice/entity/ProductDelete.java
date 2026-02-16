package com.semih.productservice.entity;

import com.semih.productservice.constant.OperationStatus;
import com.semih.productservice.constant.ProductStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_delete")
public class ProductDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private OperationStatus operationStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ProductDelete() {
    }

    public ProductDelete(Long productId, OperationStatus operationStatus) {
        this.productId = productId;
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