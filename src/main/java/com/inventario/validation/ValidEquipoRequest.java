package com.inventario.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reglas de negocio que dependen de la combinacion de categoria/propiedad
 * y que por eso no se pueden expresar con anotaciones de campo sueltas:
 * numero de serie obligatorio segun categoria, cliente/fecha de ingreso
 * obligatorios solo si el equipo es EXTERNO.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EquipoRequestValidator.class)
public @interface ValidEquipoRequest {

    String message() default "Datos de equipo inválidos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
