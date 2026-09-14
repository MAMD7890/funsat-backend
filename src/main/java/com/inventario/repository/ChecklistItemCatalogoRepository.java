package com.inventario.repository;

import com.inventario.entity.ChecklistItemCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemCatalogoRepository extends JpaRepository<ChecklistItemCatalogo, Long> {

    List<ChecklistItemCatalogo> findByCategoriaIdOrderByOrdenAsc(Long categoriaId);

    List<ChecklistItemCatalogo> findAllByOrderByCategoriaIdAscOrdenAsc();
}
