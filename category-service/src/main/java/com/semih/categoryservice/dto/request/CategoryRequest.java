package com.semih.categoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Kategori adı boş olamaz")
        @Size(min = 2, max = 50, message = "Kategori adı 2 ile 50 karakter arasında olmalıdır!")
        String categoryName

) {

}
