package com.semih.inventoryservice.repository;

import com.semih.inventoryservice.document.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, UUID> {
}
