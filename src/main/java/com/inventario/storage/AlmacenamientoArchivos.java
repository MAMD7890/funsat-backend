package com.inventario.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstrae dónde viven los archivos subidos (evidencia fotográfica, y en el
 * futuro cualquier otro adjunto). La única implementación de hoy guarda en
 * el filesystem local; el resto del código depende solo de esta interfaz
 * para poder migrar a S3/Cloudinary más adelante sin tocar servicios ni
 * controllers.
 */
public interface AlmacenamientoArchivos {

    /** Guarda el archivo bajo la subcarpeta indicada y devuelve la ruta relativa para persistir en BD. */
    String guardar(MultipartFile archivo, String subcarpeta);

    /** Carga el contenido del archivo a partir de la ruta relativa guardada en BD. */
    Resource cargar(String rutaRelativa);

    /** Elimina el archivo; no falla si ya no existe. */
    void eliminar(String rutaRelativa);
}
