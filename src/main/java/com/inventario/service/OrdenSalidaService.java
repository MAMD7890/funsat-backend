package com.inventario.service;

import com.inventario.dto.request.OrdenSalidaItemRequest;
import com.inventario.dto.request.OrdenSalidaRequest;
import com.inventario.dto.request.RegistrarDevolucionRequest;
import com.inventario.dto.response.OrdenSalidaResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.Cliente;
import com.inventario.entity.Equipo;
import com.inventario.entity.EquipoAccesorio;
import com.inventario.entity.OrdenSalida;
import com.inventario.entity.OrdenSalidaItem;
import com.inventario.entity.OrdenSalidaItemAccesorio;
import com.inventario.entity.Usuario;
import com.inventario.exception.AccesorioNoPerteneceException;
import com.inventario.exception.ClienteNotFoundException;
import com.inventario.exception.EquipoEnCalleException;
import com.inventario.exception.EquipoNotFoundException;
import com.inventario.exception.OrdenSalidaItemAccesorioNotFoundException;
import com.inventario.exception.OrdenSalidaItemNotFoundException;
import com.inventario.exception.OrdenSalidaNotFoundException;
import com.inventario.exception.UsuarioNotFoundException;
import com.inventario.repository.ClienteRepository;
import com.inventario.repository.EquipoAccesorioRepository;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.OrdenSalidaItemRepository;
import com.inventario.repository.OrdenSalidaRepository;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenSalidaService {

    private final OrdenSalidaRepository ordenSalidaRepository;
    private final OrdenSalidaItemRepository ordenSalidaItemRepository;
    private final EquipoRepository equipoRepository;
    private final EquipoAccesorioRepository equipoAccesorioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<OrdenSalidaResponse> buscar(Long clienteId, LocalDate desde, LocalDate hasta, String q,
                                             Pageable pageable) {
        Specification<OrdenSalida> spec = Specification.where(null);
        if (clienteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        }
        if (desde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaSalida"), desde));
        }
        if (hasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaSalida"), hasta));
        }
        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("cliente").get("nombre")), patron));
        }
        return ordenSalidaRepository.findAll(spec, pageable).map(OrdenSalidaResponse::from);
    }

    @Transactional(readOnly = true)
    public OrdenSalidaResponse obtener(Long id) {
        return OrdenSalidaResponse.from(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public List<Long> equiposEnCalle() {
        return ordenSalidaItemRepository.findEquipoIdsEnCalle();
    }

    @Transactional
    public OrdenSalidaResponse crear(OrdenSalidaRequest request, String creadoPorUsername) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNotFoundException(request.clienteId()));
        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new UsuarioNotFoundException(request.responsableId()));
        Usuario creadoPor = usuarioRepository.findByUsername(creadoPorUsername).orElse(null);

        OrdenSalida orden = new OrdenSalida();
        orden.setFechaSalida(request.fechaSalida());
        orden.setCliente(cliente);
        orden.setResponsable(responsable);
        orden.setObservaciones(request.observaciones());
        orden.setCreadoPor(creadoPor);

        for (OrdenSalidaItemRequest itemRequest : request.items()) {
            Equipo equipo = equipoRepository.findById(itemRequest.equipoId())
                    .orElseThrow(() -> new EquipoNotFoundException(itemRequest.equipoId()));
            if (ordenSalidaItemRepository.existsByEquipo_IdAndFechaDevolucionIsNull(equipo.getId())) {
                throw new EquipoEnCalleException(equipo.getDescripcionEquipo());
            }

            OrdenSalidaItem item = new OrdenSalidaItem();
            item.setOrdenSalida(orden);
            item.setEquipo(equipo);
            item.setObservacionSalida(itemRequest.observacionSalida());
            sincronizarAccesoriosItem(item, equipo, itemRequest.accesorioIds());
            orden.getItems().add(item);
        }

        OrdenSalida guardada = ordenSalidaRepository.save(orden);
        auditoriaService.registrar(AccionAuditoria.CREAR, "OrdenSalida", guardada.getId(),
                descripcionAuditoria(guardada));
        return OrdenSalidaResponse.from(guardada);
    }

    @Transactional
    public OrdenSalidaResponse actualizar(Long id, OrdenSalidaRequest request) {
        OrdenSalida orden = buscarPorId(id);

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNotFoundException(request.clienteId()));
        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new UsuarioNotFoundException(request.responsableId()));

        orden.setFechaSalida(request.fechaSalida());
        orden.setCliente(cliente);
        orden.setResponsable(responsable);
        orden.setObservaciones(request.observaciones());

        sincronizarItems(orden, request.items());

        OrdenSalida guardada = ordenSalidaRepository.save(orden);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "OrdenSalida", guardada.getId(),
                descripcionAuditoria(guardada));
        return OrdenSalidaResponse.from(guardada);
    }

    /**
     * Reconcilia en el sitio (mismo patron que ServicioService con
     * repuestos/checklist): evita el clear()+add() que choca con
     * orphanRemoval=true al reinsertar un equipo que ya estaba en la orden
     * dentro del mismo flush. Solo valida "equipo en la calle" para los
     * equipos NUEVOS que se agregan, nunca para los que ya estaban.
     */
    private void sincronizarItems(OrdenSalida orden, List<OrdenSalidaItemRequest> requests) {
        Map<Long, OrdenSalidaItem> existentesPorEquipo = orden.getItems().stream()
                .collect(Collectors.toMap(i -> i.getEquipo().getId(), Function.identity()));

        Set<Long> idsSolicitados = requests.stream()
                .map(OrdenSalidaItemRequest::equipoId)
                .collect(Collectors.toSet());

        orden.getItems().removeIf(i -> !idsSolicitados.contains(i.getEquipo().getId()));

        for (OrdenSalidaItemRequest r : requests) {
            OrdenSalidaItem existente = existentesPorEquipo.get(r.equipoId());
            if (existente != null) {
                existente.setObservacionSalida(r.observacionSalida());
                sincronizarAccesoriosItem(existente, existente.getEquipo(), r.accesorioIds());
                continue;
            }

            Equipo equipo = equipoRepository.findById(r.equipoId())
                    .orElseThrow(() -> new EquipoNotFoundException(r.equipoId()));
            if (ordenSalidaItemRepository.existsByEquipo_IdAndFechaDevolucionIsNull(equipo.getId())) {
                throw new EquipoEnCalleException(equipo.getDescripcionEquipo());
            }

            OrdenSalidaItem item = new OrdenSalidaItem();
            item.setOrdenSalida(orden);
            item.setEquipo(equipo);
            item.setObservacionSalida(r.observacionSalida());
            sincronizarAccesoriosItem(item, equipo, r.accesorioIds());
            orden.getItems().add(item);
        }
    }

    /**
     * Reconcilia en el sitio (mismo patron que sincronizarItems): evita
     * clear()+add(), que chocaria con orphanRemoval=true y ademas perderia
     * el estado de devolucion ya registrado de un accesorio que sigue
     * seleccionado. Valida que cada accesorio pertenezca realmente al
     * equipo de esta fila.
     */
    private void sincronizarAccesoriosItem(OrdenSalidaItem item, Equipo equipo, List<Long> accesorioIds) {
        Map<Long, OrdenSalidaItemAccesorio> existentesPorAccesorioId = item.getAccesorios().stream()
                .collect(Collectors.toMap(oia -> oia.getAccesorio().getId(), Function.identity()));

        Set<Long> idsSolicitados = Set.copyOf(accesorioIds);
        item.getAccesorios().removeIf(oia -> !idsSolicitados.contains(oia.getAccesorio().getId()));

        if (accesorioIds.isEmpty()) {
            return;
        }

        Set<Long> idsDelEquipo = equipo.getAccesoriosRegistrados().stream()
                .map(EquipoAccesorio::getId)
                .collect(Collectors.toSet());

        for (Long accesorioId : accesorioIds) {
            if (existentesPorAccesorioId.containsKey(accesorioId)) {
                continue;
            }
            if (!idsDelEquipo.contains(accesorioId)) {
                throw new AccesorioNoPerteneceException(accesorioId, equipo.getDescripcionEquipo());
            }

            OrdenSalidaItemAccesorio nuevo = new OrdenSalidaItemAccesorio();
            nuevo.setOrdenSalidaItem(item);
            nuevo.setAccesorio(equipoAccesorioRepository.getReferenceById(accesorioId));
            item.getAccesorios().add(nuevo);
        }
    }

    /**
     * Registra la entrada del equipo. Los accesorios en accesorioIdsDevueltos
     * (que aun no estuvieran devueltos) se marcan devueltos junto con él; el
     * resto queda pendiente -el equipo puede volver sin todos sus accesorios,
     * y esos accesorios pendientes se registran despues con
     * {@link #registrarDevolucionAccesorio}-.
     */
    @Transactional
    public OrdenSalidaResponse registrarDevolucion(Long ordenId, Long itemId, RegistrarDevolucionRequest request) {
        OrdenSalida orden = buscarPorId(ordenId);
        OrdenSalidaItem item = orden.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrdenSalidaItemNotFoundException(itemId));

        Usuario recibidoPor = usuarioRepository.findById(request.recibidoPorId())
                .orElseThrow(() -> new UsuarioNotFoundException(request.recibidoPorId()));

        item.setFechaDevolucion(request.fechaDevolucion());
        item.setRecibidoPor(recibidoPor);
        item.setObservacionDevolucion(request.observacionDevolucion());

        Set<Long> accesorioIdsDevueltos = Set.copyOf(request.accesorioIdsDevueltos());
        for (OrdenSalidaItemAccesorio oia : item.getAccesorios()) {
            if (!oia.isDevuelto() && accesorioIdsDevueltos.contains(oia.getAccesorio().getId())) {
                oia.setFechaDevolucion(request.fechaDevolucion());
                oia.setRecibidoPor(recibidoPor);
            }
        }

        OrdenSalida guardada = ordenSalidaRepository.save(orden);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "OrdenSalida", guardada.getId(),
                "Devolucion registrada: " + item.getEquipo().getDescripcionEquipo());
        return OrdenSalidaResponse.from(guardada);
    }

    /** Registra la entrada de un accesorio que había quedado pendiente al devolver su equipo. */
    @Transactional
    public OrdenSalidaResponse registrarDevolucionAccesorio(Long ordenId, Long itemId, Long ordenSalidaItemAccesorioId,
                                                              RegistrarDevolucionRequest request) {
        OrdenSalida orden = buscarPorId(ordenId);
        OrdenSalidaItem item = orden.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrdenSalidaItemNotFoundException(itemId));

        OrdenSalidaItemAccesorio oia = item.getAccesorios().stream()
                .filter(a -> a.getId().equals(ordenSalidaItemAccesorioId))
                .findFirst()
                .orElseThrow(() -> new OrdenSalidaItemAccesorioNotFoundException(ordenSalidaItemAccesorioId));

        Usuario recibidoPor = usuarioRepository.findById(request.recibidoPorId())
                .orElseThrow(() -> new UsuarioNotFoundException(request.recibidoPorId()));

        oia.setFechaDevolucion(request.fechaDevolucion());
        oia.setRecibidoPor(recibidoPor);
        oia.setObservacionDevolucion(request.observacionDevolucion());

        OrdenSalida guardada = ordenSalidaRepository.save(orden);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "OrdenSalida", guardada.getId(),
                "Devolucion de accesorio registrada: " + oia.getAccesorio().getNombre()
                        + " (" + item.getEquipo().getDescripcionEquipo() + ")");
        return OrdenSalidaResponse.from(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        OrdenSalida orden = buscarPorId(id);
        ordenSalidaRepository.delete(orden);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "OrdenSalida", id, descripcionAuditoria(orden));
    }

    private String descripcionAuditoria(OrdenSalida orden) {
        String cliente = orden.getCliente() != null ? orden.getCliente().getNombre() : "?";
        return "Orden de salida - " + cliente;
    }

    OrdenSalida buscarPorId(Long id) {
        return ordenSalidaRepository.findById(id).orElseThrow(() -> new OrdenSalidaNotFoundException(id));
    }
}
