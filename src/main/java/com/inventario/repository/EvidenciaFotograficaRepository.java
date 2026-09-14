package com.inventario.repository;

import com.inventario.entity.EvidenciaFotografica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaFotograficaRepository extends JpaRepository<EvidenciaFotografica, Long> {

    List<EvidenciaFotografica> findByServicioIdOrderByFechaAsc(Long servicioId);
}
