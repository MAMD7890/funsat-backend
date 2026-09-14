package com.inventario.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_salida_item")
@Getter
@Setter
@NoArgsConstructor
public class OrdenSalidaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_salida_id", nullable = false)
    private OrdenSalida ordenSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(name = "observacion_salida", length = 300)
    private String observacionSalida;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibido_por_id")
    private Usuario recibidoPor;

    @Column(name = "observacion_devolucion", length = 300)
    private String observacionDevolucion;

    /**
     * Cuáles accesorios cataloga del equipo salen físicamente con él en esta
     * orden, cada uno con su propio estado de devolución (ver
     * {@link OrdenSalidaItemAccesorio}: permite que el equipo vuelva
     * mientras un accesorio queda pendiente).
     */
    @OneToMany(mappedBy = "ordenSalidaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenSalidaItemAccesorio> accesorios = new ArrayList<>();

    public boolean isDevuelto() {
        return fechaDevolucion != null;
    }
}
