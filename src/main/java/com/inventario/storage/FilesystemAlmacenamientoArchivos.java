package com.inventario.storage;

import com.inventario.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FilesystemAlmacenamientoArchivos implements AlmacenamientoArchivos {

    private final Path raiz;

    public FilesystemAlmacenamientoArchivos(@Value("${app.uploads.dir}") String uploadsDir) {
        this.raiz = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio de uploads: " + raiz);
        }
    }

    @Override
    public String guardar(MultipartFile archivo, String subcarpeta) {
        Path carpetaDestino = resolverRutaSegura(subcarpeta);

        try {
            Files.createDirectories(carpetaDestino);
            String nombreArchivo = UUID.randomUUID() + extraerExtension(archivo.getOriginalFilename());
            Path destino = carpetaDestino.resolve(nombreArchivo);
            archivo.transferTo(destino);
            return raiz.relativize(destino).toString().replace('\\', '/');
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    public Resource cargar(String rutaRelativa) {
        Path archivo = resolverRutaSegura(rutaRelativa);
        try {
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new StorageException("Archivo no encontrado: " + rutaRelativa);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new StorageException("Ruta de archivo inválida: " + rutaRelativa);
        }
    }

    @Override
    public void eliminar(String rutaRelativa) {
        try {
            Files.deleteIfExists(resolverRutaSegura(rutaRelativa));
        } catch (IOException e) {
            throw new StorageException("No se pudo eliminar el archivo: " + e.getMessage());
        }
    }

    /** Evita path traversal (ej. subcarpeta/ruta con "../") fuera del directorio raíz de uploads. */
    private Path resolverRutaSegura(String rutaRelativa) {
        Path resuelta = raiz.resolve(rutaRelativa).normalize();
        if (!resuelta.startsWith(raiz)) {
            throw new StorageException("Ruta de archivo inválida");
        }
        return resuelta;
    }

    private String extraerExtension(String nombreOriginal) {
        if (nombreOriginal == null) {
            return "";
        }
        int idx = nombreOriginal.lastIndexOf('.');
        return idx >= 0 ? nombreOriginal.substring(idx) : "";
    }
}
