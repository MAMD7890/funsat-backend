package com.inventario.service;

import com.inventario.dto.response.EvidenciaFotograficaResponse;
import com.inventario.entity.EvidenciaFotografica;
import com.inventario.entity.Servicio;
import com.inventario.entity.TipoEvidencia;
import com.inventario.exception.ArchivoNoValidoException;
import com.inventario.exception.EvidenciaFotograficaNotFoundException;
import com.inventario.exception.ServicioNotFoundException;
import com.inventario.repository.EvidenciaFotograficaRepository;
import com.inventario.repository.ServicioRepository;
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
public class EvidenciaFotograficaService {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final EvidenciaFotograficaRepository evidenciaRepository;
    private final ServicioRepository servicioRepository;
    private final AlmacenamientoArchivos almacenamiento;

    @Transactional(readOnly = true)
    public List<EvidenciaFotograficaResponse> listar(Long servicioId) {
        if (!servicioRepository.existsById(servicioId)) {
            throw new ServicioNotFoundException(servicioId);
        }
        return evidenciaRepository.findByServicioIdOrderByFechaAsc(servicioId).stream()
                .map(EvidenciaFotograficaResponse::from)
                .toList();
    }

    @Transactional
    public EvidenciaFotograficaResponse subir(Long servicioId, MultipartFile archivo, TipoEvidencia tipoEvidencia,
                                               String descripcion) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));

        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoNoValidoException("El archivo es obligatorio");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ArchivoNoValidoException("El archivo debe ser una imagen JPEG, PNG, WEBP o GIF");
        }

        String rutaRelativa = almacenamiento.guardar(archivo, "servicio-" + servicioId);

        EvidenciaFotografica evidencia = new EvidenciaFotografica();
        evidencia.setServicio(servicio);
        evidencia.setTipoEvidencia(tipoEvidencia != null ? tipoEvidencia : TipoEvidencia.ANTES);
        evidencia.setRutaArchivo(rutaRelativa);
        evidencia.setNombreOriginal(archivo.getOriginalFilename());
        evidencia.setContentType(contentType);
        evidencia.setTamanoBytes(archivo.getSize());
        evidencia.setDescripcion(descripcion);

        return EvidenciaFotograficaResponse.from(evidenciaRepository.save(evidencia));
    }

    @Transactional(readOnly = true)
    public ArchivoDescargable descargar(Long servicioId, Long evidenciaId) {
        EvidenciaFotografica evidencia = buscarEvidencia(servicioId, evidenciaId);
        return new ArchivoDescargable(
                almacenamiento.cargar(evidencia.getRutaArchivo()),
                evidencia.getContentType(),
                evidencia.getNombreOriginal()
        );
    }

    @Transactional
    public void eliminar(Long servicioId, Long evidenciaId) {
        EvidenciaFotografica evidencia = buscarEvidencia(servicioId, evidenciaId);
        almacenamiento.eliminar(evidencia.getRutaArchivo());
        evidenciaRepository.delete(evidencia);
    }

    private EvidenciaFotografica buscarEvidencia(Long servicioId, Long evidenciaId) {
        EvidenciaFotografica evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new EvidenciaFotograficaNotFoundException(evidenciaId));
        if (!evidencia.getServicio().getId().equals(servicioId)) {
            throw new EvidenciaFotograficaNotFoundException(evidenciaId);
        }
        return evidencia;
    }
}
