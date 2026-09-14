package com.inventario.service;

import com.inventario.dto.response.AuditoriaResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.Auditoria;
import com.inventario.entity.Usuario;
import com.inventario.repository.AuditoriaRepository;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Registro explicito de auditoria: cada servicio de negocio llama a
 * {@link #registrar} al final de crear/actualizar/eliminar. Se decidio NO
 * usar listeners automaticos de Hibernate (@PostPersist/@PostUpdate) para
 * mantener control total sobre la descripcion legible de cada evento y
 * evitar el anti-patron de exponer el ApplicationContext a un listener JPA
 * que no es un bean de Spring.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrar(AccionAuditoria accion, String entidad, Long entidadId, String descripcion) {
        String username = usernameActual();

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuarioUsername(username);
        auditoria.setUsuarioNombre(resolverNombre(username));
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setDescripcion(descripcion);

        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> buscar(String usuario, String entidad, LocalDateTime desde, LocalDateTime hasta,
                                           Pageable pageable) {
        Specification<Auditoria> spec = Specification.where(null);

        if (usuario != null && !usuario.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("usuarioUsername"), usuario));
        }
        if (entidad != null && !entidad.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entidad"), entidad));
        }
        if (desde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        }
        if (hasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), hasta));
        }

        return auditoriaRepository.findAll(spec, pageable).map(AuditoriaResponse::from);
    }

    private String usernameActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private String resolverNombre(String username) {
        if (username == null) {
            return null;
        }
        return usuarioRepository.findByUsername(username).map(Usuario::getNombre).orElse(username);
    }
}
