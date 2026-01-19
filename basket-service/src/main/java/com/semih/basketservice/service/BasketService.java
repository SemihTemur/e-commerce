package com.semih.basketservice.service;

import com.semih.basketservice.client.InventoryClient;
import com.semih.basketservice.client.OrderClient;
import com.semih.basketservice.client.ProductClient;
import com.semih.basketservice.dto.response.BasketItemResponse;
import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketItem;
import com.semih.basketservice.entity.BasketStatus;
import com.semih.basketservice.exception.BasketItemNotFoundException;
import com.semih.basketservice.exception.BasketNotFoundException;
import com.semih.basketservice.repository.BasketRepository;
import com.semih.common.dto.request.OrderItemRequest;
import com.semih.common.dto.request.OrderRequest;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import com.semih.common.dto.response.ProductLineItemResponse;
import com.semih.common.exception.StockNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderClient orderClient;

    public BasketService(BasketRepository basketRepository, ProductClient productClient,
                         InventoryClient inventoryClient, OrderClient orderClient) {
        this.basketRepository = basketRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.orderClient = orderClient;
    }

    @Transactional
    public String saveBasket(ProductQuantityRequest productQuantityRequest) {
        // Aktif sepeti al
        Basket basket = findOrCreateActiveBasket();

        // Sepetteki itemi bul
        BasketItem basketItem = findByBasketItemByProductId(basket.getBasketItems(),
                productQuantityRequest.productId());

        // Ürün miktarını doğrula. inventory-service gidip
        validateProductQuantity(productQuantityRequest, basketItem);

        // Eğer item yoksa oluştur
        if (basketItem == null) {
            basketItem = createBasketItem(productQuantityRequest, basket);
            basket.addItem(basketItem);
        } else {
            // Eğer varsa, mevcut quantity ile toplama
            basketItem.setQuantity(basketItem.getQuantity() + productQuantityRequest.quantity());
        }

        // Kaydet
        basketRepository.save(basket);

        return "Successfully";
    }

    @Transactional
    public String checkoutBasket(){
        Basket activeBasket = findActiveBasketOrNull();
        List<BasketItem> basketItemList = activeBasket.getBasketItems();

        List<ProductQuantityRequest> productQuantityRequestList = createProductQuantityRequestList(
                basketItemList);

        List<ProductLineItemResponse> productLineItemResponseList = productClient.
                priceProductsForCheckout(productQuantityRequestList).getBody();

        OrderRequest orderRequest = createOrderRequest(productLineItemResponseList);
        orderClient.createOrder(orderRequest);

        activeBasket.setStatus(BasketStatus.ORDERED);

        return "Succesfully";
    }


    @Transactional(readOnly = true)
    public BasketResponse getActiveBasket() {
        Basket basket = findActiveBasketOrNull();

        List<BasketItemResponse> basketItemResponseList = new ArrayList<>();

        BigDecimal basketTotal = calculateBasketItem(basket.getBasketItems(), basketItemResponseList);

        return new BasketResponse(basketItemResponseList, basketTotal);
    }

    @Transactional
    public String deleteBasketItemByActiveBasket(Long id) {
        Basket basket = findActiveBasketOrNull();

        BasketItem basketItem = findByBasketItemByProductId(basket.getBasketItems(), id);

        if (basketItem == null)
            throw new BasketItemNotFoundException("Basket item bulunamadı!!!");

        basketItem.setBasket(null);
        basket.getBasketItems().remove(basketItem);

        return "Successfully";
    }

    private String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || authentication.getPrincipal()==null){
            throw new RuntimeException("Böyle bir Kullanıcı yoktur");
        }

        return (String) authentication.getPrincipal();
    }

    private Basket findOrCreateActiveBasket() {
        String userId = getUserId();

        Optional<Basket> basket = basketRepository.findActiveBasketWithItems(BasketStatus.ACTIVE, userId);

        return basket.orElseGet(() -> basketRepository.save(new Basket(userId, BasketStatus.ACTIVE)));
    }

    private Basket findActiveBasketOrNull() {;
        String userId = getUserId();

        Optional<Basket> basket = basketRepository.findActiveBasketWithItems(BasketStatus.ACTIVE, userId);
        return basket.orElseThrow(() -> new BasketNotFoundException("Aktif Basket Bulunamadı"));
    }

    private BasketItem findByBasketItemByProductId(List<BasketItem> basketItemList, Long id) {
        for (BasketItem basketItem : basketItemList) {
            if (basketItem.getProductId().equals(id))
                return basketItem;
        }

        return null;
    }

    private BasketItem mapToBasketItemEntity(ProductQuantityRequest productQuantityRequest) {
        return new BasketItem(productQuantityRequest.productId(), productQuantityRequest.quantity());
    }

    private void validateProductQuantity(ProductQuantityRequest productQuantityRequest,
                                         BasketItem basketItem) {
        int totalQuantity = basketItem != null
                ? basketItem.getQuantity() + productQuantityRequest.quantity()
                : productQuantityRequest.quantity();

        ProductQuantityRequest newProductQuantityRequest = new ProductQuantityRequest(
                productQuantityRequest.productId(),
                totalQuantity
        );

        inventoryClient.checkAvailabilityByProductId(newProductQuantityRequest);
    }

    private BasketItem createBasketItem(ProductQuantityRequest productQuantityRequest, Basket basket) {
        BasketItem newBasketItem = mapToBasketItemEntity(productQuantityRequest);
        newBasketItem.setBasket(basket);
        return newBasketItem;
    }

    private BigDecimal calculateBasketItem(List<BasketItem> basketItemList,
                                           List<BasketItemResponse> basketItemResponseList) {

        List<Long> productIdList = basketItemList.stream()
                .map(BasketItem::getProductId)
                .toList();

        List<BasketProductResponse> basketProductResponseList = Objects.requireNonNull(
                productClient.getBasketProductResponse(productIdList).getBody()
        );

        Map<Long, BasketProductResponse> basketProductResponseMap = basketProductResponseList.stream()
                .collect(Collectors.toMap(
                        BasketProductResponse::productId,
                        Function.identity()
                ));

        BigDecimal basketTotal = BigDecimal.ZERO;

        for (BasketItem basketItem : basketItemList) {
            BasketProductResponse basketProductResponse = basketProductResponseMap
                    .get(basketItem.getProductId());

            if (basketProductResponse == null) {
                throw new StockNotFoundException("Product not found: " + basketItem.getProductId());
            }

            BigDecimal lineTotal = basketProductResponse.productPrice()
                    .multiply(BigDecimal.valueOf(basketItem.getQuantity()));

            BasketItemResponse basketItemResponse = new BasketItemResponse(
                    basketItem.getId(),
                    basketProductResponse,
                    basketItem.getQuantity(),
                    lineTotal
            );

            basketItemResponseList.add(basketItemResponse);

            basketTotal = basketTotal.add(lineTotal);
        }

        return basketTotal;
    }

    private List<ProductQuantityRequest> createProductQuantityRequestList(List<BasketItem> basketItemList){
        List<ProductQuantityRequest> productQuantityRequestList = new ArrayList<>();

        for(BasketItem basketItem : basketItemList){
            productQuantityRequestList.add(
                    new ProductQuantityRequest(
                            basketItem.getProductId(),
                            basketItem.getQuantity()
                    )
            );
        }

        return productQuantityRequestList;
    }
//    private Map<Long, ProductQuantityRequest> getAggregatedProductQuantities(
//            List<ProductQuantityRequest> requests) {
//        Map<Long, ProductQuantityRequest> maps = new HashMap<>();
//
//        for (ProductQuantityRequest request : requests) {
//            maps.merge(request.productId(), request, (oldValue, newValue) ->
//                    new ProductQuantityRequest(
//                            request.productId(),
//                            oldValue.quantity() + newValue.quantity()
//                    )
//            );
//        }
//
//        return maps;
//    }

    private OrderRequest createOrderRequest(
            List<ProductLineItemResponse> productLineItemResponseList) {

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemRequest> orderItemRequests = new ArrayList<>();

        for (ProductLineItemResponse productLineItemResponse : productLineItemResponseList) {

            BigDecimal lineTotal = productLineItemResponse.unitPrice()
                    .multiply(BigDecimal.valueOf(productLineItemResponse.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            orderItemRequests.add(
                    new OrderItemRequest(
                            productLineItemResponse.productId(),
                            productLineItemResponse.productName(),
                            productLineItemResponse.unitPrice(),
                            productLineItemResponse.quantity(),
                            lineTotal
                    )
            );

            totalAmount = totalAmount.add(lineTotal);
        }

        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        return new OrderRequest(totalAmount, orderItemRequests);
    }

}
