package com.inventario.service;

import com.inventario.dto.response.DashboardResponse;
import com.inventario.dto.response.NotificacionesRecordatorioResponse;
import com.inventario.entity.Equipo;
import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.EstadoServicio;
import com.inventario.entity.Propiedad;
import com.inventario.entity.Servicio;
import com.inventario.entity.TipoServicio;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.OrdenSalidaItemRepository;
import com.inventario.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_RECIENTES = 6;
    private static final int LIMITE_EN_CALLE = 8;

    private final EquipoRepository equipoRepository;
    private final ServicioRepository servicioRepository;
    private final OrdenSalidaItemRepository ordenSalidaItemRepository;
    private final RecordatorioMantenimientoService recordatorioMantenimientoService;

    @Value("${app.alertas.dias-equipo-en-calle}")
    private int diasAlertaEquipoEnCalle;

    @Transactional(readOnly = true)
    public DashboardResponse resumen() {
        List<Equipo> equipos = equipoRepository.findAll();
        Set<Long> idsEnCalle = new HashSet<>(ordenSalidaItemRepository.findEquipoIdsEnCalle());

        long total = equipos.size();
        long activos = contar(equipos, EstadoEquipo.ACTIVO);
        long inactivos = contar(equipos, EstadoEquipo.INACTIVO);
        long enMantenimiento = contar(equipos, EstadoEquipo.EN_MANTENIMIENTO);
        long dadosDeBaja = contar(equipos, EstadoEquipo.DADO_DE_BAJA);
        long enCalle = idsEnCalle.size();

        List<DashboardResponse.CategoriaConteo> porCategoria = equipos.stream()
                .collect(Collectors.groupingBy(Equipo::getCategoria, Collectors.counting())).entrySet().stream()
                .map(e -> new DashboardResponse.CategoriaConteo(e.getKey().getId(), e.getKey().getNombre(), e.getValue()))
                .sorted(Comparator.comparing(DashboardResponse.CategoriaConteo::categoriaNombre))
                .toList();
        Map<Propiedad, Long> porPropiedad = equipos.stream()
                .collect(Collectors.groupingBy(Equipo::getPropiedad, Collectors.counting()));

        DashboardResponse.Equipos resumenEquipos = new DashboardResponse.Equipos(
                total, activos, inactivos, enMantenimiento, dadosDeBaja, total - enCalle, enCalle, porCategoria,
                porPropiedad);

        List<Servicio> servicios = servicioRepository.findAll();
        Map<EstadoServicio, Long> porEstado = servicios.stream()
                .collect(Collectors.groupingBy(Servicio::getEstado, Collectors.counting()));
        Map<TipoServicio, Long> porTipo = servicios.stream()
                .collect(Collectors.groupingBy(Servicio::getTipoServicio, Collectors.counting()));

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        BigDecimal costoTotalHistorico = servicioRepository.sumCostoValorizado()
                .add(servicioRepository.sumCostoRepuestos());
        BigDecimal costoMesActual = servicioRepository.sumCostoValorizadoDesde(inicioMes)
                .add(servicioRepository.sumCostoRepuestosDesde(inicioMes));

        DashboardResponse.Servicios resumenServicios = new DashboardResponse.Servicios(
                servicios.size(), porEstado, porTipo, costoTotalHistorico, costoMesActual);

        NotificacionesRecordatorioResponse notificaciones = recordatorioMantenimientoService.notificaciones();
        DashboardResponse.Mantenimientos resumenMantenimientos = new DashboardResponse.Mantenimientos(
                notificaciones.vencidos().size(), notificaciones.proximos().size());

        List<DashboardResponse.ServicioReciente> serviciosRecientes = servicios.stream()
                .sorted(Comparator.comparing(Servicio::getCreadoEn).reversed())
                .limit(LIMITE_RECIENTES)
                .map(s -> new DashboardResponse.ServicioReciente(
                        s.getId(),
                        "SRV-" + String.format("%06d", s.getId()),
                        s.getEquipo().getDescripcionEquipo(),
                        s.getTipoServicio(),
                        s.getEstado(),
                        s.getFecha(),
                        s.getTecnicoResponsable() != null ? s.getTecnicoResponsable().getNombre() : null))
                .toList();

        List<DashboardResponse.EquipoEnCalle> todosEnCalle = calcularEquiposEnCalle();

        long enAlerta = todosEnCalle.stream().filter(DashboardResponse.EquipoEnCalle::enAlerta).count();
        DashboardResponse.EquiposEnCalleResumen resumenEnCalle = new DashboardResponse.EquiposEnCalleResumen(
                todosEnCalle.size(), enAlerta, diasAlertaEquipoEnCalle);

        List<DashboardResponse.EquipoEnCalle> equiposEnCalle = todosEnCalle.stream()
                .limit(LIMITE_EN_CALLE)
                .toList();

        return new DashboardResponse(resumenEquipos, resumenServicios, resumenMantenimientos, resumenEnCalle,
                serviciosRecientes, equiposEnCalle);
    }

    /** Equipos en la calle (sin limite de cantidad) cuyos dias fuera ya alcanzaron el umbral de alerta. */
    @Transactional(readOnly = true)
    public List<DashboardResponse.EquipoEnCalle> listarEquiposEnAlerta() {
        return calcularEquiposEnCalle().stream()
                .filter(DashboardResponse.EquipoEnCalle::enAlerta)
                .toList();
    }

    /** Todos los equipos actualmente en la calle, ordenados de mas a menos dias fuera. */
    private List<DashboardResponse.EquipoEnCalle> calcularEquiposEnCalle() {
        LocalDate hoy = LocalDate.now();
        return ordenSalidaItemRepository.findTodosAbiertos().stream()
                .map(i -> {
                    long diasFuera = ChronoUnit.DAYS.between(i.getOrdenSalida().getFechaSalida(), hoy);
                    return new DashboardResponse.EquipoEnCalle(
                            i.getEquipo().getId(),
                            i.getEquipo().getDescripcionEquipo(),
                            i.getEquipo().getCodigo(),
                            i.getOrdenSalida().getCliente().getNombre(),
                            i.getOrdenSalida().getFechaSalida(),
                            diasFuera,
                            diasFuera >= diasAlertaEquipoEnCalle);
                })
                .sorted(Comparator.comparingLong(DashboardResponse.EquipoEnCalle::diasFuera).reversed())
                .toList();
    }

    private long contar(List<Equipo> equipos, EstadoEquipo estado) {
        return equipos.stream().filter(e -> e.getEstado() == estado).count();
    }
}
