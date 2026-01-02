package com.semih.orderservice.repository;

import com.semih.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.userId = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdWithItems(@Param("userId") String userId);

}
