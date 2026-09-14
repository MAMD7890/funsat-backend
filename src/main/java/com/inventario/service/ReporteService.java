package com.inventario.service;

import com.inventario.dto.response.ReporteResponse;
import com.inventario.entity.Servicio;
import com.inventario.entity.ServicioRepuesto;
import com.inventario.entity.TipoServicio;
import com.inventario.entity.Usuario;
import com.inventario.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Agregaciones para la pagina de Reportes. Sigue el mismo enfoque que
 * DashboardService: trae las entidades relevantes y agrupa en memoria con
 * streams, en vez de escribir SQL de agregacion, porque el volumen de datos
 * de un taller no lo justifica y mantiene el estilo del resto del proyecto.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final int MESES_TENDENCIA = 6;
    private static final int TOP_EQUIPOS = 10;
    private static final int TOP_REPUESTOS = 10;

    private final ServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public ReporteResponse generar() {
        List<Servicio> servicios = servicioRepository.findAll();

        return new ReporteResponse(
                costosPorMes(servicios),
                topEquiposPorCorrectivos(servicios),
                topRepuestosMasUsados(servicios),
                cargaPorTecnico(servicios)
        );
    }

    private List<ReporteResponse.CostoMensual> costosPorMes(List<Servicio> servicios) {
        YearMonth actual = YearMonth.now();
        List<YearMonth> meses = IntStream.rangeClosed(0, MESES_TENDENCIA - 1)
                .mapToObj(actual::minusMonths)
                .sorted()
                .toList();

        return meses.stream()
                .map(mes -> {
                    List<Servicio> delMes = servicios.stream()
                            .filter(s -> YearMonth.from(s.getFecha()).equals(mes))
                            .toList();
                    BigDecimal costoValorizado = delMes.stream()
                            .map(Servicio::getCostoValorizado)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal costoRepuestos = delMes.stream()
                            .flatMap(s -> s.getRepuestos().stream())
                            .map(this::costoRepuesto)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ReporteResponse.CostoMensual(
                            mes.atDay(1), costoValorizado, costoRepuestos, costoValorizado.add(costoRepuestos));
                })
                .toList();
    }

    private List<ReporteResponse.EquipoCorrectivos> topEquiposPorCorrectivos(List<Servicio> servicios) {
        Map<Long, Long> conteoPorEquipo = servicios.stream()
                .filter(s -> s.getTipoServicio() == TipoServicio.CORRECTIVO)
                .collect(Collectors.groupingBy(s -> s.getEquipo().getId(), Collectors.counting()));

        Map<Long, Servicio> ultimoServicioPorEquipo = servicios.stream()
                .filter(s -> s.getTipoServicio() == TipoServicio.CORRECTIVO)
                .collect(Collectors.toMap(s -> s.getEquipo().getId(), s -> s, (a, b) -> a));

        return conteoPorEquipo.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(TOP_EQUIPOS)
                .map(e -> {
                    Servicio ejemplo = ultimoServicioPorEquipo.get(e.getKey());
                    return new ReporteResponse.EquipoCorrectivos(
                            e.getKey(), ejemplo.getEquipo().getDescripcionEquipo(),
                            ejemplo.getEquipo().getCodigo(), e.getValue());
                })
                .toList();
    }

    private List<ReporteResponse.RepuestoUso> topRepuestosMasUsados(List<Servicio> servicios) {
        List<ServicioRepuesto> repuestos = servicios.stream()
                .flatMap(s -> s.getRepuestos().stream())
                .toList();

        Map<String, List<ServicioRepuesto>> porNombre = repuestos.stream()
                .collect(Collectors.groupingBy(sr -> sr.getNombre().trim()));

        return porNombre.entrySet().stream()
                .map(e -> {
                    BigDecimal cantidadTotal = e.getValue().stream()
                            .map(ServicioRepuesto::getCantidad)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal costoTotal = e.getValue().stream()
                            .map(this::costoRepuesto)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ReporteResponse.RepuestoUso(e.getKey(), cantidadTotal, costoTotal, e.getValue().size());
                })
                .sorted(Comparator.comparing(ReporteResponse.RepuestoUso::cantidadTotal).reversed())
                .limit(TOP_REPUESTOS)
                .toList();
    }

    private List<ReporteResponse.CargaTecnico> cargaPorTecnico(List<Servicio> servicios) {
        Map<Usuario, List<Servicio>> porTecnico = servicios.stream()
                .filter(s -> s.getTecnicoResponsable() != null)
                .collect(Collectors.groupingBy(Servicio::getTecnicoResponsable));

        return porTecnico.entrySet().stream()
                .map(e -> {
                    BigDecimal costoTotal = e.getValue().stream()
                            .map(Servicio::getCostoValorizado)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ReporteResponse.CargaTecnico(
                            e.getKey().getId(), e.getKey().getNombre(), e.getValue().size(), costoTotal);
                })
                .sorted(Comparator.comparingLong(ReporteResponse.CargaTecnico::cantidadServicios).reversed())
                .toList();
    }

    private BigDecimal costoRepuesto(ServicioRepuesto sr) {
        return sr.getCostoUnitario().multiply(sr.getCantidad());
    }
}
