package com.inventario.service;

import com.inventario.dto.request.CategoriaCatalogoRequest;
import com.inventario.dto.response.CategoriaCatalogoResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.CategoriaCatalogo;
import com.inventario.exception.CategoriaCatalogoNotFoundException;
import com.inventario.exception.CategoriaCatalogoYaExisteException;
import com.inventario.repository.CategoriaCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaCatalogoService {

    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<CategoriaCatalogoResponse> listar(boolean soloActivas) {
        List<CategoriaCatalogo> categorias = soloActivas
                ? categoriaCatalogoRepository.findByActivoTrueOrderByOrdenAscNombreAsc()
                : categoriaCatalogoRepository.findAllByOrderByOrdenAscNombreAsc();
        return categorias.stream().map(CategoriaCatalogoResponse::from).toList();
    }

    @Transactional
    public CategoriaCatalogoResponse crear(CategoriaCatalogoRequest request) {
        if (categoriaCatalogoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new CategoriaCatalogoYaExisteException(request.nombre());
        }

        CategoriaCatalogo categoria = CategoriaCatalogo.builder()
                .nombre(request.nombre())
                .requiereNumeroSerie(request.requiereNumeroSerie())
                .orden(request.orden())
                .activo(request.activo() == null || request.activo())
                .build();

        CategoriaCatalogo guardada = categoriaCatalogoRepository.save(categoria);
        auditoriaService.registrar(AccionAuditoria.CREAR, "CategoriaCatalogo", guardada.getId(), guardada.getNombre());
        return CategoriaCatalogoResponse.from(guardada);
    }

    @Transactional
    public CategoriaCatalogoResponse actualizar(Long id, CategoriaCatalogoRequest request) {
        CategoriaCatalogo categoria = buscarPorId(id);

        if (categoriaCatalogoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new CategoriaCatalogoYaExisteException(request.nombre());
        }

        categoria.setNombre(request.nombre());
        categoria.setRequiereNumeroSerie(request.requiereNumeroSerie());
        categoria.setOrden(request.orden());
        if (request.activo() != null) {
            categoria.setActivo(request.activo());
        }

        CategoriaCatalogo guardada = categoriaCatalogoRepository.save(categoria);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "CategoriaCatalogo", guardada.getId(), guardada.getNombre());
        return CategoriaCatalogoResponse.from(guardada);
    }

    private CategoriaCatalogo buscarPorId(Long id) {
        return categoriaCatalogoRepository.findById(id)
                .orElseThrow(() -> new CategoriaCatalogoNotFoundException(id));
    }
}
