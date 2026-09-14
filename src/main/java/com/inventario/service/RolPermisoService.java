package com.inventario.service;

import com.inventario.dto.request.ActualizarPermisosRequest;
import com.inventario.dto.request.PermisoItemRequest;
import com.inventario.dto.response.PermisoResponse;
import com.inventario.dto.response.RolPermisosResponse;
import com.inventario.entity.Rol;
import com.inventario.entity.RolPermiso;
import com.inventario.repository.RolPermisoRepository;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<RolPermisosResponse> listarMatriz() {
        List<RolPermiso> todos = rolPermisoRepository.findAllByOrderByRolAscModuloAscAccionAsc();

        return Arrays.stream(Rol.values())
                .map(rol -> new RolPermisosResponse(
                        rol,
                        usuarioRepository.countByRolAndActivoTrue(rol),
                        todos.stream()
                                .filter(rp -> rp.getRol() == rol)
                                .map(PermisoResponse::from)
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public RolPermisosResponse actualizar(Rol rol, ActualizarPermisosRequest request) {
        for (PermisoItemRequest item : request.permisos()) {
            RolPermiso permiso = rolPermisoRepository.findByRolAndModuloAndAccion(rol, item.modulo(), item.accion())
                    .orElseGet(() -> RolPermiso.builder().rol(rol).modulo(item.modulo()).accion(item.accion()).build());
            permiso.setPermitido(item.permitido());
            rolPermisoRepository.save(permiso);
        }

        List<PermisoResponse> permisos = rolPermisoRepository.findByRolOrderByModuloAscAccionAsc(rol).stream()
                .map(PermisoResponse::from)
                .collect(Collectors.toList());

        return new RolPermisosResponse(rol, usuarioRepository.countByRolAndActivoTrue(rol), permisos);
    }
}
