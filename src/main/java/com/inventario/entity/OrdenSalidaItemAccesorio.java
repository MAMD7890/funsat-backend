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

import java.time.LocalDate;

/**
 * Un accesorio especifico que salio con un item de orden de salida, con su
 * propio estado de devolucion. Antes esto era un simple cruce (el accesorio
 * "vuelve" cuando vuelve el equipo, todo o nada); ahora cada fila lleva su
 * propia fecha_devolucion para permitir devoluciones parciales: el equipo
 * puede quedar registrado como devuelto mientras uno de sus accesorios
 * sigue pendiente (y sigue mostrando la ubicacion del cliente hasta que se
 * registre su propia entrada).
 */
@Entity
@Table(name = "orden_salida_item_accesorio")
@Getter
@Setter
@NoArgsConstructor
public class OrdenSalidaItemAccesorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_salida_item_id", nullable = false)
    private OrdenSalidaItem ordenSalidaItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_accesorio_id", nullable = false)
    private EquipoAccesorio accesorio;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibido_por_id")
    private Usuario recibidoPor;

    @Column(name = "observacion_devolucion", length = 300)
    private String observacionDevolucion;

    public boolean isDevuelto() {
        return fechaDevolucion != null;
    }
}
