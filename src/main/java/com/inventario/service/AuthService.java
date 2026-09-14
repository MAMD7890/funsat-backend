package com.inventario.service;

import com.inventario.dto.request.ChangePasswordRequest;
import com.inventario.dto.request.LoginRequest;
import com.inventario.dto.request.RefreshTokenRequest;
import com.inventario.dto.request.RegisterRequest;
import com.inventario.dto.response.AuthResponse;
import com.inventario.dto.response.UsuarioResponse;
import com.inventario.entity.Usuario;
import com.inventario.exception.InvalidTokenException;
import com.inventario.repository.UsuarioRepository;
import com.inventario.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Delega en el AuthenticationManager (DaoAuthenticationProvider) para que
        // usuario inexistente / contraseña incorrecta / usuario inactivo se
        // resuelvan de forma consistente vía BadCredentialsException/DisabledException.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        return buildAuthResponse(usuario);
    }

    @Transactional
    public UsuarioResponse register(RegisterRequest request) {
        return usuarioService.crear(request);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        String username;

        try {
            if (!jwtService.isRefreshToken(token)) {
                throw new InvalidTokenException("El token proporcionado no es un refresh token válido");
            }
            username = jwtService.extractUsername(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Refresh token inválido o expirado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido o expirado"));

        if (!usuario.isActivo()) {
            throw new DisabledException("El usuario está desactivado");
        }
        if (!jwtService.isValid(token, usuario.getUsername())) {
            throw new InvalidTokenException("Refresh token inválido o expirado");
        }

        return buildAuthResponse(usuario);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!passwordEncoder.matches(request.currentPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        return new AuthResponse(
                jwtService.generateAccessToken(usuario),
                jwtService.generateRefreshToken(usuario),
                "Bearer",
                jwtService.getAccessTokenTtlMs() / 1000,
                UsuarioResponse.from(usuario)
        );
    }
}
