package com.semih.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CategoryValidationRequest(

        @NotNull(message = "Kategori ID boş olamaz")
        @Positive(message = "Kategori ID pozitif olmalı")
        Long categoryId,

        @NotEmpty(message = "Alt kategori listesi boş olamaz")
        List<
                @NotNull(message = "Alt kategori ID boş olamaz")
                @Positive(message = "Alt kategori ID pozitif olmalı")
                        Long
                > subCategoriesId

) {}
