package com.inventario.dto.request;

import com.inventario.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @NotBlank(message = "El username es obligatorio")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]{3,60}$",
                message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos (3-60 caracteres)"
        )
        String username,

        /** Opcional: si se define, el usuario recibe el resumen diario de alertas por correo. */
        @Email(message = "El correo no es válido")
        @Size(max = 180, message = "El correo no puede superar 180 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
