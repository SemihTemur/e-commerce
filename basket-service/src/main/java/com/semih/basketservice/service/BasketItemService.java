package com.semih.basketservice.service;

import com.semih.basketservice.client.ProductClient;
import com.semih.basketservice.dto.request.BasketRequest;
import com.semih.basketservice.dto.response.BasketItemResponse;
import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketItem;
import com.semih.basketservice.exception.BasketItemNotFoundException;
import com.semih.basketservice.repository.BasketItemRepository;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import com.semih.common.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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

    public String saveBasketItem(BasketRequest basketRequest) {
        // Aktif sepeti al
        Basket basket = basketService.findByActiveBasket();

        // Sepetteki itemi bul
        BasketItem basketItem = basketItemRepository.findByProductId(basketRequest.productId());

        // Ürün miktarını doğrula
        validateProductQuantity(basketRequest, basketItem);

        // Eğer item yoksa oluştur
        if (basketItem == null) {
            basketItem = createBasketItem(basketRequest, basket);
        } else {
            // Eğer varsa, mevcut quantity ile toplama
            basketItem.setQuantity(basketItem.getQuantity() + basketRequest.quantity());
        }

        // Kaydet
        basketItemRepository.save(basketItem);

        return "Successfully";
    }

    public BasketResponse getBasketItemList(){
        Basket basket = basketService.findByActiveBasket();

        List<BasketItem> basketItemList = basketItemRepository.findByBasket(basket)
                .orElseThrow(()-> new BasketItemNotFoundException("Böyle bir Basket Item bulunamadı"));

        List<BasketItemResponse> basketItemResponseList = new ArrayList<>();
        BigDecimal basketTotal = BigDecimal.ZERO;

        for (BasketItem basketItem : basketItemList) {
            basketTotal = calculateBasketItem(basketItem, basketItemResponseList, basketTotal);
        }

        return new BasketResponse(basketItemResponseList,basketTotal);

    }

    public String deleteBasketItem(Long productId){
        Basket basket = basketService.findByActiveBasket();

        BasketItem basketItem = basketItemRepository.findByBasketAndProductId(basket,productId)
                .orElseThrow(()-> new BasketItemNotFoundException("Böyle bir Basket Item bulunamadı"));

        basketItemRepository.delete(isExistProductOrThrowException(basketItem,productId));

        return "Successfully";
    }

    private BasketItem isExistProductOrThrowException(BasketItem basketItem,Long productId){
        if(basketItem.getProductId().equals(productId))
                return basketItem;

        throw new ProductNotFoundException("Silmek istediğiniz ürün bulunamadı!");
    }

    private ProductQuantityRequest mapToProductQuantityRequest(Long productId,Integer quantity){
        return new ProductQuantityRequest(productId,quantity);
    }

    private BasketItem mapToBasketItemEntity(BasketRequest basketRequest){
        return new BasketItem(basketRequest.productId(),basketRequest.quantity());
    }

    private void validateProductQuantity(BasketRequest basketRequest, BasketItem basketItem) {
        int totalQuantity = basketItem != null
                ? basketItem.getQuantity() + basketRequest.quantity()
                : basketRequest.quantity();

        ProductQuantityRequest productQuantityRequest = new ProductQuantityRequest(
                basketRequest.productId(),
                totalQuantity
        );

        productClient.checkAvailabilityByProductId(productQuantityRequest);
    }

    private BasketItem createBasketItem(BasketRequest basketRequest, Basket basket) {
        BasketItem basketItem = mapToBasketItemEntity(basketRequest);
        basketItem.setBasket(basket);
        return basketItem;
    }

    private BigDecimal calculateBasketItem(BasketItem basketItem,
                                           List<BasketItemResponse> basketItemResponseList,
                                           BigDecimal basketTotal) {

        BasketProductResponse basketProductResponse = Objects.requireNonNull(
                productClient.getBasketProductResponse(basketItem.getProductId()).getBody()
        );

        BigDecimal lineTotal = basketProductResponse.productPrice()
                .multiply(BigDecimal.valueOf(basketItem.getQuantity()));

        BasketItemResponse basketItemResponse = new BasketItemResponse(basketProductResponse, basketItem.getQuantity(),
                lineTotal);
        basketItemResponseList.add(basketItemResponse);

        return basketTotal.add(lineTotal);
    }
}
