package com.semih.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
@Order(-2) // En yüksek önceliğe sahip olmalı
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    // JSON dönüşümü için ObjectMapper (WebFlux'ta bulunur)
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GlobalErrorWebExceptionHandler() {
        // LocalDateTime'ı JSON'a düzgün çevirmek için
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        // 1. JWT Hatalarını Kontrol Et
        if (ex instanceof AuthenticationCredentialsNotFoundException jwtEx) {

            // 2. HTTP Durum Kodunu Ayarla
            HttpStatus status = HttpStatus.UNAUTHORIZED; // 401 Unauthorized
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // 3. ApiError Objesini Oluştur
            ApiError errorResponse = new ApiError(
                    status.value(),
                    jwtEx.getMessage(), // GlobalFilter'dan gelen mesajı kullan
                    OffsetDateTime.now(),
                    Map.of("tokenError", jwtEx.getCause() != null ? jwtEx.getCause().getClass().getSimpleName() : "Unknown")
            );

            try {
                // 4. ApiError Objesini JSON'a Çevir
                byte[] jsonBytes = objectMapper.writeValueAsBytes(errorResponse);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonBytes);

                // 5. Cevabı İstemciye Yaz
                return exchange.getResponse().writeWith(Mono.just(buffer));

            } catch (JsonProcessingException e) {
                // JSON'a çevrim hatası olursa
                return Mono.error(e);
            }
        }

        // Diğer hatalar için varsayılan davranışı sürdür
        return Mono.error(ex);
    }

    // ApiError Record Tanımı (Bu sınıfın dışında veya içinde tanımlanabilir)
    public record ApiError(
            int status,
            String message,
            OffsetDateTime timestamp,
            Map<String, String> errors
    ) {}
}
