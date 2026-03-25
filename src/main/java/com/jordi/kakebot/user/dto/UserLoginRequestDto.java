package com.jordi.kakebot.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
