package com.inventario.repository;

import com.inventario.entity.OrdenSalidaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenSalidaItemRepository extends JpaRepository<OrdenSalidaItem, Long> {

    boolean existsByEquipo_IdAndFechaDevolucionIsNull(Long equipoId);

    @Query("select distinct i.equipo.id from OrdenSalidaItem i where i.fechaDevolucion is null")
    List<Long> findEquipoIdsEnCalle();

    @Query("select i from OrdenSalidaItem i join fetch i.ordenSalida o join fetch o.cliente "
            + "where i.equipo.id = :equipoId and i.fechaDevolucion is null")
    Optional<OrdenSalidaItem> findAbiertoPorEquipoId(@Param("equipoId") Long equipoId);

    @Query("select i from OrdenSalidaItem i join fetch i.ordenSalida o join fetch o.cliente "
            + "where i.equipo.id in :equipoIds and i.fechaDevolucion is null")
    List<OrdenSalidaItem> findAbiertosPorEquipoIds(@Param("equipoIds") List<Long> equipoIds);

    @Query("select i from OrdenSalidaItem i join fetch i.equipo join fetch i.ordenSalida o join fetch o.cliente "
            + "where i.fechaDevolucion is null order by o.fechaSalida desc")
    List<OrdenSalidaItem> findTodosAbiertos();
}
