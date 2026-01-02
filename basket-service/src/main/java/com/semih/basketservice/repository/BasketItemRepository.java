package com.semih.basketservice.repository;

import com.semih.basketservice.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {

    Optional<BasketItem> findByProductId(Long productId);


}
