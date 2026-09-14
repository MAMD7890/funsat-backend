package com.inventario.service;

import com.inventario.dto.request.ServicioChecklistRespuestaRequest;
import com.inventario.dto.request.ServicioRepuestoRequest;
import com.inventario.dto.request.ServicioRequest;
import com.inventario.dto.response.ServicioResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.ChecklistItemCatalogo;
import com.inventario.entity.EstadoServicio;
import com.inventario.entity.Equipo;
import com.inventario.entity.Servicio;
import com.inventario.entity.ServicioChecklistRespuesta;
import com.inventario.entity.ServicioRepuesto;
import com.inventario.entity.TipoServicio;
import com.inventario.entity.Usuario;
import com.inventario.exception.ChecklistItemCategoriaMismatchException;
import com.inventario.exception.ChecklistItemNotFoundException;
import com.inventario.exception.EquipoNotFoundException;
import com.inventario.exception.ServicioNotFoundException;
import com.inventario.exception.UsuarioNotFoundException;
import com.inventario.repository.ChecklistItemCatalogoRepository;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.ServicioRepository;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChecklistItemCatalogoRepository checklistItemRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ServicioResponse> buscar(Long equipoId, TipoServicio tipoServicio, Long tecnicoId,
                                          EstadoServicio estado, LocalDate desde, LocalDate hasta, String q,
                                          Pageable pageable) {
        Specification<Servicio> spec = Specification.where(null);

        if (equipoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("equipo").get("id"), equipoId));
        }
        if (tipoServicio != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoServicio"), tipoServicio));
        }
        if (tecnicoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tecnicoResponsable").get("id"), tecnicoId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (desde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        }
        if (hasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), hasta));
        }
        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("equipo").get("descripcionEquipo")), patron),
                    cb.like(cb.lower(root.get("equipo").get("codigo")), patron)
            ));
        }

        return servicioRepository.findAll(spec, pageable).map(ServicioResponse::from);
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return ServicioResponse.from(buscarPorId(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = new Servicio();
        aplicarCambios(servicio, request);
        servicio.setEstado(request.estado() != null ? request.estado() : EstadoServicio.REGISTRADO);
        Servicio guardado = servicioRepository.save(servicio);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Servicio", guardado.getId(), descripcionAuditoria(guardado));
        return ServicioResponse.from(guardado);
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscarPorId(id);
        aplicarCambios(servicio, request);
        if (request.estado() != null) {
            servicio.setEstado(request.estado());
        }
        Servicio guardado = servicioRepository.save(servicio);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Servicio", guardado.getId(), descripcionAuditoria(guardado));
        return ServicioResponse.from(guardado);
    }

    @Transactional
    public ServicioResponse cambiarEstado(Long id, EstadoServicio nuevoEstado) {
        Servicio servicio = buscarPorId(id);
        servicio.setEstado(nuevoEstado);
        Servicio guardado = servicioRepository.save(servicio);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Servicio", guardado.getId(),
                descripcionAuditoria(guardado) + " -> " + nuevoEstado);
        return ServicioResponse.from(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = buscarPorId(id);
        servicioRepository.delete(servicio);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Servicio", id, descripcionAuditoria(servicio));
    }

    private String descripcionAuditoria(Servicio servicio) {
        String equipoDesc = servicio.getEquipo() != null ? servicio.getEquipo().getDescripcionEquipo() : "?";
        return servicio.getTipoServicio() + " - " + equipoDesc;
    }

    private void aplicarCambios(Servicio servicio, ServicioRequest request) {
        Equipo equipo = equipoRepository.findById(request.equipoId())
                .orElseThrow(() -> new EquipoNotFoundException(request.equipoId()));
        Usuario tecnico = usuarioRepository.findById(request.tecnicoResponsableId())
                .orElseThrow(() -> new UsuarioNotFoundException(request.tecnicoResponsableId()));

        servicio.setEquipo(equipo);
        servicio.setTipoServicio(request.tipoServicio());
        servicio.setDescripcion(request.descripcion());
        servicio.setFecha(request.fecha());
        servicio.setCostoValorizado(request.costoValorizado());
        servicio.setTecnicoResponsable(tecnico);

        sincronizarRepuestos(servicio, request.repuestos());
        sincronizarChecklist(servicio, equipo, request.checklist());
    }

    /**
     * Reconcilia en el sitio (mismo patron que EquipoService con
     * accesoriosRegistrados): evita el clear()+add() que choca con
     * orphanRemoval=true al reinsertar una fila que ya existia dentro
     * del mismo flush. Aqui la clave es el id propio de la fila (no hay
     * un catalogo externo al que apunte, nombre y costo son libres).
     */
    private void sincronizarRepuestos(Servicio servicio, List<ServicioRepuestoRequest> requests) {
        Map<Long, ServicioRepuesto> existentesPorId = servicio.getRepuestos().stream()
                .collect(Collectors.toMap(ServicioRepuesto::getId, Function.identity()));

        Set<Long> idsSolicitados = requests.stream()
                .map(ServicioRepuestoRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        servicio.getRepuestos().removeIf(sr -> !idsSolicitados.contains(sr.getId()));

        for (ServicioRepuestoRequest r : requests) {
            ServicioRepuesto existente = r.id() != null ? existentesPorId.get(r.id()) : null;
            if (existente != null) {
                existente.setNombre(r.nombre());
                existente.setCantidad(r.cantidad());
                existente.setCostoUnitario(r.costoUnitario());
                continue;
            }

            ServicioRepuesto servicioRepuesto = new ServicioRepuesto();
            servicioRepuesto.setServicio(servicio);
            servicioRepuesto.setNombre(r.nombre());
            servicioRepuesto.setCantidad(r.cantidad());
            servicioRepuesto.setCostoUnitario(r.costoUnitario());
            servicio.getRepuestos().add(servicioRepuesto);
        }
    }

    /** Mismo motivo que sincronizarRepuestos: actualiza en el sitio en vez de clear()+add(). */
    private void sincronizarChecklist(Servicio servicio, Equipo equipo,
                                       List<ServicioChecklistRespuestaRequest> requests) {
        Map<Long, ServicioChecklistRespuesta> existentesPorItem = servicio.getChecklist().stream()
                .collect(Collectors.toMap(c -> c.getItem().getId(), Function.identity()));

        Set<Long> idsSolicitados = requests.stream()
                .map(ServicioChecklistRespuestaRequest::itemId)
                .collect(Collectors.toSet());

        servicio.getChecklist().removeIf(c -> !idsSolicitados.contains(c.getItem().getId()));

        for (ServicioChecklistRespuestaRequest c : requests) {
            ServicioChecklistRespuesta existente = existentesPorItem.get(c.itemId());
            if (existente != null) {
                existente.setCompletado(c.completado());
                existente.setObservacion(c.observacion());
                continue;
            }

            ChecklistItemCatalogo item = checklistItemRepository.findById(c.itemId())
                    .orElseThrow(() -> new ChecklistItemNotFoundException(c.itemId()));
            if (!item.getCategoria().getId().equals(equipo.getCategoria().getId())) {
                throw new ChecklistItemCategoriaMismatchException(item.getId(), equipo.getCategoria().getNombre());
            }

            ServicioChecklistRespuesta respuesta = new ServicioChecklistRespuesta();
            respuesta.setServicio(servicio);
            respuesta.setItem(item);
            respuesta.setCompletado(c.completado());
            respuesta.setObservacion(c.observacion());
            servicio.getChecklist().add(respuesta);
        }
    }

    private Servicio buscarPorId(Long id) {
        return servicioRepository.findById(id).orElseThrow(() -> new ServicioNotFoundException(id));
    }
}
