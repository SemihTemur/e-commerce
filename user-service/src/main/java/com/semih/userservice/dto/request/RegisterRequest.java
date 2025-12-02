package com.semih.userservice.dto.request;

import com.semih.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(

        @NotBlank(message = "Kullanıcı adı boş olamaz.")
        @Size(min = 3, message = "Kullanıcı adı en az 3 karakter olmalıdır.")
        String userName,

        @NotBlank(message = "Email boş olamaz.")
        @Email(message = "Geçerli bir email adresi giriniz.")
        String email,

        @NotBlank(message = "Şifre boş olamaz.")
        @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır.")
        String password,

        @NotEmpty(message = "En az bir rol seçilmelidir.")
        Set<Role> roles

) { }

