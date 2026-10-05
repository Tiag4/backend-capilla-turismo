package com.upc.demo.servicio.storage;

import com.upc.demo.dto.media.UploadedMediaDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de servicio de almacenamiento de medios agnóstico (Hexagonal Architecture).
 * Desacopla la lógica de negocio y controladores del proveedor concreto (Cloudinary, Local, S3, etc.).
 */
public interface StorageService {

    /**
     * Sube un archivo de medio al almacenamiento configurado.
     *
     * @param file Archivo multipart a subir
     * @param folder Carpeta o prefijo lógico de destino
     * @return DTO con metadatos del medio subido (URL, publicId, tamaño, etc.)
     */
    UploadedMediaDto upload(MultipartFile file, String folder);

    /**
     * Elimina un archivo de medio del almacenamiento mediante su identificador público.
     *
     * @param publicId Identificador público del recurso a eliminar
     */
    void delete(String publicId);
}
