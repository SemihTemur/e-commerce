package com.semih.basketservice.controller;

import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.service.BasketService;
import com.semih.common.dto.request.ProductQuantityRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(BASKETS)
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @PostMapping("/items")
    public ResponseEntity<String> addItem(@RequestBody ProductQuantityRequest request) {
        return ResponseEntity.ok(basketService.saveBasket(request));
    }

    @GetMapping("/items")
    public ResponseEntity<BasketResponse> getActiveBasket() {
        return ResponseEntity.ok(basketService.getActiveBasket());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        return ResponseEntity.ok(basketService.deleteBasketItemByActiveBasket(id));
    }
}
