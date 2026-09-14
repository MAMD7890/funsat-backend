package com.inventario.repository;

import com.inventario.entity.Rol;
import com.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Usuario> findByActivoTrueOrderByNombreAsc();

    List<Usuario> findByActivoTrueAndRolOrderByNombreAsc(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();

    long countByRolAndActivoTrue(Rol rol);
}
