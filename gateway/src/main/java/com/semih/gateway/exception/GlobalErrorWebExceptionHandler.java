package com.semih.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler; // Doğru Interface
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler() {
        // Senin mapper ayarlarını koruyoruz
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {

        // 1. Durum kodunu ve mesajı belirle (Default: 500)
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = ex.getMessage();
        Map<String, String> errorDetails = Map.of("type", "SystemError");

        // 2. Senin JWT Hata kontrolün (Sınıf ismini kendi projenle eşleştir)
        if (ex.getClass().getSimpleName().contains("AuthenticationCredentialsNotFoundException")) {
            status = HttpStatus.UNAUTHORIZED;
            errorDetails = Map.of("tokenError", ex.getCause() != null ? ex.getCause().getClass().getSimpleName() : "Invalid");
        }

        // 3. Response Ayarları
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 4. ApiError Objesini oluştur
        ApiError errorResponse = new ApiError(
                status.value(),
                errorMessage,
                OffsetDateTime.now(),
                errorDetails
        );

        try {
            // 5. JSON'a çevir ve yaz
            byte[] jsonBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonBytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    // Senin Record yapını koruyoruz
    public record ApiError(
            int status,
            String message,
            OffsetDateTime timestamp,
            Map<String, String> errors
    ) {}
}