package com.semih.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CategoryValidationRequest(

        @NotNull(message = "Kategori ID boş olamaz")
        @Positive(message = "Kategori ID pozitif olmalı")
        Long categoryId,

        List<
                @Positive(message = "Alt kategori ID pozitif olmalı")
                        Long
                > subCategoriesId

) {}
