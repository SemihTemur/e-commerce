package com.semih.productservice.dto.request;

import com.semih.common.dto.request.CategoryValidationRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(

        @NotEmpty(message = "Kategori listesi boş olamaz")
        List<@Valid CategoryValidationRequest> categoryRequestList,

        @NotBlank(message = "Ürün adı boş olamaz")
        @Size(min = 3, message = "Ürün adı en az 3 karakter olmalı")
        String productName,

        @Size(max = 500, message = "Ürün açıklaması en fazla 500 karakter olabilir")
        String productDescription,

        @NotNull(message = "Ürün fiyatı boş olamaz")
        @Positive(message = "Ürün fiyatı 0'dan büyük olmalı")
        BigDecimal productPrice,

        @NotNull(message = "Ürün miktarı boş olamaz")
        @Positive(message = "Ürün miktarı 0'dan büyük olmalı")
        Integer quantity

) {}

