package com.semih.basketservice.service;

import com.semih.basketservice.entity.OutboxMessage;
import com.semih.basketservice.repository.OutboxRepository;
import com.semih.common.dto.request.BasketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;

    private final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final KafkaTemplate<String, BasketEvent> kafkaTemplate;

    private final String basketEventsTopic;

    public OutboxService(OutboxRepository outboxRepository,
                         KafkaTemplate<String, BasketEvent> kafkaTemplate,
                         @Value("${spring.kafka.properties.topics.basket-events}") String basketEventsTopic) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.basketEventsTopic = basketEventsTopic;
    }

    public void save(OutboxMessage outboxMessage){
        outboxRepository.save(outboxMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Her mesaj için YENİ bir transaction açar
    public void sendAndMarkAsProcessed(OutboxMessage event) {
       kafkaTemplate.send(basketEventsTopic,
                event.getAggregateId().toString(),
               event.getPayload());

        event.setProcessed(true);
        outboxRepository.save(event);

        log.info("Mesaj işlendi: {}", event.getId());
    }

    public List<OutboxMessage> findByProcessedFalse(){
        return outboxRepository.findByProcessedFalse();
    }
}
