package com.inventario.controller;

import com.inventario.dto.response.EvidenciaFotograficaResponse;
import com.inventario.entity.TipoEvidencia;
import com.inventario.service.EvidenciaFotograficaService;
import com.inventario.storage.ArchivoDescargable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/servicios/{servicioId}/evidencias")
@RequiredArgsConstructor
@Tag(name = "Evidencia Fotográfica")
public class EvidenciaFotograficaController {

    private final EvidenciaFotograficaService evidenciaService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'VER')")
    @Operation(summary = "Listar la evidencia fotográfica de un servicio")
    public List<EvidenciaFotograficaResponse> listar(@PathVariable Long servicioId) {
        return evidenciaService.listar(servicioId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'CREAR')")
    @Operation(summary = "Subir evidencia fotográfica (ANTES/DESPUES) para un servicio")
    public ResponseEntity<EvidenciaFotograficaResponse> subir(
            @PathVariable Long servicioId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(name = "tipoEvidencia", defaultValue = "ANTES") TipoEvidencia tipoEvidencia,
            @RequestParam(required = false) String descripcion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenciaService.subir(servicioId, archivo, tipoEvidencia, descripcion));
    }

    @GetMapping("/{evidenciaId}/archivo")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'VER')")
    @Operation(summary = "Descargar los bytes de una evidencia fotográfica")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long servicioId, @PathVariable Long evidenciaId) {
        ArchivoDescargable archivo = evidenciaService.descargar(servicioId, evidenciaId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(archivo.nombreOriginal() != null ? archivo.nombreOriginal() : "evidencia",
                        StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(archivo.recurso());
    }

    @DeleteMapping("/{evidenciaId}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar una evidencia fotográfica (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long servicioId, @PathVariable Long evidenciaId) {
        evidenciaService.eliminar(servicioId, evidenciaId);
        return ResponseEntity.noContent().build();
    }
}
