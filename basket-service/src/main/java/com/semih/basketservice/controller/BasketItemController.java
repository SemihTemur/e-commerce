package com.semih.basketservice.controller;

import com.semih.basketservice.dto.request.BasketRequest;
import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.service.BasketItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.semih.basketservice.config.RestApis.*;

@RestController
@RequestMapping(BASKET_SERVICE)
public class BasketItemController {

    private final BasketItemService basketItemService;

    public BasketItemController(BasketItemService basketItemService) {
        this.basketItemService = basketItemService;
    }

    @PostMapping(ADD_BASKET_ITEM)
    public ResponseEntity<String> saveBasketItem(@RequestBody BasketRequest basketRequest){
        String message = basketItemService.saveBasketItem(basketRequest);
        return ResponseEntity.ok(message);
    }

    @GetMapping(GET_BASKET_ITEM_LIST)
    public ResponseEntity<BasketResponse> getBasketItemList(){
        BasketResponse basketResponse = basketItemService.getBasketItemList();
        return ResponseEntity.ok(basketResponse);
    }

    @DeleteMapping(DELETE_BASKET_ITEM)
    public ResponseEntity<String> deleteBasketItem(@PathVariable Long id){
        String message = basketItemService.deleteBasketItem(id);
        return ResponseEntity.ok(message);
    }


}
