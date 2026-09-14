package com.inventario.repository;

import com.inventario.entity.OrdenSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrdenSalidaRepository extends JpaRepository<OrdenSalida, Long>, JpaSpecificationExecutor<OrdenSalida> {
}
