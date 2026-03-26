package com.jordi.kakebot.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(min = 3, max = 15, message = "El nombre de usuario debe tener entre 3 y 15 caracteres")
        String username,
        String currentPassword,
        @Size(min = 6, max = 10, message = "La contraseña debe tener entre 6 y 10 caracteres")
        String newPassword
) {

    @AssertTrue(message = "Debes enviar al menos un campo para actualizar")
    public boolean hasAnyFieldToUpdate() {
        return username != null || newPassword != null;
    }

    @AssertTrue(message = "La contraseña actual es obligatoria para cambiar la contraseña")
    public boolean hasCurrentPasswordWhenNewPasswordIsPresent() {
        return newPassword == null || (currentPassword != null && !currentPassword.isBlank());
    }
}
