package com.inventario.dto.response;

import com.inventario.entity.Equipo;
import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.EstadoServicioTaller;
import com.inventario.entity.OrdenSalidaItem;
import com.inventario.entity.OrdenSalidaItemAccesorio;
import com.inventario.entity.Propiedad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record EquipoResponse(
        Long id,
        String codigo,
        String codigoTaller,
        String descripcionEquipo,
        CategoriaCatalogoResponse categoria,
        String marca,
        String numeroSerie,
        String modelo,
        TipoMotorCatalogoResponse motor,
        String ubicacion,
        LocalDate fechaRegistro,
        EstadoEquipo estado,
        boolean checkMtto,
        boolean checkHv,
        boolean checkFt,
        Propiedad propiedad,
        ClienteResponse cliente,
        String accesorios,
        LocalDate fechaIngreso,
        LocalDate fechaDiagnostico,
        boolean rotulado,
        EstadoServicioTaller estadoServicioTaller,
        List<EquipoAccesorioResponse> accesoriosRegistrados,
        boolean enCalle,
        String ubicacionActual,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    /**
     * @param itemAbierto la fila de orden de salida sin devolución de este equipo, si tiene una
     *                     "en la calle" en este momento; null si el equipo está en su ubicación de origen.
     * @param accesoriosPendientes accesorios de este equipo que aún no se han devuelto, sin importar si
     *                     el item/equipo al que pertenecen ya se marcó como devuelto (permite que un
     *                     accesorio siga "en la calle" aunque el equipo ya volvió).
     */
    public static EquipoResponse from(Equipo equipo, OrdenSalidaItem itemAbierto,
                                       List<OrdenSalidaItemAccesorio> accesoriosPendientes) {
        boolean enCalle = itemAbierto != null;
        String home = (equipo.getUbicacion() != null && !equipo.getUbicacion().isBlank())
                ? equipo.getUbicacion() : "Almacén";
        String destinoActual = enCalle ? itemAbierto.getOrdenSalida().getCliente().getNombre() : null;
        String ubicacionActual = enCalle ? destinoActual : home;

        Map<Long, String> destinoPorAccesorioPendiente = accesoriosPendientes.stream()
                .collect(Collectors.toMap(
                        oia -> oia.getAccesorio().getId(),
                        oia -> oia.getOrdenSalidaItem().getOrdenSalida().getCliente().getNombre(),
                        (a, b) -> a));

        List<EquipoAccesorioResponse> accesorios = equipo.getAccesoriosRegistrados().stream()
                .map(a -> {
                    String destinoPendiente = destinoPorAccesorioPendiente.get(a.getId());
                    boolean accesorioEnCalle = destinoPendiente != null;
                    String accUbicacion = accesorioEnCalle ? destinoPendiente : home;
                    return EquipoAccesorioResponse.from(a, accUbicacion, accesorioEnCalle);
                })
                .toList();

        return new EquipoResponse(
                equipo.getId(),
                equipo.getCodigo(),
                equipo.getCodigoTaller(),
                equipo.getDescripcionEquipo(),
                CategoriaCatalogoResponse.from(equipo.getCategoria()),
                equipo.getMarca(),
                equipo.getNumeroSerie(),
                equipo.getModelo(),
                equipo.getMotor() != null ? TipoMotorCatalogoResponse.from(equipo.getMotor()) : null,
                equipo.getUbicacion(),
                equipo.getFechaRegistro(),
                equipo.getEstado(),
                equipo.isCheckMtto(),
                equipo.isCheckHv(),
                equipo.isCheckFt(),
                equipo.getPropiedad(),
                equipo.getCliente() != null ? ClienteResponse.from(equipo.getCliente()) : null,
                equipo.getAccesorios(),
                equipo.getFechaIngreso(),
                equipo.getFechaDiagnostico(),
                equipo.isRotulado(),
                equipo.getEstadoServicioTaller(),
                accesorios,
                enCalle,
                ubicacionActual,
                equipo.getCreadoEn(),
                equipo.getActualizadoEn()
        );
    }
}
