package com.semih.categoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubCategoryUpdateRequest(
        @NotBlank(message = "Alt kategori adı boş olamaz!")
        @Size(min = 2, max = 50, message = "Alt kategori adı 2 ile 50 karakter arasında olmalıdır!")
        String subCategoryName
) {
}
