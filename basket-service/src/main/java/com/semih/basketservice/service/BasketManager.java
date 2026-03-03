package com.semih.basketservice.service;

import com.semih.basketservice.entity.Basket;
import com.semih.basketservice.entity.BasketStatus;
import com.semih.basketservice.entity.OutboxMessage;
import com.semih.basketservice.entity.ProcessedEvent;
import com.semih.basketservice.repository.BasketRepository;
import com.semih.basketservice.repository.ProcessedEventRepository;
import com.semih.common.constant.OrderBasketStatus;
import com.semih.common.dto.request.BasketEvent;
import com.semih.common.dto.request.BasketItemEvent;
import com.semih.common.dto.request.OrderBasketResultEvent;
import com.semih.common.dto.response.ProductLineItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BasketManager {

    private final BasketRepository basketRepository;

    private final OutboxService outboxService;

    private final ProcessedEventRepository processedEventRepository;

    private static final Logger logger = LoggerFactory.getLogger(BasketManager.class);

    public BasketManager(BasketRepository basketRepository, OutboxService outboxService,
                         ProcessedEventRepository processedEventRepository) {
        this.basketRepository = basketRepository;
        this.outboxService = outboxService;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public Basket findOrCreateActiveBasket() {
        String userId = getUserId();

        if (basketRepository.existsByUserIdAndStatus(userId, BasketStatus.ORDER_IN_PROGRESS)) {
            throw new RuntimeException("Siparişiniz şu an işleniyor, sepet üzerinde değişiklik yapılamaz.");
        }

        return basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseGet(() -> {
                    Basket newBasket = new Basket(userId, BasketStatus.ACTIVE);
                    return basketRepository.save(newBasket);
                });
    }


    @Transactional
    public void updateBasket(Basket basket) {
        basketRepository.save(basket);
    }

    @Transactional
    public void handleOrderResult(OrderBasketResultEvent event) {
        // 1. Idempotency Check (Aynı mesajı tekrar işlemeyelim)
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        // 2. Sepeti Bul (Artık 'ORDER_IN_PROGRESS' olan sepeti arıyoruz!)
        Optional<Basket> basketOpt = basketRepository.findByUserIdAndStatus(
                event.userId(),
                BasketStatus.ORDER_IN_PROGRESS// Bu statüdekini ara
        );

        if (basketOpt.isEmpty()) {
            logger.warn("İşlemde olan bir sepet bulunamadı! UserId: {}, EventId: {}", event.userId(),
                    event.eventId());
            processedEventRepository.save(new ProcessedEvent(event.eventId()));
            return;
        }

        Basket basket = basketOpt.get();

        // 3. Karar Mekanizması
        if (event.status() == OrderBasketStatus.ORDER_COMPLETED) {
            // Sipariş başarılı, sepeti artık kapat
            basket.setStatus(BasketStatus.ORDERED);
            logger.info("Sipariş başarıyla tamamlandı, sepet ORDERED yapıldı. UserId: {}", event.userId());
        } else {
            // Sipariş başarısız (Stok yok vb.), sepeti tekrar ACTIVE yap ki kullanıcı düzeltebilsin
            basket.setStatus(BasketStatus.ACTIVE);
            logger.info("Sipariş başarısız, sepet tekrar ACTIVE yapıldı. UserId: {}", event.userId());
        }

        // 4. Değişiklikleri kaydet ve mesajı 'işlendi' olarak işaretle
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
    }

    @Transactional
    public void startCheckoutProcess(Basket basket, List<ProductLineItemResponse> prices) {
        basket.setStatus(BasketStatus.ORDER_IN_PROGRESS);
        basketRepository.save(basket);

        OutboxMessage outboxMessage = new OutboxMessage(basket.getId(), createBasketItem(basket.getUserId()
                ,prices));
        outboxService.save(outboxMessage);

    }

    private String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || authentication.getPrincipal()==null){
            throw new RuntimeException("Böyle bir Kullanıcı yoktur");
        }

        return (String) authentication.getPrincipal();
    }

    private BasketEvent createBasketItem(String userId,
            List<ProductLineItemResponse> productLineItemResponseList) {

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BasketItemEvent> basketItemEvents = new ArrayList<>();

        for (ProductLineItemResponse productLineItemResponse : productLineItemResponseList) {

            BigDecimal lineTotal = productLineItemResponse.unitPrice()
                    .multiply(BigDecimal.valueOf(productLineItemResponse.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            basketItemEvents.add(
                    new BasketItemEvent(
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

        return new BasketEvent(UUID.randomUUID(),userId,totalAmount,basketItemEvents);
    }


}
