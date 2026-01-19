package com.semih.productservice.entity;

import com.semih.common.constant.OutboxEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "outbox")
@Entity
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String aggregateType = "PRODUCT"; // PRODUCT

    @Column(nullable = false)
    private Long aggregateId; // ProductId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Object payload;

    private boolean processed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public OutboxMessage() {
    }

    public OutboxMessage(Long aggregateId, OutboxEventType type, Object payload) {
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public OutboxEventType getType() {
        return type;
    }

    public void setType(OutboxEventType type) {
        this.type = type;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Long aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
