package com.inventario.service;

import com.inventario.dto.request.EquipoAccesorioRequest;
import com.inventario.dto.request.EquipoRequest;
import com.inventario.dto.response.EquipoResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.CategoriaCatalogo;
import com.inventario.entity.Equipo;
import com.inventario.entity.EquipoAccesorio;
import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.OrdenSalidaItem;
import com.inventario.entity.OrdenSalidaItemAccesorio;
import com.inventario.entity.Propiedad;
import com.inventario.entity.TipoMotorCatalogo;
import com.inventario.exception.CategoriaCatalogoNotFoundException;
import com.inventario.exception.ClienteNotFoundException;
import com.inventario.exception.EquipoNotFoundException;
import com.inventario.exception.TipoMotorCatalogoNotFoundException;
import com.inventario.repository.CategoriaCatalogoRepository;
import com.inventario.repository.ClienteRepository;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.OrdenSalidaItemAccesorioRepository;
import com.inventario.repository.OrdenSalidaItemRepository;
import com.inventario.repository.TipoMotorCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdenSalidaItemRepository ordenSalidaItemRepository;
    private final OrdenSalidaItemAccesorioRepository ordenSalidaItemAccesorioRepository;
    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final TipoMotorCatalogoRepository tipoMotorCatalogoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<EquipoResponse> buscar(Long categoriaId, EstadoEquipo estado, Long clienteId,
                                        Propiedad propiedad, String q, Pageable pageable) {
        Specification<Equipo> spec = Specification.where(null);

        if (categoriaId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (clienteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        }
        if (propiedad != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("propiedad"), propiedad));
        }
        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("descripcionEquipo")), patron),
                    cb.like(cb.lower(root.get("codigo")), patron),
                    cb.like(cb.lower(root.get("codigoTaller")), patron)
            ));
        }

        Page<Equipo> equipos = equipoRepository.findAll(spec, pageable);
        List<Long> equipoIds = equipos.getContent().stream().map(Equipo::getId).toList();
        Map<Long, OrdenSalidaItem> abiertosPorEquipo = itemsAbiertosPorEquipo(equipoIds);
        Map<Long, List<OrdenSalidaItemAccesorio>> accesoriosPendientesPorEquipo = accesoriosPendientesPorEquipo(equipoIds);
        return equipos.map(equipo -> EquipoResponse.from(equipo, abiertosPorEquipo.get(equipo.getId()),
                accesoriosPendientesPorEquipo.getOrDefault(equipo.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        Equipo equipo = buscarPorId(id);
        return EquipoResponse.from(equipo, ordenSalidaItemRepository.findAbiertoPorEquipoId(id).orElse(null),
                ordenSalidaItemAccesorioRepository.findPendientesPorEquipoId(id));
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest request) {
        Equipo equipo = new Equipo();
        aplicarCambios(equipo, request);
        Equipo guardado = equipoRepository.save(equipo);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Equipo", guardado.getId(), descripcionAuditoria(guardado));
        return EquipoResponse.from(guardado, null, List.of());
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo equipo = buscarPorId(id);
        aplicarCambios(equipo, request);
        Equipo guardado = equipoRepository.save(equipo);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Equipo", guardado.getId(), descripcionAuditoria(guardado));
        return EquipoResponse.from(guardado, ordenSalidaItemRepository.findAbiertoPorEquipoId(id).orElse(null),
                ordenSalidaItemAccesorioRepository.findPendientesPorEquipoId(id));
    }

    private Map<Long, OrdenSalidaItem> itemsAbiertosPorEquipo(List<Long> equipoIds) {
        if (equipoIds.isEmpty()) {
            return Map.of();
        }
        return ordenSalidaItemRepository.findAbiertosPorEquipoIds(equipoIds).stream()
                .collect(Collectors.toMap(i -> i.getEquipo().getId(), Function.identity()));
    }

    private Map<Long, List<OrdenSalidaItemAccesorio>> accesoriosPendientesPorEquipo(List<Long> equipoIds) {
        if (equipoIds.isEmpty()) {
            return Map.of();
        }
        return ordenSalidaItemAccesorioRepository.findPendientesPorEquipoIds(equipoIds).stream()
                .collect(Collectors.groupingBy(oia -> oia.getAccesorio().getEquipo().getId()));
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscarPorId(id);
        equipoRepository.delete(equipo);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Equipo", id, descripcionAuditoria(equipo));
    }

    private String descripcionAuditoria(Equipo equipo) {
        return equipo.getDescripcionEquipo() + (equipo.getCodigo() != null ? " (" + equipo.getCodigo() + ")" : "");
    }

    private void aplicarCambios(Equipo equipo, EquipoRequest request) {
        equipo.setCodigo(blankToNull(request.codigo()));
        equipo.setCodigoTaller(blankToNull(request.codigoTaller()));
        equipo.setDescripcionEquipo(request.descripcionEquipo());
        equipo.setCategoria(buscarCategoria(request.categoriaId()));
        equipo.setMarca(request.marca());
        equipo.setNumeroSerie(blankToNull(request.numeroSerie()));
        equipo.setModelo(request.modelo());
        equipo.setMotor(request.motorId() != null ? buscarMotor(request.motorId()) : null);
        equipo.setUbicacion(request.ubicacion());
        equipo.setEstado(request.estado() != null ? request.estado() : EstadoEquipo.ACTIVO);
        equipo.setCheckMtto(request.checkMtto());
        equipo.setCheckHv(request.checkHv());
        equipo.setCheckFt(request.checkFt());
        equipo.setPropiedad(request.propiedad());
        equipo.setAccesorios(request.accesorios());
        equipo.setFechaIngreso(request.fechaIngreso());
        equipo.setFechaDiagnostico(request.fechaDiagnostico());
        equipo.setRotulado(request.rotulado());
        equipo.setEstadoServicioTaller(request.estadoServicioTaller());

        if (request.propiedad() == Propiedad.EXTERNO) {
            equipo.setCliente(clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new ClienteNotFoundException(request.clienteId())));
        } else {
            equipo.setCliente(null);
        }

        sincronizarAccesorios(equipo, request.accesoriosRegistrados());
    }

    /**
     * Reconcilia en el sitio (mismo patron que ServicioService con
     * repuestos/checklist): evita el clear()+add() que choca con
     * orphanRemoval=true al reinsertar un accesorio que ya existia dentro
     * del mismo flush. Aqui la clave es el id propio del accesorio (no hay
     * un catalogo externo al que apunte, es texto libre por fila).
     */
    private void sincronizarAccesorios(Equipo equipo, List<EquipoAccesorioRequest> requests) {
        Map<Long, EquipoAccesorio> existentesPorId = equipo.getAccesoriosRegistrados().stream()
                .collect(Collectors.toMap(EquipoAccesorio::getId, Function.identity()));

        Set<Long> idsSolicitados = requests.stream()
                .map(EquipoAccesorioRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        equipo.getAccesoriosRegistrados().removeIf(a -> !idsSolicitados.contains(a.getId()));

        for (EquipoAccesorioRequest r : requests) {
            EquipoAccesorio existente = r.id() != null ? existentesPorId.get(r.id()) : null;
            if (existente != null) {
                existente.setNombre(r.nombre());
                existente.setCantidad(r.cantidad() != null ? r.cantidad() : 1);
                continue;
            }

            EquipoAccesorio nuevo = new EquipoAccesorio();
            nuevo.setEquipo(equipo);
            nuevo.setNombre(r.nombre());
            nuevo.setCantidad(r.cantidad() != null ? r.cantidad() : 1);
            equipo.getAccesoriosRegistrados().add(nuevo);
        }
    }

    private Equipo buscarPorId(Long id) {
        return equipoRepository.findById(id).orElseThrow(() -> new EquipoNotFoundException(id));
    }

    private CategoriaCatalogo buscarCategoria(Long categoriaId) {
        return categoriaCatalogoRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaCatalogoNotFoundException(categoriaId));
    }

    private TipoMotorCatalogo buscarMotor(Long motorId) {
        return tipoMotorCatalogoRepository.findById(motorId)
                .orElseThrow(() -> new TipoMotorCatalogoNotFoundException(motorId));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
