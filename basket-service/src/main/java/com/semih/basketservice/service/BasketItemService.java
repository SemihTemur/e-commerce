package com.semih.basketservice.service;

import com.semih.basketservice.client.ProductClient;
import com.semih.basketservice.dto.request.BasketRequest;
import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketItem;
import com.semih.basketservice.exception.BasketItemNotFoundException;
import com.semih.basketservice.repository.BasketItemRepository;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BasketItemService {

    private final BasketService basketService;
    private final BasketItemRepository basketItemRepository;
    private final ProductClient productClient;

    public BasketItemService(BasketService basketService, BasketItemRepository basketItemRepository, ProductClient productClient) {
        this.basketService = basketService;
        this.basketItemRepository = basketItemRepository;
        this.productClient = productClient;
    }

    public String saveBasketItem(BasketRequest basketRequest){
        Basket basket = basketService.findByActiveBasket();

        ProductQuantityRequest productQuantityRequest = new ProductQuantityRequest
                (basketRequest.productId(), basketRequest.quantity());

        productClient.checkAvailabilityByProductId(productQuantityRequest);

        BasketItem basketItem = mapToBasketItemEntity(basketRequest);
        basketItem.setBasket(basket);

        basketItemRepository.save(basketItem);

        return "Succesfully";
    }

    public List<BasketResponse> getBasketItem(){
        Basket basket = basketService.findByActiveBasket();

        List<BasketItem> basketItemList = basketItemRepository.findByBasket(basket)
                .orElseThrow(()-> new BasketItemNotFoundException("Böyle bir Basket Item bulunamadı"));



    }


    public String deleteBasketItem(Long productId){
        Basket basket = basketService.findByActiveBasket();

        List<BasketItem> basketItemList = basketItemRepository.findByBasket(basket)
                .orElseThrow(()-> new BasketItemNotFoundException("Böyle bir Basket Item bulunamadı"));

        isExistProductOrThrowException(basketItemList,productId);

        basketItemRepository.saveAll(basketItemList);

        return "Successfully";
    }

    private void isExistProductOrThrowException(List<BasketItem> basketItemList,Long productId){
        BasketItem isExistBasketItem = null;
        for(BasketItem basketItem:basketItemList){
            if(basketItem.getProductId().equals(productId)){
                isExistBasketItem = basketItem;
                break;
            }
        }

        if(isExistBasketItem==null)
            throw new ProductNotFoundException("Silmek istediğiniz ürün bulunamadı!");

        basketItemList.remove(isExistBasketItem);
    }

    private ProductQuantityRequest mapToProductQuantityRequest(Long productId,Integer quantity){
        return new ProductQuantityRequest(productId,quantity);
    }

    private BasketItem mapToBasketItemEntity(BasketRequest basketRequest){
        return new BasketItem(basketRequest.productId(),basketRequest.quantity());
    }

}
