package dev.luisvives.dawazon.common.storage.controller;

import dev.luisvives.dawazon.common.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador para devolver archivos almacenados.
 * <p>
 * Proporciona endpoints para acceder a archivos subidos.
 * </p>
 */
@Controller
public class StorageController {
    /**
     * Servicio de almacenamiento.
     */
    @Autowired
    StorageService storageService;

    /**
     * Devuelve el archivo almacenado.
     *
     * @param filename Nombre del archivo
     * @return Recurso del archivo
     */
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().body(file);
    }
}
