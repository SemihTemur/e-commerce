package com.semih.basketservice.service;

import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketStatus;
import com.semih.basketservice.repository.BasketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BasketService {

    private final BasketRepository basketRepository;

    public BasketService(BasketRepository basketRepository) {
        this.basketRepository = basketRepository;
    }

    public Basket findByActiveBasket(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Optional<Basket> basket = basketRepository.findByStatusAndUserId(BasketStatus.ACTIVE,userId);

        return basket.orElseGet(() -> basketRepository.save(new Basket(userId, BasketStatus.ACTIVE)));
    }

}
