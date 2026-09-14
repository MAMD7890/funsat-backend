package com.inventario.repository;

import com.inventario.entity.CategoriaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaCatalogoRepository extends JpaRepository<CategoriaCatalogo, Long> {

    List<CategoriaCatalogo> findAllByOrderByOrdenAscNombreAsc();

    List<CategoriaCatalogo> findByActivoTrueOrderByOrdenAscNombreAsc();

    Optional<CategoriaCatalogo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
