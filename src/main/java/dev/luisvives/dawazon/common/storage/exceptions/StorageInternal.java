package dev.luisvives.dawazon.common.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Excepción personalizada para errores internos del sistema de almacenamiento.
 * <p>
 * Se lanza cuando ocurre un fallo inesperado durante operaciones de archivos,
 * como problemas al leer, escribir o eliminar ficheros en el almacenamiento.
 * </p>
 *
 * <p>
 * La anotación {@link ResponseStatus} asegura que, al lanzarse esta excepción,
 * Spring devuelva automáticamente un estado HTTP 500 (Internal Server Error).
 * </p>
 *
 * @see StorageException
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class StorageInternal extends StorageException {

    /** Identificador de versión para serialización */
    @Serial
    private static final long serialVersionUID = 43876691117560211L;

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje Mensaje descriptivo de la excepción.
     */
    public StorageInternal(String mensaje) {
        super(mensaje);
    }
}
