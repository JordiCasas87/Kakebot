package com.jordi.kakebot.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 15, message = "El nombre de usuario debe tener entre 3 y 15 caracteres")
        String username,
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, max = 10, message = "La contrasena debe tener entre 6 y 10 caracteres")
        String password
) {
}
