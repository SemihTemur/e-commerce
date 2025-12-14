package com.semih.basketservice.repository;

import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket,Long> {

    Optional<Basket> findByStatusAndUserId(BasketStatus basketStatus,String userId);

}
