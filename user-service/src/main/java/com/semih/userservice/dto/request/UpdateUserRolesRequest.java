package com.semih.userservice.dto.request;

import com.semih.userservice.entity.Role;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateUserRolesRequest(

        @NotNull(message = "Kullanıcı ID boş olamaz.")
        @Positive(message = "Kullanıcı ID pozitif bir sayı olmalıdır.")
        Long userId,

        @NotEmpty(message = "En az bir rol seçilmelidir.")
        Set<Role> roles

) { }

