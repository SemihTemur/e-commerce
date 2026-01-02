package com.semih.basketservice.repository;

import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket,Long> {

    @Query("SELECT b FROM Basket b " +
            "LEFT JOIN FETCH b.basketItems " +
            "WHERE b.status = :status AND b.userId = :userId")
    Optional<Basket> findActiveBasketWithItems(
            @Param("status") BasketStatus status,
            @Param("userId") String userId
    );

}
