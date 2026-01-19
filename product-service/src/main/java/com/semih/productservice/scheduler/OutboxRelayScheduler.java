package com.semih.productservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semih.common.dto.request.ProductStockEvent;
import com.semih.productservice.entity.OutboxMessage;
import com.semih.productservice.repository.OutboxMessageRepository;
import com.semih.productservice.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private final OutboxService outboxService;

    public OutboxRelayScheduler(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxMessage(){
        List<OutboxMessage> messageList = outboxService.
                findAllOutboxMessageFindByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxMessage outboxMessage : messageList) {
            try {
                outboxService.sendSingleMessageWithTransaction(outboxMessage);
            } catch (Exception exception) {
                log.error("Hata oluştu, bu mesaj atlanıyor veya döngü kırılıyor: {}", exception.getMessage());
                break;
            }
        }
    }


}
