package com.semih.inventoryservice.repository;

import com.semih.inventoryservice.document.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, Integer> {

    Optional<Inventory> findByProductId(Long productId);

}
