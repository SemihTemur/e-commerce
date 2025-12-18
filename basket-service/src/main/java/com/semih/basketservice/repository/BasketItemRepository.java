package com.semih.basketservice.repository;

import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {

    Optional<BasketItem> findByBasketAndProductId(Basket basket, Long productId);

    Optional<List<BasketItem>> findByBasket(Basket basket);

    BasketItem findByProductId(Long productId);

}
