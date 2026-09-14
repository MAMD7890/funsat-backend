package com.inventario.service;

import com.inventario.dto.request.ChecklistItemRequest;
import com.inventario.dto.response.ChecklistItemResponse;
import com.inventario.entity.CategoriaCatalogo;
import com.inventario.entity.ChecklistItemCatalogo;
import com.inventario.exception.CategoriaCatalogoNotFoundException;
import com.inventario.exception.ChecklistItemNotFoundException;
import com.inventario.repository.CategoriaCatalogoRepository;
import com.inventario.repository.ChecklistItemCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistItemService {

    private final ChecklistItemCatalogoRepository checklistItemRepository;
    private final CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> listar(Long categoriaId) {
        List<ChecklistItemCatalogo> items = categoriaId != null
                ? checklistItemRepository.findByCategoriaIdOrderByOrdenAsc(categoriaId)
                : checklistItemRepository.findAllByOrderByCategoriaIdAscOrdenAsc();
        return items.stream().map(ChecklistItemResponse::from).toList();
    }

    @Transactional
    public ChecklistItemResponse crear(ChecklistItemRequest request) {
        ChecklistItemCatalogo item = ChecklistItemCatalogo.builder()
                .categoria(buscarCategoria(request.categoriaId()))
                .nombre(request.nombre())
                .orden(request.orden())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        return ChecklistItemResponse.from(checklistItemRepository.save(item));
    }

    @Transactional
    public ChecklistItemResponse actualizar(Long id, ChecklistItemRequest request) {
        ChecklistItemCatalogo item = buscarPorId(id);
        item.setCategoria(buscarCategoria(request.categoriaId()));
        item.setNombre(request.nombre());
        item.setOrden(request.orden());
        if (request.activo() != null) {
            item.setActivo(request.activo());
        }

        return ChecklistItemResponse.from(checklistItemRepository.save(item));
    }

    private CategoriaCatalogo buscarCategoria(Long categoriaId) {
        return categoriaCatalogoRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaCatalogoNotFoundException(categoriaId));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!checklistItemRepository.existsById(id)) {
            throw new ChecklistItemNotFoundException(id);
        }
        checklistItemRepository.deleteById(id);
    }

    private ChecklistItemCatalogo buscarPorId(Long id) {
        return checklistItemRepository.findById(id).orElseThrow(() -> new ChecklistItemNotFoundException(id));
    }
}
