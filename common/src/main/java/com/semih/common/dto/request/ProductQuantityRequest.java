package com.semih.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductQuantityRequest(

        @NotNull(message = "Ürün ID boş olamaz")
        @Positive(message = "Ürün ID pozitif olmalı")
        Long productId,

        @NotNull(message = "Miktar boş olamaz")
        @Positive(message = "Miktar 0'dan büyük olmalı")
        Integer quantity

) {}
