package com.inventario.service;

import com.inventario.dto.response.DocumentoEquipoResponse;
import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.DocumentoEquipo;
import com.inventario.entity.Equipo;
import com.inventario.entity.TipoDocumentoEquipo;
import com.inventario.exception.ArchivoNoValidoException;
import com.inventario.exception.DocumentoEquipoNotFoundException;
import com.inventario.exception.EquipoNotFoundException;
import com.inventario.repository.DocumentoEquipoRepository;
import com.inventario.repository.EquipoRepository;
import com.inventario.storage.AlmacenamientoArchivos;
import com.inventario.storage.ArchivoDescargable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentoEquipoService {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg", "image/png", "image/webp",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final DocumentoEquipoRepository documentoRepository;
    private final EquipoRepository equipoRepository;
    private final AlmacenamientoArchivos almacenamiento;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<DocumentoEquipoResponse> listar(Long equipoId) {
        if (!equipoRepository.existsById(equipoId)) {
            throw new EquipoNotFoundException(equipoId);
        }
        return documentoRepository.findByEquipoIdOrderByFechaDesc(equipoId).stream()
                .map(DocumentoEquipoResponse::from)
                .toList();
    }

    @Transactional
    public DocumentoEquipoResponse subir(Long equipoId, MultipartFile archivo, TipoDocumentoEquipo tipoDocumento,
                                          String descripcion) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoNotFoundException(equipoId));

        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoNoValidoException("El archivo es obligatorio");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ArchivoNoValidoException(
                    "El archivo debe ser PDF, imagen (JPEG/PNG/WEBP) o documento de Word/Excel");
        }

        String rutaRelativa = almacenamiento.guardar(archivo, "equipo-" + equipoId);

        DocumentoEquipo documento = new DocumentoEquipo();
        documento.setEquipo(equipo);
        documento.setTipoDocumento(tipoDocumento != null ? tipoDocumento : TipoDocumentoEquipo.OTRO);
        documento.setRutaArchivo(rutaRelativa);
        documento.setNombreOriginal(archivo.getOriginalFilename());
        documento.setContentType(contentType);
        documento.setTamanoBytes(archivo.getSize());
        documento.setDescripcion(descripcion);

        DocumentoEquipo guardado = documentoRepository.save(documento);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Equipo", equipoId,
                "Documento adjunto: " + guardado.getNombreOriginal() + " (" + equipo.getDescripcionEquipo() + ")");
        return DocumentoEquipoResponse.from(guardado);
    }

    @Transactional(readOnly = true)
    public ArchivoDescargable descargar(Long equipoId, Long documentoId) {
        DocumentoEquipo documento = buscarDocumento(equipoId, documentoId);
        return new ArchivoDescargable(
                almacenamiento.cargar(documento.getRutaArchivo()),
                documento.getContentType(),
                documento.getNombreOriginal()
        );
    }

    @Transactional
    public void eliminar(Long equipoId, Long documentoId) {
        DocumentoEquipo documento = buscarDocumento(equipoId, documentoId);
        almacenamiento.eliminar(documento.getRutaArchivo());
        documentoRepository.delete(documento);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Equipo", equipoId,
                "Documento eliminado: " + documento.getNombreOriginal() + " ("
                        + documento.getEquipo().getDescripcionEquipo() + ")");
    }

    private DocumentoEquipo buscarDocumento(Long equipoId, Long documentoId) {
        DocumentoEquipo documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new DocumentoEquipoNotFoundException(documentoId));
        if (!documento.getEquipo().getId().equals(equipoId)) {
            throw new DocumentoEquipoNotFoundException(documentoId);
        }
        return documento;
    }
}
