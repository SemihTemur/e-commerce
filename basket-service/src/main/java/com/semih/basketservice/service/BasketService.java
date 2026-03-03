package com.semih.basketservice.service;

import com.semih.basketservice.dto.response.BasketItemResponse;
import com.semih.basketservice.dto.response.BasketResponse;
import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketItem;
import com.semih.basketservice.entity.BasketStatus;
import com.semih.basketservice.exception.BasketItemNotFoundException;
import com.semih.basketservice.exception.BasketNotFoundException;
import com.semih.basketservice.repository.BasketRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BasketService {

    private final BasketManager basketManager;

    private final ProductClientService productClientService;

    private final InventoryClientService inventoryClientService;

    public BasketService(BasketManager basketManager,
                         ProductClientService productClientService,
                         InventoryClientService inventoryClientService) {
        this.basketManager = basketManager;
        this.productClientService = productClientService;
        this.inventoryClientService = inventoryClientService;
    }

    // feign client ve redis eklemeye calıs
    public String saveBasket(ProductQuantityRequest productQuantityRequest) {
        // Aktif sepeti al
        Basket basket = basketManager.findOrCreateActiveBasket();

        // Sepetteki itemi bul
        BasketItem basketItem = findByBasketItemByProductId(basket.getBasketItems(),
                productQuantityRequest.productId());

        validateProductQuantity(productQuantityRequest, basketItem);

        if (basketItem == null) {
            BasketItem newItem = createBasketItem(productQuantityRequest, basket);
            basket.addItem(newItem);
        } else {
            basketItem.setQuantity(basketItem.getQuantity() + productQuantityRequest.quantity());
        }

        basketManager.updateBasket(basket);

        return "Successfully";
    }

    public void checkoutBasket() {
        Basket activeBasket = basketManager.findOrCreateActiveBasket();
        List<BasketItem> basketItemList = activeBasket.getBasketItems();


        List<ProductQuantityRequest> productQuantityRequestList =
                createProductQuantityRequestList(basketItemList);

        List<ProductLineItemResponse> productLineItemResponseList = productClientService
                .priceProductsForCheckout(productQuantityRequestList);

        basketManager.startCheckoutProcess(activeBasket, productLineItemResponseList);
    }

    @Transactional(readOnly = true)
    public BasketResponse getActiveBasket() {
        Basket basket = basketManager.findOrCreateActiveBasket();

        List<BasketItemResponse> basketItemResponseList = new ArrayList<>();

        BigDecimal basketTotal = calculateBasketItem(basket.getBasketItems(), basketItemResponseList);

        return new BasketResponse(basketItemResponseList, basketTotal);
    }

    @Transactional
    public String deleteBasketItemByActiveBasket(Long id) {
        Basket basket = basketManager.findOrCreateActiveBasket();

        BasketItem basketItem = findByBasketItemByProductId(basket.getBasketItems(), id);

        if (basketItem == null)
            throw new BasketItemNotFoundException("Basket item bulunamadı!!!");

        // dirket olarak o nesne ile ilişkiyi kesiyorumki başkaları kullanmasın alt satırlarda.
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

        inventoryClientService.checkAvailabilityByProductId(newProductQuantityRequest);
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

        List<BasketProductResponse> basketProductResponseList = productClientService.
                getBasketProducts(productIdList);

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


}
