package com.semih.productservice.repository;

import com.semih.productservice.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    List<OutboxMessage> findByProcessedFalseOrderByCreatedAtAsc();

}
