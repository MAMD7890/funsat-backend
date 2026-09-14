package com.inventario.controller;

import com.inventario.dto.response.DocumentoEquipoResponse;
import com.inventario.entity.TipoDocumentoEquipo;
import com.inventario.service.DocumentoEquipoService;
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
@RequestMapping("/equipos/{equipoId}/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos de Equipo")
public class DocumentoEquipoController {

    private final DocumentoEquipoService documentoService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'VER')")
    @Operation(summary = "Listar los documentos adjuntos de un equipo")
    public List<DocumentoEquipoResponse> listar(@PathVariable Long equipoId) {
        return documentoService.listar(equipoId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'EDITAR')")
    @Operation(summary = "Subir un documento (manual, factura, garantía, etc) para un equipo")
    public ResponseEntity<DocumentoEquipoResponse> subir(
            @PathVariable Long equipoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(name = "tipoDocumento", defaultValue = "OTRO") TipoDocumentoEquipo tipoDocumento,
            @RequestParam(required = false) String descripcion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoService.subir(equipoId, archivo, tipoDocumento, descripcion));
    }

    @GetMapping("/{documentoId}/archivo")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'VER')")
    @Operation(summary = "Descargar los bytes de un documento de equipo")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long equipoId, @PathVariable Long documentoId) {
        ArchivoDescargable archivo = documentoService.descargar(equipoId, documentoId);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(archivo.nombreOriginal() != null ? archivo.nombreOriginal() : "documento",
                        StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(archivo.recurso());
    }

    @DeleteMapping("/{documentoId}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'EDITAR')")
    @Operation(summary = "Eliminar un documento adjunto de un equipo")
    public ResponseEntity<Void> eliminar(@PathVariable Long equipoId, @PathVariable Long documentoId) {
        documentoService.eliminar(equipoId, documentoId);
        return ResponseEntity.noContent().build();
    }
}
