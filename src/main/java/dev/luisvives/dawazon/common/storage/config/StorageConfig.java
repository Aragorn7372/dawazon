package dev.luisvives.dawazon.common.storage.config;

import dev.luisvives.dawazon.common.storage.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de almacenamiento de archivos.
 * <p>
 * Inicializa el sistema de almacenamiento y opcionalmente elimina archivos
 * existentes.
 * </p>
 */
@Configuration
@Slf4j
public class StorageConfig {
    /**
     * Servicio de almacenamiento.
     */
    private final StorageService storageService;

    /**
     * Indica si se deben borrar todos los archivos al iniciar.
     */
    @Value("${upload.delete}")
    private String deleteAll;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param storageService Servicio de almacenamiento.
     */
    @Autowired
    public StorageConfig(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Inicializa el sistema de almacenamiento después de la construcción.
     * <p>
     * Elimina archivos existentes si {@code upload.delete=true} en la
     * configuración.
     * </p>
     */
    @PostConstruct
    public void init() {
        if (deleteAll.equals("true")) {
            log.info("Borrando ficheros de almacenamiento...");
            storageService.deleteAll();
        }
        storageService.init();
    }
}
