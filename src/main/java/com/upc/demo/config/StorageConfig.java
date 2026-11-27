package com.upc.demo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.upc.demo.servicio.storage.StorageService;
import com.upc.demo.servicio.storage.impl.CloudinaryStorageService;
import com.upc.demo.servicio.storage.impl.LocalStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de inyección de dependencias para el servicio de almacenamiento agnóstico.
 */
@Slf4j
@Configuration
public class StorageConfig {

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${storage.local.upload-dir:uploads}")
    private String localUploadDir;

    @Value("${storage.local.base-url:}")
    private String localBaseUrl;

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary")
    public StorageService cloudinaryStorageService() {
        log.info("Inicializando proveedor de almacenamiento: CLOUDINARY");

        Cloudinary cloudinary;
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            cloudinary = new Cloudinary(cloudinaryUrl);
        } else {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", true);
            cloudinary = new Cloudinary(config);
        }
        return new CloudinaryStorageService(cloudinary);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
    public StorageService localStorageService() {
        log.info("Inicializando proveedor de almacenamiento: LOCAL (Directorio: {})", localUploadDir);
        return new LocalStorageService(localUploadDir, localBaseUrl);
    }
}
