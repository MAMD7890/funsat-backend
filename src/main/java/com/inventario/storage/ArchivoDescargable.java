package com.inventario.storage;

import org.springframework.core.io.Resource;

public record ArchivoDescargable(Resource recurso, String contentType, String nombreOriginal) {
}
