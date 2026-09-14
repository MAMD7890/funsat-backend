package com.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Categoria de equipo, administrable por el usuario (antes era un enum Java
 * fijo). {@link #requiereNumeroSerie} preserva la regla de negocio que antes
 * vivia hardcodeada como EnumSet.of(VEHICULO, TRAILER): el admin decide, al
 * crear o editar una categoria, si exige numero de serie.
 */
@Entity
@Table(name = "categoria_catalogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "requiere_numero_serie", nullable = false)
    @Builder.Default
    private boolean requiereNumeroSerie = false;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
    }
}
