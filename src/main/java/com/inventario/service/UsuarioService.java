package com.inventario.service;

import com.inventario.dto.request.EditarPerfilRequest;
import com.inventario.dto.request.EditarUsuarioRequest;
import com.inventario.dto.request.RegisterRequest;
import com.inventario.dto.request.ResetPasswordRequest;
import com.inventario.dto.response.UsuarioResponse;
import com.inventario.dto.response.UsuarioResumenResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.Rol;
import com.inventario.entity.Usuario;
import com.inventario.exception.OperacionUsuarioInvalidaException;
import com.inventario.exception.UsuarioNotFoundException;
import com.inventario.exception.UsuarioYaExisteException;
import com.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<UsuarioResumenResponse> listar(Rol rol) {
        List<Usuario> usuarios = rol != null
                ? usuarioRepository.findByActivoTrueAndRolOrderByNombreAsc(rol)
                : usuarioRepository.findByActivoTrueOrderByNombreAsc();

        return usuarios.stream().map(UsuarioResumenResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse crear(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new UsuarioYaExisteException(request.username());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .username(request.username())
                .email(blankToNull(request.email()))
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(true)
                .debeCambiarPassword(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Usuario", guardado.getId(),
                guardado.getNombre() + " (" + guardado.getUsername() + ")");
        return UsuarioResponse.from(guardado);
    }

    @Transactional
    public UsuarioResponse editar(Long id, EditarUsuarioRequest request) {
        Usuario usuario = obtenerOLanzar(id);

        if (esUltimoAdminActivo(usuario) && request.rol() != Rol.ADMIN) {
            throw new OperacionUsuarioInvalidaException(
                    "No puedes cambiar el rol del último administrador activo del sistema");
        }

        usuario.setNombre(request.nombre());
        usuario.setEmail(blankToNull(request.email()));
        usuario.setRol(request.rol());
        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Usuario", guardado.getId(),
                guardado.getNombre() + " (" + guardado.getUsername() + ")");
        return UsuarioResponse.from(guardado);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo, String usernameActual) {
        Usuario usuario = obtenerOLanzar(id);

        if (!activo) {
            if (usuario.getUsername().equalsIgnoreCase(usernameActual)) {
                throw new OperacionUsuarioInvalidaException("No puedes desactivar tu propia cuenta");
            }
            if (esUltimoAdminActivo(usuario)) {
                throw new OperacionUsuarioInvalidaException(
                        "No puedes desactivar al último administrador activo del sistema");
            }
        }

        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Usuario", guardado.getId(),
                guardado.getNombre() + " (" + guardado.getUsername() + ") -> " + (activo ? "activado" : "desactivado"));
        return UsuarioResponse.from(guardado);
    }

    @Transactional
    public void resetearPassword(Long id, ResetPasswordRequest request) {
        Usuario usuario = obtenerOLanzar(id);
        usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuario.setDebeCambiarPassword(true);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(AccionAuditoria.EDITAR, "Usuario", usuario.getId(),
                "Contraseña restablecida para " + usuario.getNombre() + " (" + usuario.getUsername() + ")");
    }

    @Transactional
    public UsuarioResponse actualizarPerfilPropio(String username, EditarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        usuario.setNombre(request.nombre());
        usuario.setEmail(blankToNull(request.email()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    private boolean esUltimoAdminActivo(Usuario usuario) {
        return usuario.isActivo() && usuario.getRol() == Rol.ADMIN
                && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1;
    }

    private Usuario obtenerOLanzar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
