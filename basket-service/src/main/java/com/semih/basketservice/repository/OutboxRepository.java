package com.semih.basketservice.repository;

import com.semih.basketservice.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxMessage,Long> {
    List<OutboxMessage> findByProcessedFalse();
}
