package com.inventario.repository;

import com.inventario.entity.OrdenSalidaItemAccesorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenSalidaItemAccesorioRepository extends JpaRepository<OrdenSalidaItemAccesorio, Long> {

    @Query("select oia from OrdenSalidaItemAccesorio oia "
            + "join fetch oia.ordenSalidaItem oi join fetch oi.ordenSalida o join fetch o.cliente "
            + "where oia.accesorio.equipo.id = :equipoId and oia.fechaDevolucion is null")
    List<OrdenSalidaItemAccesorio> findPendientesPorEquipoId(@Param("equipoId") Long equipoId);

    @Query("select oia from OrdenSalidaItemAccesorio oia "
            + "join fetch oia.ordenSalidaItem oi join fetch oi.ordenSalida o join fetch o.cliente "
            + "where oia.accesorio.equipo.id in :equipoIds and oia.fechaDevolucion is null")
    List<OrdenSalidaItemAccesorio> findPendientesPorEquipoIds(@Param("equipoIds") List<Long> equipoIds);
}
