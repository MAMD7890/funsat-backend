package com.inventario.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo unificado de equipo: cubre tanto los equipos propios de la empresa
 * (Inventario_Master.xlsx) como los equipos externos de clientes que
 * ingresan al taller (Inventario_SERVICIO_TALLER.xlsx), diferenciados por
 * {@link #propiedad}.
 * <p>
 * {@link #estado} = estado fisico/operativo del equipo. {@link #estadoServicioTaller}
 * = estado del flujo de servicio dentro del taller. Son independientes a
 * proposito: un equipo puede estar ACTIVO como estado general mientras su
 * paso por el taller esta, por ejemplo, EN STAND_BY.
 */
@Entity
@Table(name = "equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String codigo;

    @Column(name = "codigo_taller", length = 20)
    private String codigoTaller;

    @Column(name = "descripcion_equipo", nullable = false, length = 200)
    private String descripcionEquipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaCatalogo categoria;

    @Column(length = 100)
    private String marca;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(length = 100)
    private String modelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id")
    private TipoMotorCatalogo motor;

    @Column(length = 160)
    private String ubicacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoEquipo estado = EstadoEquipo.ACTIVO;

    @Column(name = "check_mtto", nullable = false)
    @Builder.Default
    private boolean checkMtto = false;

    @Column(name = "check_hv", nullable = false)
    @Builder.Default
    private boolean checkHv = false;

    @Column(name = "check_ft", nullable = false)
    @Builder.Default
    private boolean checkFt = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Propiedad propiedad = Propiedad.PROPIO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(columnDefinition = "TEXT")
    private String accesorios;

    /** Catálogo estructurado de accesorios propios del equipo (distinto de {@link #accesorios}, que es solo texto libre). */
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EquipoAccesorio> accesoriosRegistrados = new ArrayList<>();

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_diagnostico")
    private LocalDate fechaDiagnostico;

    @Column(nullable = false)
    @Builder.Default
    private boolean rotulado = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_servicio_taller", length = 40)
    private EstadoServicioTaller estadoServicioTaller;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.creadoEn = now;
        this.actualizadoEn = now;
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDate.now();
        }
        if (this.estado == null) {
            this.estado = EstadoEquipo.ACTIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
