package com.inventario.dto.request;

import com.inventario.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Email(message = "El correo no es válido")
        @Size(max = 180, message = "El correo no puede superar 180 caracteres")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
