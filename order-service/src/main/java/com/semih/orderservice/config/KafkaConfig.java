package com.semih.orderservice.config;

import com.semih.common.dto.request.OrderCreatedEvent;
import com.semih.common.dto.response.ProductStockResponseEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    private final String orderEventsTopic;

    private final String orderBasketResult;

    public KafkaConfig(KafkaProperties kafkaProperties,
                       @Value("${spring.kafka.properties.topics.order-events}") String orderEventsTopic,
                       @Value("${spring.kafka.properties.topics.order-basket-result}") String orderBasketResult) {
        this.kafkaProperties = kafkaProperties;
        this.orderEventsTopic = orderEventsTopic;
        this.orderBasketResult = orderBasketResult;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        Map<String,Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());

        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic productStockResponseEventsTopic(){
        return TopicBuilder
                .name(orderEventsTopic)
                .replicas(3)
                .configs(Map.of("min.insync.replicas","2"))
                .build();
    }

    @Bean
    public NewTopic orderBasketResultTopic(){
        return TopicBuilder
                .name(orderBasketResult)
                .replicas(3)
                .configs(Map.of("min.insync.replicas","2"))
                .build();
    }
}
