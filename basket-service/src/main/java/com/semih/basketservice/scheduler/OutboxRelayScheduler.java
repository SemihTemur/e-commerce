package com.semih.basketservice.scheduler;

import com.semih.basketservice.entity.OutboxMessage;
import com.semih.basketservice.repository.OutboxRepository;
import com.semih.basketservice.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
public class OutboxRelayScheduler {

    private final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxService outboxService;

    // kafka eklencek


    public OutboxRelayScheduler(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents(){
        List<OutboxMessage> unprocessedEvents = outboxService.findByProcessedFalse();

        if(unprocessedEvents.isEmpty())
            return;

        log.info("{} adet işlenmemiş event bulundu, aktarım başlıyor.", unprocessedEvents.size());

        for(OutboxMessage event : unprocessedEvents){
            try {
                outboxService.sendAndMarkAsProcessed(event);
            } catch (Exception e) {
                log.error("Mesaj gönderilemedi ID: {}, Hata: {}", event.getId(), e.getMessage());
            }
        }
    }

}
