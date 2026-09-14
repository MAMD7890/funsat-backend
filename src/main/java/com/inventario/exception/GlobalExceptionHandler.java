package com.inventario.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ApiErrorResponse> handleUsuarioYaExiste(UsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Usuario Ya Existe", ex.getMessage()));
    }

    @ExceptionHandler({CategoriaCatalogoYaExisteException.class, TipoMotorCatalogoYaExisteException.class})
    public ResponseEntity<ApiErrorResponse> handleCatalogoYaExiste(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Ya Existe", ex.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Credenciales Inválidas",
                        "El username o la contraseña son incorrectos"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Usuario Inactivo",
                        "El usuario está desactivado"));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Token Inválido", ex.getMessage()));
    }

    @ExceptionHandler({ClienteNotFoundException.class, EquipoNotFoundException.class, RepuestoNotFoundException.class,
            ServicioNotFoundException.class, ChecklistItemNotFoundException.class, UsuarioNotFoundException.class,
            EvidenciaFotograficaNotFoundException.class, RecordatorioNotFoundException.class,
            OrdenSalidaNotFoundException.class, OrdenSalidaItemNotFoundException.class,
            DocumentoEquipoNotFoundException.class, CategoriaCatalogoNotFoundException.class,
            TipoMotorCatalogoNotFoundException.class, OrdenSalidaItemAccesorioNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), "No Encontrado", ex.getMessage()));
    }

    @ExceptionHandler(EquipoEnCalleException.class)
    public ResponseEntity<ApiErrorResponse> handleEquipoEnCalle(EquipoEnCalleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Equipo No Disponible", ex.getMessage()));
    }

    @ExceptionHandler(OperacionUsuarioInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleOperacionUsuarioInvalida(OperacionUsuarioInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Operación No Permitida", ex.getMessage()));
    }

    @ExceptionHandler(AccesorioNoPerteneceException.class)
    public ResponseEntity<ApiErrorResponse> handleAccesorioNoPertenece(AccesorioNoPerteneceException ex) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Accesorio Inválido", ex.getMessage()));
    }

    @ExceptionHandler(ChecklistItemCategoriaMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleChecklistMismatch(ChecklistItemCategoriaMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Checklist Inválido", ex.getMessage()));
    }

    @ExceptionHandler(ArchivoNoValidoException.class)
    public ResponseEntity<ApiErrorResponse> handleArchivoNoValido(ArchivoNoValidoException ex) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Archivo Inválido", ex.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(StorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error de Almacenamiento",
                        ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Conflicto de Datos",
                        "La operación viola una restricción de integridad (código duplicado o registro referenciado por otra entidad)"));
    }

    /**
     * AuthorizationDeniedException (lanzada por @PreAuthorize desde Spring
     * Security 6) hereda de AccessDeniedException, pero @ExceptionHandler la
     * intercepta aqui -dentro del despacho normal de Spring MVC- antes de
     * que llegue a JwtAccessDeniedHandler (que solo ve denegaciones a nivel
     * de filtro). Sin este handler, caia en el generico de abajo -> 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(HttpStatus.FORBIDDEN.value(), "Acceso Denegado",
                        "Tu rol no tiene permisos para realizar esta acción"));
    }

    @ExceptionHandler(ImportFileException.class)
    public ResponseEntity<ApiErrorResponse> handleImportFile(ImportFileException ex) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Archivo Inválido", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Solicitud Inválida",
                        "Errores de validación", fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error Interno",
                        "Ocurrió un error inesperado"));
    }
}
