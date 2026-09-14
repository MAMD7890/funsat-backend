package com.inventario.repository;

import com.inventario.entity.EstadoRecordatorio;
import com.inventario.entity.RecordatorioMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecordatorioMantenimientoRepository extends JpaRepository<RecordatorioMantenimiento, Long> {

    List<RecordatorioMantenimiento> findByFechaProgramadaBetweenOrderByFechaProgramadaAsc(LocalDate desde, LocalDate hasta);

    List<RecordatorioMantenimiento> findByEstadoAndFechaProgramadaBeforeOrderByFechaProgramadaAsc(
            EstadoRecordatorio estado, LocalDate fecha);

    List<RecordatorioMantenimiento> findByEstadoAndFechaProgramadaBetweenOrderByFechaProgramadaAsc(
            EstadoRecordatorio estado, LocalDate desde, LocalDate hasta);
}
