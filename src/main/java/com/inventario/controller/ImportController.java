package com.inventario.controller;

import com.inventario.dto.response.ImportResultResponse;
import com.inventario.exception.ImportFileException;
import com.inventario.excel.EquipoExcelImportService;
import com.inventario.excel.PlantillaExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/importacion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Importación Excel")
public class ImportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final EquipoExcelImportService importService;
    private final PlantillaExcelService plantillaService;

    @PostMapping(value = "/equipos-master", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar equipos propios desde Inventario_Master.xlsx (solo ADMIN)")
    public ResponseEntity<ImportResultResponse> importarMaster(@RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(importService.importarMaster(abrirStream(archivo)));
    }

    @PostMapping(value = "/equipos-taller", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar equipos externos desde Inventario_SERVICIO_TALLER.xlsx (solo ADMIN)")
    public ResponseEntity<ImportResultResponse> importarTaller(@RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(importService.importarTaller(abrirStream(archivo)));
    }

    @GetMapping("/plantilla/master")
    @Operation(summary = "Descargar plantilla de ejemplo para Inventario Master (solo ADMIN)")
    public ResponseEntity<byte[]> plantillaMaster() {
        return descargarXlsx(plantillaService.generarPlantillaMaster(), "Plantilla_Inventario_Master.xlsx");
    }

    @GetMapping("/plantilla/taller")
    @Operation(summary = "Descargar plantilla de ejemplo para Servicio Taller (solo ADMIN)")
    public ResponseEntity<byte[]> plantillaTaller() {
        return descargarXlsx(plantillaService.generarPlantillaTaller(), "Plantilla_Servicio_Taller.xlsx");
    }

    private ResponseEntity<byte[]> descargarXlsx(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }

    private InputStream abrirStream(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ImportFileException("El archivo Excel es obligatorio");
        }
        try {
            return archivo.getInputStream();
        } catch (IOException e) {
            throw new ImportFileException("No se pudo leer el archivo: " + e.getMessage());
        }
    }
}
