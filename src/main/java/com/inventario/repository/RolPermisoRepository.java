package com.inventario.repository;

import com.inventario.entity.Accion;
import com.inventario.entity.Modulo;
import com.inventario.entity.Rol;
import com.inventario.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    boolean existsByRolAndModuloAndAccionAndPermitidoTrue(Rol rol, Modulo modulo, Accion accion);

    List<RolPermiso> findByRolOrderByModuloAscAccionAsc(Rol rol);

    Optional<RolPermiso> findByRolAndModuloAndAccion(Rol rol, Modulo modulo, Accion accion);

    List<RolPermiso> findAllByOrderByRolAscModuloAscAccionAsc();
}
