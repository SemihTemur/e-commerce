package com.semih.basketservice.entity;

import com.semih.common.dto.request.BasketEvent;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "outboxMessage")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long aggregateId;    // Sadece basketId'yi tutsak yeterli

    @JdbcTypeCode(SqlTypes.JSON)
// Veritabanına gönderirken Hibernate bu alanın JSON olduğunu bilir ve JSON olarak bind eder
    @Column(columnDefinition = "jsonb")
// PostgreSQL’de jsonb tipinde tutulur (içeride binary formatta saklanır ve indexlenebilir)
    private BasketEvent payload;

    private boolean processed = false;

    public OutboxMessage() {}

    public OutboxMessage(Long aggregateId, BasketEvent payload) {
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.processed = false;
    }

    public Long getId() {
        return id;
    }

    public Long getAggregateId() { return aggregateId; }
    public BasketEvent getPayload() { return payload; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}
