package com.inventario.validation;

import com.inventario.dto.request.EquipoRequest;
import com.inventario.entity.Propiedad;
import com.inventario.repository.CategoriaCatalogoRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class EquipoRequestValidator implements ConstraintValidator<ValidEquipoRequest, EquipoRequest> {

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Override
    public boolean isValid(EquipoRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valido = true;
        context.disableDefaultConstraintViolation();

        boolean requiereNumeroSerie = request.categoriaId() != null
                && categoriaCatalogoRepository.findById(request.categoriaId())
                        .map(c -> c.isRequiereNumeroSerie())
                        .orElse(false);

        if (requiereNumeroSerie && (request.numeroSerie() == null || request.numeroSerie().isBlank())) {
            addViolation(context, "numeroSerie",
                    "El número de serie es obligatorio para esta categoría de equipo");
            valido = false;
        }

        if (request.propiedad() == Propiedad.EXTERNO) {
            if (request.clienteId() == null) {
                addViolation(context, "clienteId", "El cliente asociado es obligatorio para equipos externos");
                valido = false;
            }
            if (request.fechaIngreso() == null) {
                addViolation(context, "fechaIngreso", "La fecha de ingreso es obligatoria para equipos externos");
                valido = false;
            }
        } else if (request.propiedad() == Propiedad.PROPIO && request.clienteId() != null) {
            addViolation(context, "clienteId", "Un equipo propio no debe tener cliente asociado");
            valido = false;
        }

        return valido;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
