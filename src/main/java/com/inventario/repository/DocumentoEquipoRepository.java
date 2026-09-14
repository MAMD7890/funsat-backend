package com.inventario.repository;

import com.inventario.entity.DocumentoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoEquipoRepository extends JpaRepository<DocumentoEquipo, Long> {

    List<DocumentoEquipo> findByEquipoIdOrderByFechaDesc(Long equipoId);
}
