package com.inventario.repository;

import com.inventario.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {

    @Query("select coalesce(sum(s.costoValorizado), 0) from Servicio s")
    BigDecimal sumCostoValorizado();

    @Query("select coalesce(sum(s.costoValorizado), 0) from Servicio s where s.fecha >= :desde")
    BigDecimal sumCostoValorizadoDesde(@Param("desde") LocalDate desde);

    @Query("select coalesce(sum(sr.costoUnitario * sr.cantidad), 0) from Servicio s join s.repuestos sr")
    BigDecimal sumCostoRepuestos();

    @Query("select coalesce(sum(sr.costoUnitario * sr.cantidad), 0) from Servicio s join s.repuestos sr where s.fecha >= :desde")
    BigDecimal sumCostoRepuestosDesde(@Param("desde") LocalDate desde);
}
