package com.semih.productservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semih.common.constant.OutboxEventType;
import com.semih.common.dto.request.ProductStockEvent;
import com.semih.productservice.dto.request.ProductMessagePayload;
import com.semih.productservice.entity.OutboxMessage;
import com.semih.productservice.entity.Product;
import com.semih.productservice.producer.ProductStockEventProducer;
import com.semih.productservice.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxService {
    private final OutboxMessageRepository outboxMessageRepository;
    private final ProductStockEventProducer productStockEventProducer;

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    public OutboxService(OutboxMessageRepository outboxMessageRepository,
                         ProductStockEventProducer productStockEventProducer) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.productStockEventProducer = productStockEventProducer;
    }

    @Transactional
    public void sendSingleMessageWithTransaction(OutboxMessage outboxMessage) throws RuntimeException {
        ProductStockEvent event = convertToEvent(outboxMessage);

        productStockEventProducer.send(event,outboxMessage.getAggregateId().toString());

        outboxMessage.setProcessed(true);
        outboxMessageRepository.save(outboxMessage);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveProductOutboxEvent(Product product,OutboxEventType outboxEventType, Integer quantity){
        OutboxMessage outboxMessage = new OutboxMessage(
                product.getId(),
                outboxEventType,
                quantity
        );

        outboxMessageRepository.save(outboxMessage);
    }

    public List<OutboxMessage> findAllOutboxMessageFindByProcessedFalseOrderByCreatedAtAsc(){
        return outboxMessageRepository.findByProcessedFalseOrderByCreatedAtAsc();
    }

    private ProductStockEvent convertToEvent(OutboxMessage outboxMessage){
        return new ProductStockEvent(
                outboxMessage.getId(),
                outboxMessage.getAggregateId(),
                outboxMessage.getType(),
                outboxMessage.getPayload(),
                System.currentTimeMillis()
        );
    }



}
