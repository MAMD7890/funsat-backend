package com.inventario.service;

import com.inventario.dto.request.ClienteRequest;
import com.inventario.dto.response.ClienteResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.Cliente;
import com.inventario.exception.ClienteNotFoundException;
import com.inventario.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String q, Pageable pageable) {
        Specification<Cliente> spec = Specification.where(null);
        if (q != null && !q.isBlank()) {
            String patron = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron));
        }
        return clienteRepository.findAll(spec, pageable).map(ClienteResponse::from);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.from(buscarPorId(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .tipo(request.tipo())
                .telefono(request.telefono())
                .email(request.email())
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Cliente", guardado.getId(), guardado.getNombre());
        return ClienteResponse.from(guardado);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarPorId(id);
        cliente.setNombre(request.nombre());
        cliente.setTipo(request.tipo());
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());

        Cliente guardado = clienteRepository.save(cliente);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Cliente", guardado.getId(), guardado.getNombre());
        return ClienteResponse.from(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Cliente", id, cliente.getNombre());
    }

    private Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
    }
}
