package com.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalogo de accesorios propios de un equipo (ej. batería, cargador,
 * maletín), independiente de la columna de texto libre
 * {@link Equipo#getAccesorios()} (esa es solo una nota general para
 * equipos externos). Se usa para elegir, al generar una orden de salida,
 * cuáles accesorios salen con el equipo y cuáles se quedan en el almacén.
 */
@Entity
@Table(name = "equipo_accesorio")
@Getter
@Setter
@NoArgsConstructor
public class EquipoAccesorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private int cantidad = 1;
}
