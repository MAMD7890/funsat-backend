package com.inventario.repository;

import com.inventario.entity.TipoMotorCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoMotorCatalogoRepository extends JpaRepository<TipoMotorCatalogo, Long> {

    List<TipoMotorCatalogo> findAllByOrderByOrdenAscNombreAsc();

    List<TipoMotorCatalogo> findByActivoTrueOrderByOrdenAscNombreAsc();

    Optional<TipoMotorCatalogo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
